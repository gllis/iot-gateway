package com.gllis.gateway.server.core.manager;

import com.gllis.gateway.server.core.constant.SysConstant;
import com.gllis.gateway.server.core.util.NetworkUtil;
import com.gllis.gateway.server.domain.DeviceLog;
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

    @Value("{iot.deviceLog.topic:iot.device.log}")
    private String iotDeviceLogTopic;
    @Autowired
    private KafkaTemplate<String, Serializable> kafkaTemplate;

    /**
     * 发送日志
     *
     * @param sn
     * @param message
     */
    public void sendDevicePacketDownLog(String sn,  String message) {
        DeviceLog deviceLog = new DeviceLog(sn, SysConstant.DEVICE_LOG_PACKET_DOWN, message, NetworkUtil.lookupLocalIp());
        kafkaTemplate.send(iotDeviceLogTopic, sn, deviceLog);
    }

    /**
     * 发送日志
     *
     * @param sn
     * @param message
     */
    public void sendDevicePacketUpLog(String sn,  String message) {
        DeviceLog deviceLog = new DeviceLog(sn, SysConstant.DEVICE_LOG_PACKET_UP, message, NetworkUtil.lookupLocalIp());
        kafkaTemplate.send(iotDeviceLogTopic, sn, deviceLog);
    }


}
