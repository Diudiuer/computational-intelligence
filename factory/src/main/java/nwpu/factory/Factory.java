package nwpu.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Factory {
    //单个产品的生产成本
    private final int costA = 20;
    private final int costB = 30;
    private final int costC = 40;

    //单个产品的收益
    private final int profitA = 20;
    private final int profitB = 40;
    private final int profitC = 30;

    //工人单个生产周期的薪资以及加班费
    private final int[] salary = {100, 120, 140, 160, 180};

    //单个产品的定价
    private final int priceA = costA + profitA + salary[0];
    private final int priceB = costB + profitB + salary[0];
    private final int priceC = costC + profitC + salary[0];

    //三种产品的单个生产周期的生产个数
    private final int numA = 8;
    private final int numB = 4;
    private final int numC = 6;


    //当前工厂的总收益
    private double profit = 0;
    //当前工业开业的天数
    private int day = 0;
    //工厂当前仍未完成的订单
    private ArrayList<Order> orders;

    public Factory() {
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order o) {
        o.setDay(day);
        o.setEndDay(day + o.getTime());
        o.setFees(calculateFees(o));
        orders.add(o);
    }

    public int calculateFees(Order o) {
        int fees = 0;
        int[] nums = o.getProductsTotal();
        fees += priceA * nums[0];
        fees += priceB * nums[1];
        fees += priceC * nums[2];
        return fees;
    }

    public void removeOrder(Order o, Iterator<Order> it) {
        if (day <= o.getEndDay()) {
            profit += o.getFees();
        } else {
            profit += o.getFees() * 0.9;
        }
        it.remove();  // 使用迭代器的remove方法
    }

    public void display() {
        System.out.println("日期：" + day);
        System.out.println("当前收益：" + profit);
        for (Order o : orders) {
            System.out.println(o.toString());
        }
    }

    public void addDay(String arrangement) {
        day++;
        int[] productsNum = calculateArrangement(arrangement);
        System.out.println("昨天共生产的产品数目：" + Arrays.toString(productsNum));
        profit -= productsNum[0] * costA + productsNum[1] * costB + productsNum[2] * costC;
        profit -= calculateSalary(arrangement);
        arrangeProductToOrder(productsNum);
        display();


    }

    //计算今天安排的工作共加工的产品数目
    public int[] calculateArrangement(String arrangement) {
        int[] productsNum = {0, 0, 0};
        for (int i = 0; i < 18; i += 3) {
            productsNum[0] += (arrangement.charAt(i)-'0')*numA;
            productsNum[1] += (arrangement.charAt(i + 1)-'0')*numB;
            productsNum[2] += (arrangement.charAt(i + 2)-'0')*numC;
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
        salaryTotal += salary[0] * 2;
        for (int i = 2; i < 6; i += 1) {
            if (arrangement.charAt(i)-'0' + arrangement.charAt(i + 1) -'0'+ arrangement.charAt(i + 2)-'0' == 3) {
                salaryTotal += salary[i - 1];
            }
        }
        return salaryTotal;
    }
}
