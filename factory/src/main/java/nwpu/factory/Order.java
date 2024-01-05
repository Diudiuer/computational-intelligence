package nwpu.factory;

import java.util.Arrays;

public class Order {
    //目前订单的需要的所有产品数目
    private int[] productsTotal;

    //目前订单的需要的剩余产品数目
    private int[] productsRemain;

    private int fees;

    //订单接收日期
    private int day;

    //订单结束日期
    private int endDay;

    //订单工期
    private int time;

    public Order(int[] products, int time) {
        this.productsTotal = Arrays.copyOf(products, products.length);
        this.productsRemain =Arrays.copyOf(products, products.length);
        this.time = time;
    }

    public int getFees() {
        return fees;
    }

    public void setFees(int fees) {
        this.fees = fees;
    }

    public int[] getProductsTotal() {
        return productsTotal;
    }

    public int[] getProductsRemain() {
        return productsRemain;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "订单接收日期：" + day + "  订单结束日期：" + endDay + "  订单总费用：" + fees +
                "  订单共计需要产品数目：" + Arrays.toString(productsTotal) +
                "  订单剩余需产品数目：" + Arrays.toString(productsRemain);
    }
}
