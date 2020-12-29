package com.wl.constant;


public class Constants {

//---------------------加密算法中的常量----------------------------------------------------------
    /**
     * 保存OPE加密后的明文与密文的对照表
     */
    public static final String OPE_ENCRYPT = "OPE-KeyTable.txt";
    public static final String AES_ENCRYPT = "AES-SecretKey.txt";

    /**
     * 待加密的明文域：0 - 64 (不包括)
     * ENCODE_NUM : 每次只对二进制序列的6为进行编码
     */
    public static final int ENCRYPT_START = 0;
    public static final int ENCRYPT_END = 64;
    public static final int ENCODE_NUM = 6;

    /**
     * 密文域：0 - 10000 (不包括)
     */
    public static final int ENCRYPT_C_END = 10000;
    public static final int ENCRYPT_C_NUM = 4;


//--------------------------STCode的常量-------------------------------------------------------------
    /**
     * STCode查询方案中创建的表增加GeoHash列用来存储GeoHash编码
     */
    public static final String STCODE_COLUMN = "STCode";

    /**
     * 365天的毫秒数
     */
    public static final long MAX_MILLSECOND = 365L * 24 * 60 * 60 * 1000;

    /**
     * STCode编码的level
     */
    public static final int LEVEL = 10;
    public static final int TIME_RANGE = 527040;
    public static final double LAT_SIZE = 180.0D / Math.pow(2, LEVEL);
    public static final double LON_SIZE = 360.0D / Math.pow(2, LEVEL);
    public static final double TIME_SIZE = TIME_RANGE * 1.0 / Math.pow(2, LEVEL);

    /**
     * 递归的阈值
     */
    public static final int MAX_LENGTH = 2 * LEVEL;

    /**
     * STCode方案中创建表的所需要的列名
     */
    public static final String[] FIELDS = {"Latitude", "Longitude", "Time"};

// -----------------------FastGeo查询方案的常量--------------------------------------------
    /**
     * SSW加密算法
     */
    public static final int NUMPRIME = 4;

    /**
     * SSW加密的向量位数
     */
    public static final String[] FASTGEO_FIELDS = {"SSW_Lon", "SSW_Time", "AES_Lat"};

    /**
     * FastGeo方案中二级索引的MAP大小
     */
    public static final int MAP_SIZE = 400;

    public static final String SSW_SECRETKEY = "SSW-SecretKey.txt";

    public static final String FASTGEO_TABLE = "FastGeoTest";

}
