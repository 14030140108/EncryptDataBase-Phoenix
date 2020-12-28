package com.wl.proxy;

import com.wl.constant.Constants;
import com.wl.threadHandler.SQLHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SuppressWarnings("ALL")
public class ClientProxy extends Thread {

    @Override
    public void run() {
        try {
            log.info("代理程序启动，正在监听端口：9001");
            ExecutorService exe = Executors.newFixedThreadPool(20);
            ServerSocket serverSocket = new ServerSocket(9001);
            while (true) {
                Socket socket = serverSocket.accept();
                Date now = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
                log.info("端口9001收到客户端请求时间：" + simpleDateFormat.format(now));
                exe.execute(new SQLHandler(socket, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
