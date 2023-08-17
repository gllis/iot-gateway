package com.gllis.gateway.analysis.consumer;

import com.gllis.gateway.analysis.HandlerFactory;
import com.gllis.gateway.analysis.ProtocolProcessing;
import com.gllis.gateway.server.domain.Packet;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消息接收
 *
 * @author glli
 * @date 2023/8/14
 */
@Slf4j
@Component
public class MqConsumer {

    @Autowired
    private HandlerFactory handlerFactory;

    @KafkaListener
    public void onMessage(ConsumerRecord<String, Packet> record) {
        try {
            Packet packet = record.value();
            if (packet == null) {
                return;
            }
            if (packet.getProtocolEnum() != null) {
                ProtocolProcessing processing = handlerFactory.getHandler(packet);
                if (processing != null) {
                    processing.handler(packet);
                } else {
                    log.error("错误的packet:{}", packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接收数据异常：{}", e);
        }
    }
}
