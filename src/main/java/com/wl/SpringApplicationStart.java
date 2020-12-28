package com.wl;

import com.wl.proxy.ClientProxy;
import com.wl.proxy.ClientProxyToEncrypt;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wl.mapper")
public class SpringApplicationStart {

    public static void main(String[] args) {
        SpringApplication.run(SpringApplicationStart.class);

        //开启代理程序,将SQL语句进行加密，端口为9000
        ClientProxyToEncrypt clientProxyToEncrypt = new ClientProxyToEncrypt();
        clientProxyToEncrypt.start();

        //开启代理程序，直接进行明文查询，端口为9001
        ClientProxy clientProxy = new ClientProxy();
        clientProxy.start();
    }
}
