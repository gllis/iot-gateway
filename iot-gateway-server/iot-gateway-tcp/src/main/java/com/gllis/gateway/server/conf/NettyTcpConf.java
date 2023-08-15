package com.gllis.gateway.server.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TCP配置
 *
 * @author glli
 * @date 2023/8/15
 */
@Data
@ConfigurationProperties(prefix = "net.tcp")
public class NettyTcpConf {

    private String boosThreadName = "boss-tcp";

    private String host;
    private Integer port = 3000;
    private boolean epollEnabled = false;
    private Integer timeout = 6;
    private Integer threadNum = 8;
}
