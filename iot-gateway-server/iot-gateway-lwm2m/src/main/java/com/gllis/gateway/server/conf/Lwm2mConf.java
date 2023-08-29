package com.gllis.gateway.server.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * lwm2m配置
 *
 * @author glli
 * @date 2023/27
 */
@Data
@ConfigurationProperties(prefix = "net.lwm2m")
public class Lwm2mConf {
    private boolean enabled = false;
    private Integer coapPort = 5683;
    private Integer coapsPort = 5684;
}
