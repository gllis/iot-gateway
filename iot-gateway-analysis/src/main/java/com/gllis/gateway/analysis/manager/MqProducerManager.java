package com.gllis.gateway.analysis.manager;

import com.alibaba.fastjson2.JSON;
import com.gllis.gateway.server.constant.GatewayLogConstant;
import com.gllis.gateway.server.domain.DeviceLog;
import com.gllis.gateway.server.domain.OutBasePacket;
import com.gllis.gateway.server.util.NetworkUtil;
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
    @Value("${iot.mq.producer.topic-deviceLog}")
    private String iotDeviceLogTopic;
    @Autowired
    private KafkaTemplate<String, Serializable> kafkaTemplate;

    /**
     * 发送解析好的数据包
     *
     * @param packet
     */
    public void sendOutPacket(OutBasePacket packet) {
        kafkaTemplate.send(iotOutPacket, packet.getSn(), packet);
        sendDevicePacketUpLog(packet.getSn(), packet);
    }


    /**
     * 发送日志
     *
     * @param sn
     * @param message
     */
    public void sendDevicePacketUpLog(String sn,  String message) {
        DeviceLog deviceLog = new DeviceLog(sn, GatewayLogConstant.DEVICE_LOG_PACKET_UP, message, NetworkUtil.lookupLocalIp());
        kafkaTemplate.send(iotDeviceLogTopic, sn, deviceLog);
    }

    /**
     * 发送日志
     *
     * @param sn
     * @param message
     */
    public void sendDevicePacketUpLog(String sn, Object message) {
        DeviceLog deviceLog = new DeviceLog(sn, GatewayLogConstant.DEVICE_LOG_PACKET_UP, JSON.toJSONString(message), NetworkUtil.lookupLocalIp());
        kafkaTemplate.send(iotDeviceLogTopic, sn, deviceLog);
    }

}
