package com.gllis.gateway.server.main;

import com.gllis.gateway.server.conf.NettyMqttConf;
import com.gllis.gateway.server.conf.NettyUdpConf;
import com.gllis.gateway.server.main.job.BootChain;
import com.gllis.gateway.server.main.job.ServerBoot;
import com.gllis.gateway.server.mqtt.NettyMqttServer;
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

    private BootChain chain = BootChain.chain();

    @Autowired
    private NettyUdpConf nettyUdpConf;
    @Autowired
    private NettyMqttConf nettyMqttConf;

    @Autowired
    private NettyTcpServer nettyTcpServer;

    @Autowired
    private NettyUdpServer nettyUdpServer;

    @Autowired
    private NettyMqttServer nettyMqttServer;

    @PostConstruct
    public void start() {
        chain.setNext(new ServerBoot(nettyTcpServer))
                .setNext(new ServerBoot(nettyUdpServer), nettyUdpConf.isEnabled())
                .setNext(new ServerBoot(nettyMqttServer), nettyMqttConf.isEnabled())
                .end();

        chain.start();
    }

    @PreDestroy
    public void destroy() {
        chain.stop();
    }
}
