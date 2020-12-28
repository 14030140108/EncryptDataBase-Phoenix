package com.wl.InsertDataFastGeo;

import com.wl.Util.Base32Util;
import com.wl.Util.DateUtil;
import com.wl.Util.TypeUtil;
import com.wl.beans.KeyType;
import com.wl.constant.Constants;
import com.wl.encryptAlgorithm.AES;
import com.wl.encryptAlgorithm.OPE;
import com.wl.encryptAlgorithm.SSW;
import com.wl.service.FastGeoOperator;
import com.wl.stcoder.Cube;
import com.wl.stcoder.STCodeTime;
import org.apache.phoenix.jdbc.PhoenixConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;

/*
 *  Author : LinWang
 *  Date : 2020/12/18
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings("ALL")
public class InsertData {

    @Autowired
    Base32Util base32Util;

    @Autowired
    AES aes;

    @Autowired
    OPE ope;

    @Autowired
    SSW sswLon;

    @Autowired
    SSW sswTime;

    @Autowired
    FastGeoOperator fastGeoOperator;

    /**
     * 批量插入10W条密文数据
     */
    @Test
    public void batchInsertEncryptData() {
        PhoenixConnection conn;
        PreparedStatement stmt;
        try {

            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            conn = (PhoenixConnection) DriverManager.getConnection("jdbc:phoenix:master:2181");
            conn.setAutoCommit(false);
            int mutateBatchSize = conn.getMutateBatchSize();
            String tableName = "FastGeoTest";
            tableName = base32Util.encoder(aes.encrypt(tableName, KeyType.TABLENAME_ENCRYPT.getValue()));
            String sql = "upsert into \"" + tableName + "\" values (?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);

            String path = this.getClass().getResource("/TestData.txt").getPath();
            BufferedReader in = new BufferedReader(new FileReader(path));
            String temp;
            int i = 1;

            sswLon.setup(361);
            sswTime.setup(366);

            long start = System.currentTimeMillis();
            while ((temp = in.readLine()) != null) {
                stmt.setString(1, base32Util.encoder(aes.encrypt(String.valueOf(i++), KeyType.TABLEDATA_ENCRYPT.getValue())));
                String lat = temp.split(" ")[0];
                String lon = temp.split(" ")[1];
                String time = temp.split(" ")[2] + " " + temp.split(" ")[3];

                stmt.setString(2, base32Util.encoder(aes.encrypt(lat, KeyType.TABLEDATA_ENCRYPT.getValue())));
                stmt.setString(3, base32Util.encoder(aes.encrypt(lon, KeyType.TABLEDATA_ENCRYPT.getValue())));
                stmt.setString(4, base32Util.encoder(aes.encrypt(time, KeyType.TABLEDATA_ENCRYPT.getValue())));

                int[] mLon = fastGeoOperator.getVectorLon(lon);
                int[] mTime = fastGeoOperator.getVectorTime(time);
                stmt.setString(5, sswLon.encryptVector(mLon).toString());
                stmt.setString(6, sswTime.encryptVector(mTime).toString());

                stmt.setString(7, base32Util.encoder(aes.encrypt(String.valueOf(TypeUtil.getIntFromString(lat)), KeyType.TABLEDATA_ENCRYPT.getValue())));
                stmt.addBatch();
                if (i % mutateBatchSize == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("批量插入10W条加密数据总耗时：" + (end - start) * 1.0 / 1000 + "秒");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 批量插入10W条明文数据
     */
    @Test
    public void insertData() {
        PhoenixConnection conn;
        PreparedStatement stmt = null;

        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            conn = (PhoenixConnection) DriverManager.getConnection("jdbc:phoenix:master:2181");
            conn.setAutoCommit(false);
            int mutateBatchSize = conn.getMutateBatchSize();
            String tableName = "STCodeTest";
            String sql = "upsert into \"" + tableName + "\" values (?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);

            String path = this.getClass().getResource("/TestData.txt").getPath();
            BufferedReader in = new BufferedReader(new FileReader(path));
            String temp;
            int i = 1;
            long start = System.currentTimeMillis();
            while ((temp = in.readLine()) != null) {
                stmt.setString(1, String.valueOf(i++));
                String lat = temp.split(" ")[0];
                String lon = temp.split(" ")[1];
                String time = temp.split(" ")[2] + " " + temp.split(" ")[3];

                stmt.setString(2, lat);
                stmt.setString(3, lon);
                stmt.setString(4, time);
                stmt.setString(5, Arrays.toString(fastGeoOperator.getVectorLon(lon)));
                stmt.setString(6, Arrays.toString(fastGeoOperator.getVectorTime(lon)));
                stmt.setString(7, String.valueOf(TypeUtil.getIntFromString(lat)));

                stmt.addBatch();
                if (i % mutateBatchSize == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            long end = System.currentTimeMillis();
            System.out.println("批量插入10W条明文数据总耗时：" + (end - start) * 1.0 / 1000 + "秒");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
