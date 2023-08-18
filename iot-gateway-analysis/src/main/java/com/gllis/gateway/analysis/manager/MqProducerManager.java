package com.gllis.gateway.analysis.manager;

import com.gllis.gateway.server.domain.DeviceLog;
import com.gllis.gateway.server.domain.OutBasePacket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;


/**
 * kafka 发送管理器
 *
 * @author glli
 * @date 2023/8/16
 */
@Component
public class MqProducerManager {

    @Value("${iot.mq.producer.topic-outPacket}")
    private String iotOutPacket;
    @Autowired
    private KafkaTemplate<String, Serializable> kafkaTemplate;

    /**
     * 发送解析好的数据包
     *
     * @param packet
     */
    public void sendOutPacket(OutBasePacket packet) {
        kafkaTemplate.send(iotOutPacket, packet.getSn(), packet);
    }



}
