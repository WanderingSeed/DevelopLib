package com.morgan.app.account.model;


/**
 * 转账记录类
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年7月29日
 */
public class TransferRecord {

    private String id;
    private TransferType type;
    private String time;
    private float money;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransferType getType() {
        return type;
    }

    public void setType(TransferType type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

}
