package com.gllis.gateway.server.core.manager;

import com.gllis.gateway.server.constant.GatewayLogConstant;
import com.gllis.gateway.server.util.NetworkUtil;
import com.gllis.gateway.server.domain.DeviceLog;
import com.gllis.gateway.server.domain.Packet;
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

    @Value("${iot.mq.topic-deviceLog}")
    private String iotDeviceLogTopic;
    @Value("${iot.mq.topic-packet}")
    private String iotPacketTopic;

    @Autowired
    private KafkaTemplate<String, Serializable> kafkaTemplate;

    /**
     * 发送日志
     *
     * @param sn
     * @param message
     */
    public void sendDevicePacketDownLog(String sn,  String message) {
        DeviceLog deviceLog = new DeviceLog(sn, GatewayLogConstant.DEVICE_LOG_PACKET_DOWN, message, NetworkUtil.lookupLocalIp());
        kafkaTemplate.send(iotDeviceLogTopic, sn, deviceLog);
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
     * 转发报文解析
     *
     * @param packet
     */
    public void sendPacket(Packet packet) {
        if (packet.getSn() == null) {
            return;
        }
        kafkaTemplate.send(iotPacketTopic, packet.getSn(), packet);
    }
}
