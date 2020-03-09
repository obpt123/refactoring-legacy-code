package cn.xpbootcamp.legacy_code.entity;

public class User {
    private long id;
    private double balance;

    public double getBalance() {
        return balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
