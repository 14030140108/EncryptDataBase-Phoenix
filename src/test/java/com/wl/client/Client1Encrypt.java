package com.wl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client1Encrypt {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9000);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader receiveMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String data = input.readLine();
                if ("exit".equals(data)) {
                    socket.close();
                    break;
                }
                pw.write(data + "\r\n");
                pw.flush();
                String result = receiveMsg.readLine();
                if (data.contains("SELECT")) {
                    if (result.contains("失败") || result.contains("空") || result.contains("null")) {
                        System.out.println("总共查询的数据: " + 0 + "条");
                    } else {
                        String[] rs = result.split("\\$");
                        for (String r : rs) {
                            System.out.println(r);
                        }
                        System.out.println("总共查询的数据: " + rs.length + "条");
                    }
                } else {
                    System.out.println(result);
                }
            }
            System.out.println("客户端正常退出");
        } catch (IOException e) {
            System.out.println("远程服务器已关闭");
            e.printStackTrace();
        }
    }
}
