package nwpu.factory;

import java.util.*;

public class Factory {
    //单个产品的生产成本
    private final int[] cost={20,30,40};

    //单个产品的收益
    private final int[] profit={20,40,30};//160，160，180

    //工人单个生产周期的薪资以及加班费
    private final int[] salary = {80, 160, 180, 230, 280};

    //单个产品的定价
    private final int[] price={cost[0] + profit[0]+salary[0] ,cost[1] + profit[1]+salary[0] ,cost[2] + profit[2]+salary[0] };

    //三种产品的单个生产周期的生产个数
    private final int[] num={8,4,6};


    //当前工厂的总收益
    private double profitTotal= 0;
    //当前工业开业的天数
    private int day = 0;
    //工厂当前仍未完成的订单
    private ArrayList<Order> orders;

    private String[] solutions;

    public Factory() {
        this.orders = new ArrayList<>();
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public int[] getCost() {
        return cost;
    }

    public int[] getProfit() {
        return profit;
    }

    public int[] getSalary() {
        return salary;
    }

    public int[] getPrice() {
        return price;
    }

    public int[] getNum() {
        return num;
    }

    public double getProfitTotal() {
        return profitTotal;
    }

    public int getDay() {
        return day;
    }

    public void addOrder(Order o) {
        o.setDay(day);
        o.setTime(o.getEndDay()-day);
        o.setFees(calculateFees(o));
        orders.add(o);
    }

    public int calculateFees(Order o) {
        int fees = 0;
        int[] nums = o.getProductsTotal();
        fees += price[0] * nums[0];
        fees += price[1] * nums[1];
        fees += price[2] * nums[2];
        fees+=o.getTime()*500;
        return fees;
    }

    public void removeOrder(Order o, Iterator<Order> it) {
        if (day <= o.getEndDay()) {
            profitTotal += o.getFees();
        } else {
            profitTotal += o.getFees() * 0.9;
        }
        it.remove();  // 使用迭代器的remove方法
    }

    public void display() {
        System.out.println("日期：" + day);
        System.out.println("当前收益：" + profitTotal);
        for (Order o : orders) {
            System.out.println(o.toString());
        }
    }

    public void addDay() {
        day++;
        String arrangement=solutions[day-1];
        int[] productsNum = calculateArrangement(arrangement);
        System.out.println("昨天共生产的产品数目：" + Arrays.toString(productsNum));
        profitTotal -= productsNum[0] * cost[0] + productsNum[1] * cost[1] + productsNum[2] * cost[2];
        profitTotal -= calculateSalary(arrangement);
        arrangeProductToOrder(productsNum);
        display();
    }
    public void renewSolutions(String[] strs) {
        if (day == 0) {
            solutions = strs; // 直接将 strs 赋值给 solutions
        } else {
            int solutionsLength = solutions.length;
            int newLength = day + strs.length;
            String[] newSolutions = new String[newLength];

            // 复制 solutions 中前 day 个元素到新数组 newSolutions
            System.arraycopy(solutions, 0, newSolutions, 0, Math.min(day, solutionsLength));

            // 将 strs 中的所有元素添加到 newSolutions 数组的 day 之后
            for (int i = 0; i < strs.length; i++) {
                newSolutions[day + i] = strs[i];
            }

            solutions = newSolutions; // 更新 solutions 数组

        }
        System.out.println("当前的生产调度方案为："+ Arrays.toString(solutions));
    }

    //计算今天安排的工作共加工的产品数目
    public int[] calculateArrangement(String arrangement) {
        int[] productsNum = {0, 0, 0};
        for (int i = 0; i < 18; i ++) {
            if (arrangement.charAt(i)-'0'==1){
                productsNum[0] += num[0];
            }else if (arrangement.charAt(i)-'0'==2){
                productsNum[1] += num[1];
            }else if (arrangement.charAt(i)-'0'==3){
                productsNum[2] += num[2];
            }
        }
        return productsNum;
    }

    public void arrangeProductToOrder(int[] productsNum) {
        for (int i = 0; i < 3; i++) {
            for (Order o : orders) {
                if (o.getProductsRemain()[i] <= productsNum[i]) {
                    o.getProductsRemain()[i] = 0;
                } else {
                    o.getProductsRemain()[i] -= productsNum[i];
                }
            }
        }
        for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
            Order o = it.next();
            int[] productsRemain = o.getProductsRemain();
            if (productsRemain[0] == 0 && productsRemain[1] == 0 && productsRemain[2] == 0) {
                removeOrder(o, it);  // 修改了此处
            }
        }
    }

    //计算今天安排的工资费
    public int calculateSalary(String arrangement) {
        int salaryTotal = 0;
        for (int i = 0; i < 18; i ++) {
            if (arrangement.charAt(i)-'0' !=0){
                if (i<6){
                    salaryTotal+=salary[0];
                }else{
                    salaryTotal+=salary[i/3-1];
                }
            }
        }
        return salaryTotal;
    }

    public  void sortOrdersByGreedy() {
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                // 首先按截止时间排序
                int timeCompare = Integer.compare(o1.getEndDay(), o2.getEndDay());
                if (timeCompare != 0) {
                    return timeCompare;
                }
                // 如果截止时间相同，则按订单金额排序
                return Integer.compare(o1.getFees(), o2.getFees());
            }
        });
    }



    public static void main(String[] args) {
        Factory factory=new Factory();
        System.out.println(factory.calculateSalary("112333000000000000"));
    }

}
