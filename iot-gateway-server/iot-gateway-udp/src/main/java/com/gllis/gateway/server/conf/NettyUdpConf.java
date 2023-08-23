package com.gllis.gateway.server.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UDP配置
 *
 * @author glli
 * @date 2023/8/19
 */
@Data
@ConfigurationProperties(prefix = "net.udp")
public class NettyUdpConf {

    private String boosThreadName = "boss-udp";

    private Integer port = 3001;
    private boolean epollEnabled = false;
    private Integer timeout = 6;
    private Integer threadNum = 8;
    private boolean enabled = true;
}
