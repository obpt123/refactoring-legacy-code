package cn.xpbootcamp.legacy_code.entity;

public class Order {
    private long buyerId;
    private long sellerId;
    private long productId;
    private String orderId;
    private double amount;

    public long getBuyerId() {
        return buyerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public void setBuyerId(long buyerId) {
        this.buyerId = buyerId;
    }
}
