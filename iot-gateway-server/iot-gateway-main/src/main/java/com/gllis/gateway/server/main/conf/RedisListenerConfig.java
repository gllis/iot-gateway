package com.gllis.gateway.server.main.conf;

import com.gllis.gateway.server.core.command.CommandListener;
import com.gllis.gateway.server.core.listener.SnModelListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.ArrayList;
import java.util.List;

/**
 * redis 监听配置
 *
 * @author glli
 * @date 2023/8/20
 */
public class RedisListenerConfig {
    @Value("${iot.command.add}")
    private String iotCmdAdd;
    @Value("${iot.command.remove}")
    private String iotCmdRemove;

    @Value("{iot.snModel.cacheSync}")
    private String cacheSyncTopic;

    @Autowired
    private CommandListener commandListener;

    @Autowired
    private SnModelListener snModelListener;

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        List<Topic> topics = new ArrayList();
        topics.add(new PatternTopic(iotCmdAdd));
        topics.add(new PatternTopic(iotCmdRemove));
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(commandListener, topics);
        container.addMessageListener(snModelListener, new PatternTopic(cacheSyncTopic));
        return container;
    }
}
