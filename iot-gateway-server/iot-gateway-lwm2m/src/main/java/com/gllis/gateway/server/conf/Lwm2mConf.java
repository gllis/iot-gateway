package com.gllis.gateway.server.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "net.lwm2m")
public class Lwm2mConf {
    private Integer coapPort = 5683;
    private Integer coapsPort = 5684;
}
