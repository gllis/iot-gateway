package com.gllis.gateway.server.handler;

import com.alibaba.fastjson2.JSON;
import com.gllis.gateway.server.core.connection.Connection;
import com.gllis.gateway.server.core.connection.ConnectionManager;
import com.gllis.gateway.server.core.connection.NettyConnect;
import com.gllis.gateway.server.core.manager.MqProducerManager;
import com.gllis.gateway.server.core.manager.SnModelManager;
import com.gllis.gateway.server.core.message.PacketReceiver;
import com.gllis.gateway.server.core.util.ParsePacketUtil;
import com.gllis.gateway.server.domain.MqttSubscribe;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.domain.SnModel;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.util.MqttMessageUtil;
import com.gllis.gateway.server.util.MqttSubStoreManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理通道数据
 *
 * @author glli
 * @date 2023/8/22
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {


    @Autowired
    @Qualifier("nettyConnectionManager")
    private ConnectionManager connectionManager;

    @Autowired
    private MqProducerManager mqProducerManager;

    @Autowired
    private PacketReceiver receiver;

    @Autowired
    private ParsePacketUtil parsePacketUtil;

    @Autowired
    private SnModelManager snModelManager;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("client connected conn = {}", ctx.channel());
        }
        Connection connection = new NettyConnect(mqProducerManager);
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        if (log.isDebugEnabled()) {
            log.debug("client disconnected conn = {}", connection);
        }
        ctx.fireChannelInactive();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MqttMessage) {
            MqttMessage message = (MqttMessage) msg;
            if (message == null || message.fixedHeader() == null) {
                return;
            }
            MqttMessageType messageType = message.fixedHeader().messageType();
            Channel channel = ctx.channel();
            switch (messageType) {
                case CONNECT:
                    this.connect(channel, (MqttConnectMessage) message);
                    break;
                case PUBLISH:
                    this.publish(ctx, (MqttPublishMessage) message);
                    break;
                case PUBACK:
                    this.pubAck(channel, (MqttPubAckMessage) message);
                    break;
                case SUBSCRIBE:
                    this.subscribe(channel, (MqttSubscribeMessage) message);
                    break;
                case UNSUBSCRIBE:
                    this.unSubscribe(channel, (MqttUnsubscribeMessage) message);
                    break;
                case PINGREQ:
                    this.pingReq(ctx, message);
                    break;
                case DISCONNECT:
                    this.disConnect(channel, message);
                    break;
                default:
                    if (log.isInfoEnabled()) {
                        log.info("Nonsupport server message  type of '{}'.", messageType);
                    }
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        log.error("client caught ex, conn = {}, Throwable = {}", connection, cause);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Connection connection = connectionManager.removeAndClose(ctx.channel());
                if (log.isDebugEnabled()) {
                    log.debug("6分钟没有心跳，client disconnected conn={}", connection);
                }
                ctx.fireUserEventTriggered(event);
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    public void connect(Channel channel, MqttConnectMessage msg) {
        String clientId = msg.payload().clientIdentifier();
        if (log.isInfoEnabled()) {
            log.info("MQTT CONNECT received on channel '{}', clientId is '{}'.",
                    channel.id().asShortText(), clientId);
            log.info("connect -> version:{},cleanSession:{},willFlag:{},willRetain:{}",
                    msg.variableHeader().version(), msg.variableHeader().isCleanSession(),
                    msg.variableHeader().isWillFlag(), msg.variableHeader().isWillRetain());
        }
        if (StringUtils.isNotEmpty(clientId)) {
            SnModel snModel = snModelManager.get(clientId);
            if (snModel != null) {
                Connection connection = connectionManager.get(channel);
                connection.setSn(snModel.getSn());
            } else {
                log.error("MQTT CONNECT received on channel '{}', clientId is '{}'. sn is null",
                        channel.id().asShortText(), clientId);
            }

        }
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(new MqttFixedHeader(
                        MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, true), null);
        channel.writeAndFlush(okResp);
    }

    public void pingReq(ChannelHandlerContext ctx, MqttMessage msg) {
        Connection connection = connectionManager.get(ctx.channel());
        if (StringUtils.isBlank(connection.getSn())) {
            log.error("MQTT channel '{}' pingReq sn is null", connection.getSn());
            ctx.channel().close();
        }
        if (log.isInfoEnabled()) {
            log.info("MQTT channel '{}' pingReq received.", connection.getSn());
        }

        Packet packet = Packet.builder().body("{}".getBytes(StandardCharsets.UTF_8)).build();
        parsePacketUtil.parsePacket(packet, ctx, connection); //解析sn、model
        packet.setCmd("ping");
        receiver.onReceive(packet, connection);

        MqttMessage pingResp = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false,
                MqttQoS.AT_MOST_ONCE, false, 0));
        ctx.channel().writeAndFlush(pingResp);
    }

    public void disConnect(Channel channel, MqttMessage msg) {
        if (channel.isActive()) {
            channel.close();

            if (log.isInfoEnabled()) {
                log.info("MQTT channel '{}' was closed.", channel.id().asShortText());
            }
        }
    }

    /**
     * 接收推送过来的数据
     *
     * @param ctx
     * @param msg
     */
    public void publish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        String topic = msg.variableHeader().topicName();
        MqttQoS qos = msg.fixedHeader().qosLevel();
        log.info("mqtt sub topic: {} qos:{}", topic, qos.value());

        // ack 回复收到数据
        if (qos == MqttQoS.AT_LEAST_ONCE) {
            MqttPubAckMessage ackMessage = MqttMessageUtil.getPubAckMessage(msg.variableHeader().packetId());
            ctx.channel().writeAndFlush(ackMessage);
        } else if (qos == MqttQoS.EXACTLY_ONCE) {
            MqttMessage pubRecMessage = MqttMessageUtil.getPubRecMessage(msg.variableHeader().packetId());
            ctx.channel().writeAndFlush(pubRecMessage);
        }
        ByteBuf buf = msg.content();
        int len = buf.readableBytes();
        if (len > 0) {
            byte[] data = new byte[len];
            buf.readBytes(data);
            MqttMessageUtil.routerMsg(ctx.channel(), topic, data);
            log.info("mqtt 数据:{}", new String(data));
            try {
                if (isJsonObject(new String(data))) {
                    Packet packet = Packet.builder().body(data).build();
                    Connection connection = connectionManager.get(ctx.channel());
                    connection.setTopic(topic);
                    parsePacketUtil.parsePacket(packet, ctx, connection); //解析sn、model
                    receiver.onReceive(packet, connection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ReferenceCountUtil.release(msg);
    }
    /**
     * 推送消息 ack
     *
     * @param channel
     * @param msg
     */
    public void pubAck(Channel channel, MqttPubAckMessage msg) {
        Connection connection = connectionManager.get(channel);
        int messageId = msg.variableHeader().messageId();
        log.info("收到ack指令：{} messageId:{}", connection.getSn(), messageId);

        if (connection != null) {
            Packet packet = Packet.builder().build();
            SnModel snModel = snModelManager.get(connection.getSn());
            if (snModel == null) {
                return;
            }
            packet.setSn(snModel.getSn());
            packet.setModel(snModel.getModel());
            packet.setProtocolEnum(ProtocolEnum.get(snModel.getProtocol()));
            receiver.onReceive(packet, connection);
        }
    }

    public void subscribe(Channel channel, MqttSubscribeMessage msg) {
        log.info("channelId:{} msgId: {}, subscribe mqtt {}", channel.id().asShortText(), msg.variableHeader().messageId(),
                JSON.toJSONString(msg.fixedHeader()));

        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        Connection connection = connectionManager.get(channel);
        List<Integer> grantedQoSLevels = new ArrayList<>();
        topicSubscriptions.forEach(topicSubscription -> {
            String topicFilter = topicSubscription.topicName();
            MqttQoS mqttQoS = topicSubscription.qualityOfService();
            grantedQoSLevels.add(mqttQoS.value());
            connection.setTopic(topicFilter);
            log.info("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", connection.getSn(), topicFilter, mqttQoS.value());
            MqttSubStoreManager.storeSubscribe(topicFilter, new MqttSubscribe(channel, topicFilter, mqttQoS.value()));
        });

        MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, msg.fixedHeader().isDup(),
                        MqttQoS.AT_MOST_ONCE, msg.fixedHeader().isRetain(), topicSubscriptions.size()),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                new MqttSubAckPayload(grantedQoSLevels));
        channel.writeAndFlush(subAckMessage);
    }

    public void unSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
        MqttUnsubAckMessage unSubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        channel.writeAndFlush(unSubAckMessage);
        MqttSubStoreManager.removeSubscribe(channel.id().asShortText());
    }
    /**
     * 是否为json字段
     *
     * @param str
     * @return
     */
    private boolean isJsonObject(String str) {
        boolean result = false;
        if (StringUtils.isNotBlank(str)) {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                result = true;
            } else if (str.startsWith("[") && str.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }


}
