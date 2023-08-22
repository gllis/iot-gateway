package com.gllis.gateway.server.util;

import com.gllis.gateway.server.domain.MqttSubscribe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Mqtt消息工具类
 *
 * @author glli
 * @since 2023/8/22
 */
@Slf4j
public class MqttMessageUtil {

    public static byte[] readBytesFromByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }


    public static MqttUnsubAckMessage getUnSubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(fixedHeader, idVariableHeader);
    }

    public static int getMessageId(MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader idVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        return idVariableHeader.messageId();
    }

    public static int getMinQos(int qos1, int qos2) {
        if (qos1 < qos2) {
            return qos1;
        }
        return qos2;
    }

    public static MqttMessage getPubRelMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttMessage(fixedHeader, idVariableHeader);
    }


    public static MqttMessage getSubAckMessage(int messageId, List<Integer> qos) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload subAckPayload = new MqttSubAckPayload(qos);
        return new MqttSubAckMessage(fixedHeader, idVariableHeader, subAckPayload);
    }

    public static MqttMessage getPingRespMessage() {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader);
        return mqttMessage;
    }

    public static MqttMessage getPubComMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttMessage getPubRecMessage(int messageId, boolean isDup) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, isDup, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    public static MqttPubAckMessage getPubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(fixedHeader, idVariableHeader);
    }

    public static MqttConnAckMessage getConnectAckMessage(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader variableHeade = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(fixedHeader, variableHeade);
    }

    /**
     * 转发消息
     */
    public static void routerMsg(Channel channel, String topic, byte[] payloadBytes) {
        List<MqttSubscribe> channelList = MqttSubStoreManager.searchTopic(topic);
        if (CollectionUtils.isEmpty(channelList)) {
            return;
        }
        for (MqttSubscribe subscribe : channelList) {
            try {
                if (subscribe.getChannel() == channel) {
                    return;
                }
                log.info("{} 转发消息：{}", subscribe.getChannel().id().asShortText(), new String(payloadBytes));
                int packetId = subscribe.getQoS() == MqttQoS.AT_MOST_ONCE.value() ? 0 : MqttMessageUtil.nextId();
                MqttMessage publishMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,
                                false,
                                MqttQoS.valueOf(subscribe.getQoS()),
                                false,
                                0),
                        new MqttPublishVariableHeader(topic, packetId),
                        Unpooled.copiedBuffer(payloadBytes));
                subscribe.getChannel().writeAndFlush(publishMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 有效唯一标识messageId占据两个字节，最大限制为65535，最小限制为1
     */
    private static final int ID_MAX = ~(-1 << 16);

    private static volatile int currentId = 0;

    /**
     * 生成消息唯一标识
     * 这种生成方式更加快速，无需释放Id，无需检测Id是否已存在，但不完全保证消息标识的唯一性
     * todo 分布式情况下如何确认不重复(雪花算法变种)
     *
     * @return int
     * @author ZhangJun
     * @date 22:29 2021/3/4
     */
    public static synchronized int nextId() {
        currentId ++;
        if (currentId > ID_MAX) {
            currentId = 1;
        }
        return currentId;
    }
}
