package com.wl.encryptAlgorithm;

import com.wl.Util.Base32Util;
import com.wl.Util.FileUtil;
import com.wl.beans.KeyType;
import com.wl.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/*
 *  Author : LinWang
 *  Date : 2020/11/25
 */
@SuppressWarnings("ALL")
@Component
public class OPE {

    private Map<Integer, Integer> enTable = null;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private Base32Util base32Util;

    @Autowired
    private AES aes;

    @PostConstruct
    public void init() {
        try {
            if (enTable == null) {
                String path = this.getClass().getResource("/" + Constants.OPE_ENCRYPT).getPath();
                Map<byte[], byte[]> data = fileUtil.readData(path, base32Util);
                if (data == null) {
                    enTable = batchEncrypt(Constants.ENCRYPT_START, Constants.ENCRYPT_END);
                    Map<String, String> c_data = encryptByAES(enTable);
                    fileUtil.writeDataFromOPE(path, c_data);
                } else {
                    enTable = decryptByAES(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> batchEncrypt(int start, int end) {
        Map<Integer, Integer> result = new HashMap<>();
        Map<Integer, Integer> keyTable = new HashMap<>();

        Set<Integer> D = new TreeSet<>();
        Set<Integer> R = new TreeSet<>();

        for (int i = start; i < end; i++) {
            D.add(i);
        }
        for (int i = 0; i < Constants.ENCRYPT_C_END; i++) {
            R.add(i);
        }
        for (int i = start; i < end; i++) {
            int temp = encrypt(keyTable, D, R, i);
            result.put(i, temp);
        }
        return result;
    }

    /**
     * 加密给定的明文值m
     *
     * @param keyTable 索引表，记录明文和密文的对应关系
     * @param D        明文域
     * @param R        密文域
     * @param m        明文值
     * @return m加密后的值
     */
    public int encrypt(Map<Integer, Integer> keyTable, Set<Integer> D, Set<Integer> R, int m) {
        Random rd = new Random();
        //M为明文域
        int M = D.size();
        //N为密文域
        int N = R.size();
        //d为明文域中最小的值 - 1
        int d = D.stream().min(Integer::compareTo).get() - 1;
        //r为密文域中最小的值 - 1
        int r = R.stream().min(Integer::compareTo).get() - 1;
        //y为密文域中的中值
        int y = (int) Math.ceil(N * 1.0 / 2);
        int x;
        //如果明文域的长度为1，则从密文域中随机一个值返回
        if (M == 1) {
            int c = rd.nextInt(N) + r + 1;
            return c;
        }
        //取密文域的中值，判断是否在key_table中，如果存在，则取其对应的明文域的值，如果不存在，使用HGD生成一个随机值
        if (keyTable.containsKey(r + y)) {
            x = keyTable.get(r + y) - d;
        } else {
            x = HGD(D.size(), R.size());
            keyTable.put(r + y, d + x);
        }
        //递归调用加密函数
        if (m <= d + x) {
            D = cutSet(D, d + 1, d + x);
            R = cutSet(R, r + 1, r + y);
        } else {
            D = cutSet(D, d + x + 1, d + M);
            R = cutSet(R, r + y + 1, r + N);
        }
        return encrypt(keyTable, D, R, m);
    }

    /**
     * 解密给定的密文值
     *
     * @param keyTable 索引表，记录明文和密文的对应关系
     * @param D        明文域
     * @param R        密文域
     * @param c        密文值
     * @return c解密后的值
     */
    public int decrypt(Map<Integer, Integer> keyTable, Set<Integer> D, Set<Integer> R, int c) {
        Random rd = new Random();
        //M为明文域
        int M = D.size();
        //N为密文域
        int N = R.size();
        //d为明文域中最小的值 - 1
        int d = D.stream().min(Integer::min).get() - 1;
        //r为密文域中最小的值 - 1
        int r = R.stream().min(Integer::min).get() - 1;
        //y为密文域中的中值
        int y = (int) Math.ceil(N * 1.0 / 2);
        int x;
        //如果明文域的长度为1，则从密文域中随机一个值返回
        if (M == 1) {
            return D.stream().min(Integer::min).get();
        }

        //取密文域的中值，判断是否在key_table中，如果存在，则取其对应的明文域的值，如果不存在，使用HGD生成一个随机值
        if (keyTable.containsKey(r + y)) {
            x = keyTable.get(r + y) - d;
        } else {
            x = HGD(D.size(), R.size());
            keyTable.put(r + y, d + x);
        }
        //递归调用加密函数
        if (c <= r + y) {
            D = cutSet(D, d + 1, d + x);
            R = cutSet(R, r + 1, r + y);
        } else {
            D = cutSet(D, d + x + 1, d + M);
            R = cutSet(R, r + y + 1, r + N);
        }
        return decrypt(keyTable, D, R, c);
    }

    //使用Runtime.getRuntime调用Python脚本，模拟超几何分布生成样本值
    private int HGD(int D, int R) {
        Process proc;
        StringBuffer sb = new StringBuffer();
        String path = this.getClass().getResource("/main.py").getPath().substring(1);
        try {
            String[] param = new String[]{"python", path, String.valueOf(D), String.valueOf(R)};
            proc = Runtime.getRuntime().exec(param);// 执行py文件
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            int result = proc.waitFor();
            if (result == 1) {
                //System.out.println("调用Python脚本失败");
            } else {
                //System.out.println("调用Python脚本成功");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Integer.valueOf(sb.toString());
    }

    private Set<Integer> cutSet(Set<Integer> srcList, int start, int end) {
        Set<Integer> result = new TreeSet<Integer>();
        Object[] srcArr = srcList.toArray();
        for (int i = 0; i < srcArr.length; i++) {
            int temp = (Integer) srcArr[i];
            if (temp >= start && temp <= end) {
                result.add(temp);
            }
        }
        return result;
    }

    /**
     * 将给定的Map使用AES加密
     *
     * @param data 待加密的数据
     * @return
     */
    private Map<String, String> encryptByAES(Map<Integer, Integer> data) {
        Map<String, String> result = new HashMap<>();
        Iterator<Map.Entry<Integer, Integer>> it = data.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> next = it.next();
                Integer key = next.getKey();
                Integer value = next.getValue();
                String c_key = base32Util.encoder(aes.encrypt(String.valueOf(key), KeyType.OPE_ENCRYPT.getValue()));
                String c_value = base32Util.encoder(aes.encrypt(String.valueOf(value), KeyType.OPE_ENCRYPT.getValue()));
                result.put(c_key, c_value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将加密的data加密
     *
     * @param c_data 待加密的数据
     * @return 明文Map
     */
    private Map<Integer, Integer> decryptByAES(Map<byte[], byte[]> c_data) {
        Map<Integer, Integer> data = new HashMap<>();
        Iterator<Map.Entry<byte[], byte[]>> it = c_data.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> next = it.next();
                byte[] c_key = next.getKey();
                byte[] c_value = next.getValue();
                String key = aes.decrypt(c_key, KeyType.OPE_ENCRYPT.getValue());
                String value = aes.decrypt(c_value, KeyType.OPE_ENCRYPT.getValue());
                data.put(Integer.parseInt(key), Integer.parseInt(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 将geoHash编码进行加密
     *
     * @param geoHash
     * @return
     * @throws Exception
     */
    public String encryptGeohash(String geoHash) throws Exception {
        int num = calCodeCount();
        if (num < geoHash.length()) {
            throw new Exception("geoHash计算位数不正确");
        }
        return encryptGeohash(geoHash, num);
    }

    private String encryptGeohash(String geoHash, int num) throws Exception {
        if (num % Constants.ENCODE_NUM != 0) {
            throw new Exception("加密编码的总位数计算错误");
        }
        while (geoHash.length() < num) {
            geoHash = "0" + geoHash;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < geoHash.length(); i += Constants.ENCODE_NUM) {
            int temp = Integer.parseInt(geoHash.substring(i, i + Constants.ENCODE_NUM), 2);
            String c_temp = String.format("%0" + Constants.ENCRYPT_C_NUM + "d", enTable.get(temp));
            sb.append(c_temp);
        }
        return sb.toString();
    }

    /**
     * 计算加密的时候需要的位数，基于GeoHash的位数和一次加密的位数来定
     *
     * @return
     */
    public int calCodeCount() {
        int num = Constants.LEVEL * 3;
        if (num % Constants.ENCODE_NUM == 0) {
            return num;
        } else {
            int temp = (int) Math.ceil((num * 1.0) / Constants.ENCODE_NUM);
            return temp * Constants.ENCODE_NUM;
        }
    }

    public Map<Integer, Integer> getEnTable() {
        return enTable;
    }

}


