package com.gllis.gateway.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关服务
 *
 * @author glli
 * @date 2023/8/14
 */
@SpringBootApplication
@ComponentScan("com.gllis.gateway.server")
public class GatewayServer {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServer.class, args);
    }
}
