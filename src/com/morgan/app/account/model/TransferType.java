package com.morgan.app.account.model;


/**
 * 转账类型
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年7月29日
 */
public enum TransferType {
    IN(0), OUT(1);

    int value;

    private TransferType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransferType valueOf(int value) {
        switch (value) {
        case 0:
            return IN;
        case 1:
            return OUT;
        default:
            break;
        }
        return IN;
    }
}
