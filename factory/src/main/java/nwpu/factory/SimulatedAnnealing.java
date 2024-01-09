package nwpu.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class SimulatedAnnealing {

    private static final int MAX_ITER = 10000;
    private static final double INITIAL_TEMP = 100.0;
    private static final double FINAL_TEMP = 1e-3;
    private static final double ALPHA = 0.95;

    private Factory factory;

    private ArrayList<Order> orders;

    private int day;

    public SimulatedAnnealing(Factory factory) {
        this.factory = factory;
        this.orders = new ArrayList<>();
        this.day = factory.getDay();
    }

    private String[] generateInitialSolution() {
        //首先计算每个订单中，需要的三种产品的数目
        int[] totalProductsNums = {0, 0, 0};
        orders=new ArrayList<>();
        for (Order order : factory.getOrders()) {
            Order clonedOrder = order.clone();
            orders.add(clonedOrder);
        }
        for (Order o :
                orders) {
            totalProductsNums[0] += o.getProductsRemain()[0];
            totalProductsNums[1] += o.getProductsRemain()[1];
            totalProductsNums[2] += o.getProductsRemain()[2];
        }
        int[] num = factory.getNum();
        int[] numPeriod = new int[3];
        for (int i = 0; i < 3; i++) {
            if (totalProductsNums[i] % num[i] == 0) {
                numPeriod[i] = totalProductsNums[i] / num[i];
            } else {
                numPeriod[i] = totalProductsNums[i] / num[i] + 1;
            }
        }
        int day = (numPeriod[0] + numPeriod[1] + numPeriod[2]) / 6 + 1;
        return splitIntoEighteen(generateRandomString(numPeriod[0], numPeriod[1], numPeriod[2], day * 6 - numPeriod[0] - numPeriod[1] - numPeriod[2]));
    }

    public String[] splitIntoEighteen(String str) {
        // 确保字符串长度是18的倍数
        if (str.length() % 6 != 0) {
            throw new IllegalArgumentException("字符串长度必须是6的倍数");
        }

        // 计算应该有多少个子字符串
        int numOfSubstrings = str.length() / 6;
        String[] result = new String[numOfSubstrings];

        for (int i = 0; i < numOfSubstrings; i++) {
            result[i] = str.substring(i * 6, (i + 1) * 6);
            result[i] += "000000000000";
        }

        return result;
    }

    public String generateRandomString(int num1, int num2, int num3, int num4) {
        char[] chars = new char[num1 + num2 + num3 + num4];
        int index = 0;

        // 填充1、2、3和0
        for (int i = 0; i < num1; i++) chars[index++] = '1';
        for (int i = 0; i < num2; i++) chars[index++] = '2';
        for (int i = 0; i < num3; i++) chars[index++] = '3';
        for (int i = 0; i < num4; i++) chars[index++] = '0';

        // 使用Fisher-Yates洗牌算法打乱数组顺序
        shuffleArray(chars);

        // 将字符数组转换为字符串
        return new String(chars);
    }

    private void shuffleArray(char[] array) {
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // 简单的交换
            char a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }


    public String[] optimizationSolution(String[] solution) {
        String[] newSolution = new String[solution.length];
        for (int i = 0; i < solution.length; i++) {
            newSolution[i] = repairSolution(solution[i]);
        }
        return newSolution;
    }

    private String repairSolution(String solution) {
        StringBuilder sb = new StringBuilder(solution);
        int numCycles = sb.length() / 3;

        // 用于记录每个周期是否为"000"
        boolean[] isZeroCycle = new boolean[numCycles];

        for (int i = 0; i < numCycles; i++) {
            if (sb.substring(i * 3, i * 3 + 3).equals("000")) {
                isZeroCycle[i] = true;
            } else {
                isZeroCycle[i] = false;
            }
        }

        // 将所有非"000"周期移动到前面
        int nonZeroIndex = 0;
        for (int i = 0; i < numCycles; i++) {
            if (!isZeroCycle[i]) {
                sb.replace(nonZeroIndex * 3, nonZeroIndex * 3 + 3, sb.substring(i * 3, i * 3 + 3));
                nonZeroIndex++;
            }
        }

        // 填充剩余的周期为"000"
        for (int i = nonZeroIndex; i < numCycles; i++) {
            sb.replace(i * 3, i * 3 + 3, "000");
        }
        return sb.toString();
    }

    public double calculateEnergy(String[] solution) {
        double profit = 0;
        this.day= factory.getDay();
        orders=new ArrayList<>();
        for (Order order : factory.getOrders()) {
            Order clonedOrder = order.clone();
            orders.add(clonedOrder);
        }
        int[] cost = factory.getCost();
        for (String s : solution) {
            profit -= factory.calculateSalary(s);
            int[] productsNum = factory.calculateArrangement(s);
            for (int i = 0; i < 3; i++) {
                profit -= cost[i] * productsNum[i];
            }
            profit += arrangeProductToOrder(productsNum);
            day++;
        }
        return profit;
    }

    public double arrangeProductToOrder(int[] productsNum) {
        double profit = 0;
        for (int i = 0; i < 3; i++) {
            for (Order o : orders) {
                if (o.getProductsRemain()[i] <= productsNum[i]) {
                    o.getProductsRemain()[i] = 0;
                } else {
                    o.getProductsRemain()[i] -= productsNum[i];
                }
            }
        }
        for (Iterator<Order> it = orders.iterator(); it.hasNext(); ) {
            Order o = it.next();
            int[] productsRemain = o.getProductsRemain();
            if (productsRemain[0] == 0 && productsRemain[1] == 0 && productsRemain[2] == 0) {
                profit += removeOrder(o, it);  // 修改了此处
            }
        }
        return profit;
    }

    public double removeOrder(Order o, Iterator<Order> it) {
        it.remove();  // 使用迭代器的remove方法
        if (day + 1 <= o.getEndDay()) {
            System.out.println("day"+day);
            System.out.println("endday:" + o.getEndDay());
            System.out.println("fees:" + o.getFees());
            return o.getFees();
        } else {
            System.out.println(day);
            return o.getFees() * 0.9;
        }
    }

    public String[] generateNeighbor(String[] solution) {
        int swapTimes = solution.length;
        Random random = new Random();
        StringBuilder combinedSolution = new StringBuilder();

        // 将所有字符串合并成一个长字符串
        for (String s : solution) {
            combinedSolution.append(s);
        }

        // 进行给定次数的随机交换
        for (int i = 0; i < swapTimes; i++) {
            int index1 = random.nextInt(combinedSolution.length());
            int index2 = random.nextInt(combinedSolution.length());
            char temp = combinedSolution.charAt(index1);
            combinedSolution.setCharAt(index1, combinedSolution.charAt(index2));
            combinedSolution.setCharAt(index2, temp);
        }

        // 将修改后的长字符串拆分回原始长度的字符串数组
        String[] newSolution = new String[solution.length];
        int len = solution[0].length(); // 假设所有字符串长度相同
        for (int i = 0; i < solution.length; i++) {
            newSolution[i] = combinedSolution.substring(i * len, (i + 1) * len);
        }
        return newSolution;
    }

    private static double acceptanceProbability(double energy, double newEnergy, double temperature) {
        if (newEnergy < energy) {
            return 1.0;
        }
        return Math.exp(-(energy - newEnergy) / temperature);
    }

    public String[] generateBestSolution() {
        String[] currentSolution = generateInitialSolution();
        currentSolution = optimizationSolution(currentSolution);
        String[] bestSolution = currentSolution;
        double currentEnergy = calculateEnergy(currentSolution);
        double bestEnergy = currentEnergy;

        double temp = INITIAL_TEMP;

        Random random = new Random();

        while (temp > FINAL_TEMP) {
            for (int i = 0; i < MAX_ITER; i++) {
                String[] newSolution = generateNeighbor(currentSolution);
                newSolution = optimizationSolution(newSolution);
                double newEnergy = calculateEnergy(newSolution);
                System.out.println("newSolution:" + Arrays.toString(newSolution));
                System.out.println("newE:" + newEnergy);

                if (acceptanceProbability(currentEnergy, newEnergy, temp) > random.nextDouble()) {
                    currentSolution = newSolution;
                    currentEnergy = newEnergy;
                    if (currentEnergy > bestEnergy) {
                        bestSolution = currentSolution;
                        bestEnergy = currentEnergy;
                    }
                }
            }

            temp *= ALPHA;
            System.out.println("bestSolution:" + Arrays.toString(bestSolution));
            System.out.println("bestEnergy:" + bestEnergy);
        }
        return bestSolution;
    }


    public static void main(String[] args) {
        Factory factory1 = new Factory();
        Order o = new Order(new int[]{20, 10, 34}, 1);
        factory1.addOrder(new Order(new int[]{20, 10, 34}, 1));
        System.out.println(factory1.calculateFees(o));
        //{56,19,58};   20,10,34  950   cost=
        //{8,4,6};      222111333333   cost=2280   salary=480  600
        // 16,8,48

        SimulatedAnnealing s = new SimulatedAnnealing(factory1);
//        int i = 0;
//        ArrayList<Order> orders = new ArrayList<>();
//        orders.add(new Order(new int[]{20, 10, 34}, 7));
//        orders.add(new Order(new int[]{13, 1, 3}, 8));
//        orders.add(new Order(new int[]{23, 8, 21}, 15));
        //s.generateNeighbor(s.generateInitialSolution());
        s.generateBestSolution();
        System.out.println(s.calculateEnergy(new String[]{"332233000000000000", "001030000000000000", "011302000000000000"}));
        System.out.println(s.calculateEnergy(new String[]{"332233000000000000", "001030000000000000", "011302000000000000"}));
    }
}
