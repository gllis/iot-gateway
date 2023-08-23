package com.gllis.gateway.server.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mqtt配置
 *
 * @author glli
 * @date 2023/8/22
 */
@Data
@ConfigurationProperties(prefix = "net.mqtt")
public class NettyMqttConf {

    private String boosThreadName = "boss-mqtt";

    private String host;
    private Integer port = 1883;
    private boolean epollEnabled = false;
    private Integer timeout = 6;
    private Integer threadNum = 8;
    private boolean enabled = true;
}
