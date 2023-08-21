package com.gllis.gateway.log.conf;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * influxBD 2.x 配置
 *
 * @author glli
 * @date 2023/8/21
 */
@Configuration
@EnableConfigurationProperties({InfluxDbProperties.class})
public class InfluxDbConfiguration {


    @Bean
    public InfluxDBClient influxDBClient(InfluxDbProperties properties) {
        return InfluxDBClientFactory.create(properties.getUrl(),
                properties.getToken().trim().toCharArray(),
                properties.getOrg(), properties.getBucket());
    }

}
