package com.gllis.gateway.server.main;

import com.gllis.gateway.server.main.job.BootChain;
import com.gllis.gateway.server.main.job.ServerBoot;
import com.gllis.gateway.server.tcp.NettyTcpServer;
import com.gllis.gateway.server.udp.NettyUdpServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 服务启动器
 *
 * @author glli
 * @date 2023/8/15
 */
@Component
public class ServerLauncher {

    private BootChain chain;

    @Autowired
    private NettyTcpServer nettyTcpServer;

    @Autowired
    private NettyUdpServer nettyUdpServer;

    @PostConstruct
    public void start() {
        if (chain == null) {
            chain = BootChain.chain();
        }
        chain.setNext(new ServerBoot(nettyTcpServer))
                .setNext(new ServerBoot(nettyUdpServer))
                .end();

        chain.start();
    }

    @PreDestroy
    public void destroy() {
        chain.stop();
    }
}
