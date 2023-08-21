package com.gllis.gateway.log.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * influx 属性
 *
 * @author glli
 * @date 2023/8/21
 */
@Data
@ConfigurationProperties(
        prefix = "spring.data.influx"
)
public class InfluxDbProperties {
    private String url;
    private String token;
    private String org;
    private String bucket;

}
