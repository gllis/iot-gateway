package com.gllis.gateway.log.consumer;

import com.gllis.gateway.log.biz.LogWriter;
import com.gllis.gateway.server.domain.DeviceLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 日志接收
 *
 * @author glli
 * @date 2023/8/21
 */
@Slf4j
@Component
public class MqConsumer {

    @Autowired
    private LogWriter logWriter;

    @KafkaListener(topics = "${iot.mq.consumer.topic-deviceLog}", groupId = "${iot.mq.consumer.group-id}")
    public void message(List<DeviceLog> list) {
        for (DeviceLog deviceLog : list) {
            logWriter.addQueue(deviceLog);
        }
    }
}
