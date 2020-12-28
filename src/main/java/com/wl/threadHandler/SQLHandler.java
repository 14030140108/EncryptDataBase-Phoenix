package com.wl.threadHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.Socket;

@Getter
@Setter
@Slf4j
public class SQLHandler implements Runnable {

    private Socket socket;
    private boolean isEncrypt;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public SQLHandler(Socket socket, boolean isEncrypt) {
        this.socket = socket;
        this.isEncrypt = isEncrypt;
        try {
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不同的SQL语句分发给不同的Handler
     * 1. Create语句 --> CreateHandler处理该语句
     * 2. Upsert语句 --> UpsertHandler处理该语句
     * 3. Select语句 --> SelectHandler处理该语句
     */
    private void readDataFromClient() {
        String readData, result;
        try {
            while (true) {
                readData = readMsg();
                log.info(readData);
                Handler handler = parseSQL(readData);
                result = handler.executeHandler();
                sendMsg(result);
            }
        } catch (Exception e) {
            closeSocket();
            log.info("客户端socket关闭连接");
        }
    }

    /**
     * 解析SQL语句，根据SQL语句的不同返回不同的Handler处理
     *
     * @param sql sql语句
     * @return Handler
     */
    private Handler parseSQL(String sql) {
        String oper = sql.split(" ")[0];
        if ("CREATE".equalsIgnoreCase(oper)) {
            return new CreateHandler(sql, isEncrypt);
        } else if ("UPSERT".equalsIgnoreCase(oper)) {
            return new UpsertHandler(sql, isEncrypt);
        } else {
            return new SelectHandler(sql, isEncrypt);
        }
    }

    private void sendMsg(String message) {
        if (message == null) {
            out.write("查询结果为空" + "\r\n");
            out.flush();
        }
        out.write(message + "\r\n");
        out.flush();
    }

    private String readMsg() throws IOException {
        String result = in.readLine();
        if (StringUtils.isEmpty(result)) {
            throw new NullPointerException("result is null");
        }
        return result;
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log.info("线程开始运行");
        readDataFromClient();
        log.info("线程结束运行");
    }
}
