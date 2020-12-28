package com.wl.Util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@SuppressWarnings("ALL")
public class FileUtil {

    public String readKey(String fileName, int type) {
        try {
            String data = null;
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String result;
            while ((result = in.readLine()) != null) {
                String ty = result.split(":")[0].trim();
                if (Integer.parseInt(ty) == type) {
                    data = result.split(":")[1].trim();
                    break;
                }
            }
            in.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeKey(String fileName, int type, String secretKey) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, true));
            pw.write(String.valueOf(type));
            pw.write(" : ");
            pw.write(secretKey);
            pw.write("\r\n");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取OPE加密后的数据
     *
     * @param name 文件名
     * @return 明文和密文的对照表
     * @throws IOException 异常
     */
    public Map<byte[], byte[]> readData(String name, Base32Util base32Util) throws IOException {
        Map<byte[], byte[]> result = null;
        File file = new File(name);
        if (!file.exists() || file.length() == 0) {
            return result;
        }
        result = new HashMap<>();
        BufferedReader in = new BufferedReader(new FileReader(name));
        String temp;
        while ((temp = in.readLine()) != null) {
            byte[] c_key = base32Util.decoder(temp.split(":")[0].trim());
            byte[] c_value = base32Util.decoder(temp.split(":")[1].trim());
            result.put(c_key, c_value);
        }
        return result;
    }

    /**
     * 将OPE加密的对照表使用使用AES加密后存储文件中
     *
     * @param name 文件名
     * @param data 数据
     */
    public void writeDataFromOPE(String name, Map<String, String> data) {
        Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(name));
            while (it.hasNext()) {
                Map.Entry<String, String> next = it.next();
                String key = next.getKey();
                String value = next.getValue();
                out.write(key);
                out.write(" : ");
                out.write(value);
                out.write("\r\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将SSW的SecretKey保存入文件中
     *
     * @param fileName  文件名
     * @param secretKey 密钥
     */
    public void writeSSWKey(String fileName, String secretKey, int length) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, true));
            pw.write(String.valueOf(length));
            pw.write(" : ");
            pw.write(secretKey);
            pw.write("\r\n");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取SSW的Key
     *
     * @param fileName 文件名
     * @return 密钥
     */
    public String readSSWKey(String fileName, int length) {
        try {
            String data = null;
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String result;
            while ((result = in.readLine()) != null) {
                String ty = result.split(":")[0].trim();
                if (Integer.parseInt(ty) == length) {
                    data = result.split(":")[1].trim();
                    break;
                }
            }
            in.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
