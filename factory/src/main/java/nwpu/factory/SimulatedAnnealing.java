package nwpu.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class SimulatedAnnealing {

    private static final int MAX_ITER = 1;
    private static final double INITIAL_TEMP = 100.0;
    private static final double FINAL_TEMP = 1e-3;
    private static final double ALPHA = 0.95;

    private int numP;

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
        this.numP=day;
        //System.out.println(numP);
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
        StringBuilder nonZeroBuilder = new StringBuilder();
        StringBuilder zeroBuilder = new StringBuilder();

        // 遍历字符串中的每个字符
        for (int i = 0; i < solution.length(); i++) {
            char ch = solution.charAt(i);
            if (ch == '0') {
                zeroBuilder.append(ch);  // 如果字符是0，则添加到zeroBuilder
            } else {
                nonZeroBuilder.append(ch); // 如果字符非0，则添加到nonZeroBuilder
            }
        }

        // 将非零值和零值的StringBuilder合并
        nonZeroBuilder.append(zeroBuilder);

        return nonZeroBuilder.toString();
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
            return o.getFees();
        } else {
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

    public String[] generateNeighbor1(String[] solution, double temperature) {
        Random random = new Random();
        // 根据温度计算交换次数
        int swapTimes = calculateSwapTimes(temperature);

        String[] newSolution = Arrays.copyOf(solution, solution.length);

        for (int swap = 0; swap < swapTimes; swap++) {
            int day1 = random.nextInt(newSolution.length);
            int day2 = random.nextInt(newSolution.length);
            while (day2 == day1) {
                day2 = random.nextInt(newSolution.length); // 确保day1和day2不同
            }

            // 确保day1在day2之前
            if (day1 > day2) {
                int temp = day1;
                day1 = day2;
                day2 = temp;
            }

            char[] charsDay1 = newSolution[day1].toCharArray();
            char[] charsDay2 = newSolution[day2].toCharArray();

            // 在day1中查找第一个0，和day2中的第一个非0进行交换
            for (int i = 0; i < charsDay1.length; i++) {
                if (charsDay1[i] == '0') {
                    for (int j = 0; j < charsDay2.length; j++) {
                        if (charsDay2[j] != '0') {
                            // 交换值
                            charsDay1[i] = charsDay2[j];
                            charsDay2[j] = '0';
                            break;
                        }
                    }
                    break;
                }
            }

            // 将修改后的字符数组转换回字符串
            newSolution[day1] = new String(charsDay1);
            newSolution[day2] = new String(charsDay2);
        }

        return newSolution;
    }

    private int calculateSwapTimes(double temperature) {
        // 示例：根据温度线性计算交换次数
        // 可根据需要调整这个公式
        return (int) Math.max(1, (numP*temperature/100)); // 确保至少有一次交换
    }

    public String[] generateNeighbor2(String[] solution, double temperature) {
        Random random = new Random();
        // 根据温度计算交换次数
        int swapTimes = calculateSwapTimes(temperature);

        String[] newSolution = Arrays.copyOf(solution, solution.length);

        for (int swap = 0; swap < swapTimes; swap++) {
            int day1 = random.nextInt(newSolution.length);
            int day2 = random.nextInt(newSolution.length);
            while (day2 == day1) {
                day2 = random.nextInt(newSolution.length); // 确保day1和day2不同
            }

            char[] charsDay1 = newSolution[day1].toCharArray();
            char[] charsDay2 = newSolution[day2].toCharArray();

            // 在两个天中交换非零元素
            swapNonZeroElements(charsDay1, charsDay2);

            // 将修改后的字符数组转换回字符串
            newSolution[day1] = new String(charsDay1);
            newSolution[day2] = new String(charsDay2);
        }

        return newSolution;
    }

    private void swapNonZeroElements(char[] day1, char[] day2) {
        Random random = new Random();
        for (int i = 0; i < day1.length; i++) {
            if (day1[i] != '0' && day2[i] != '0') {
                // 交换非零元素
                char temp = day1[i];
                day1[i] = day2[i];
                day2[i] = temp;
            }
        }
    }



    private double acceptanceProbability(double energy, double newEnergy, double temperature) {
        if (newEnergy >energy) {
            return 1.0;
        }
        double x=energy - newEnergy;
        int k=1;
        //System.out.println(energy - newEnergy);
        if (x<=50000&&x>20000){
            k=10000000;
        }else if (x<=20000&&x>10000){
            k=5000000;
        }
        else if (x<=10000&&x>5000){
            k=2000000;
        }
        else if (x<=5000&&x>2000){
            k=1000000;
        }
        else if (x<=2000&&x>1000){
            k=500000;
        }
        else if (x<=1000&&x>500){
            k=200000;
        }
        else if (x<=500&&x>100){
            k=80000;
        }

        else if (x<=100){
            k=20000;
        }
        return Math.exp(-(energy - newEnergy) / k*temperature);
    }

    public String[] generateBestSolution() {
        String[] currentSolution = generateInitialSolution();
        //currentSolution= new String[]{"330020000000000000", "231300000000000000", "123310000000000000"};
        currentSolution = optimizationSolution(currentSolution);
        String[] bestSolution = currentSolution;
        double currentEnergy = calculateEnergy(currentSolution);
        double bestEnergy = currentEnergy;
//        System.out.println("initsolution:"+ Arrays.toString(currentSolution));
//        System.out.println("initE:"+bestEnergy);

        double temp = INITIAL_TEMP;

        Random random = new Random();

        while (temp > FINAL_TEMP) {
            for (int i = 0; i < MAX_ITER; i++) {
                String[] newSolution1 = generateNeighbor1(currentSolution,temp);
                newSolution1 = optimizationSolution(newSolution1);
                String[] newSolution = generateNeighbor2(newSolution1,temp);
                newSolution = optimizationSolution(newSolution);
                double newEnergy = calculateEnergy(newSolution);
//                System.out.println("Solution:" + Arrays.toString(newSolution));
//                System.out.println("Energy:" + newEnergy);
//                System.out.println(acceptanceProbability(currentEnergy, newEnergy, temp));
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
//            System.out.println("bestSolution:" + Arrays.toString(bestSolution));
//            System.out.println("bestEnergy:" + bestEnergy);
        }
        return bestSolution;
    }


    public static void main(String[] args) {
        Factory factory1 = new Factory();
        Order o = new Order(new int[]{20, 10, 34}, 1);
        factory1.addOrder(new Order(new int[]{20, 10, 34}, 1));
        factory1.addOrder(new Order(new int[]{21, 60, 50}, 2));
        factory1.addOrder(new Order(new int[]{70, 20, 80}, 3));
        factory1.addOrder(new Order(new int[]{100, 10, 10}, 5));
        factory1.addOrder(new Order(new int[]{200, 170, 200}, 8));
        factory1.addOrder(new Order(new int[]{30, 40, 3}, 2));
        factory1.addOrder(new Order(new int[]{40, 200, 3}, 6));
        factory1.addOrder(new Order(new int[]{50, 100, 80}, 9));
        factory1.addOrder(new Order(new int[]{20, 20, 10}, 12));
        factory1.addOrder(new Order(new int[]{20, 10, 34}, 11));
        factory1.addOrder(new Order(new int[]{30, 60, 50}, 6));
        factory1.addOrder(new Order(new int[]{70, 20, 80}, 11));
        factory1.addOrder(new Order(new int[]{100, 10, 10}, 12));
        factory1.addOrder(new Order(new int[]{200, 170, 200}, 11));
        factory1.addOrder(new Order(new int[]{30, 40, 3}, 10));
        factory1.addOrder(new Order(new int[]{40, 200, 3}, 15));
        factory1.addOrder(new Order(new int[]{50, 100, 80}, 14));
        factory1.addOrder(new Order(new int[]{20, 20, 10}, 12));
        factory1.sortOrdersByGreedy();
        //{56,19,58};   20,10,34  950   cost=
        //{8,4,6};      222111333333   cost=2280   salary= 480 600  9500-2280
        // 16,8,48

        SimulatedAnnealing s = new SimulatedAnnealing(factory1);
//        int i = 0;
//        ArrayList<Order> orders = new ArrayList<>();
//        orders.add(new Order(new int[]{20, 10, 34}, 7));
//        orders.add(new Order(new int[]{13, 1, 3}, 8));
//        orders.add(new Order(new int[]{23, 8, 21}, 15));
        //s.generateNeighbor(s.generateInitialSolution());
        s.generateBestSolution();
        //s.generateInitialSolution();
        //System.out.println(s.acceptanceProbability(100,0,100));

        //System.out.println(Arrays.toString(s.generateNeighbor2(new String[]{"113000000000000000", "323100000000000000", "323320000000000000"},100)));
    }
}
