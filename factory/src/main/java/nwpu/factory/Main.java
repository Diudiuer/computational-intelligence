package nwpu.factory;

import java.util.Scanner;

public class Main {
    static Factory factory = new Factory();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("将要执行的操作：1.增加新的订单  2.查看当前剩余订单  3.进入到下一天   4.生成生产方案");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    addOrder();
                    break;
                case 2:
                    factory.display();
                    break;
                case 3:
                    factory.addDay();
                    break;
                case 4:
                    factory.renewSolutions(new SimulatedAnnealing(factory).generateBestSolution());
                default:
                    System.out.println("无效的输入，请输入1-3之间的数字。");
                    break;
            }
        }
    }

    public static void addOrder(){
        Scanner scanner=new Scanner(System.in);
        int[] productsNums=new int[3];
        System.out.println("请输入需要的产品A的数量：");
        productsNums[0]=scanner.nextInt();
        System.out.println("请输入需要的产品B的数量：");
        productsNums[1]=scanner.nextInt();
        System.out.println("请输入需要的产品C的数量：");
        productsNums[2]=scanner.nextInt();
        System.out.println("请输入截止日期：");
        int endDay=scanner.nextInt();
        factory.addOrder(new Order(productsNums,endDay));
        System.out.println("成功添加新订单");
    }
}