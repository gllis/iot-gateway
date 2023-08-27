package com.gllis.gateway.server.conf;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(Lwm2mConf.class)
public class AutoConfiguration {

}
