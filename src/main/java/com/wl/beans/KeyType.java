package com.wl.beans;

/*
 *  Author : LinWang
 *  Date : 2020/12/12
 */
public enum KeyType {

    /**
     * AES加密表名和列名的密钥类型
     */
    TABLENAME_ENCRYPT(1),

    /**
     * AES加密表中内容的密钥类型
     */
    TABLEDATA_ENCRYPT(2),

    /**
     * AES加密OPE后的结果
     */
    OPE_ENCRYPT(3);

    private int value;

    KeyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
