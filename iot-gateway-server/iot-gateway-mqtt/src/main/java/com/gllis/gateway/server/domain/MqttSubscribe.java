package com.gllis.gateway.server.domain;

import io.netty.channel.Channel;
import lombok.Data;

import java.io.Serializable;


/**
 * mqtt订单主题信息
 *
 * @author glli
 * @since 2023/8/22
 */
@Data
public class MqttSubscribe implements Serializable {
    /**
     * 客户端通道
     */
    private Channel channel;
    /**
     * 主题
     */
    private String topic;
    /**
     * qos At most once，至多一次; At least once，至少一次; Exactly once，确保只有一次
     */
    private int qoS;

    public MqttSubscribe(Channel channel, String topic, int qoS) {
        this.channel = channel;
        this.topic = topic;
        this.qoS = qoS;
    }
}
