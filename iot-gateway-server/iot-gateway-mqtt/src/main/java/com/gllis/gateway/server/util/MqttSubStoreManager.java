package com.gllis.gateway.server.util;




import com.gllis.gateway.server.domain.MqttSubscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mqtt订阅管理
 *
 * @author glli
 * @since 2023/8/22
 */
public class MqttSubStoreManager {

    private static Map<String, Map<String, MqttSubscribe>> subScribeMap = new ConcurrentHashMap<>();

    public static void storeSubscribe(String topic, MqttSubscribe subscribe) {
        subScribeMap.computeIfAbsent(topic, (key) -> new ConcurrentHashMap<>());
        subScribeMap.get(topic).put(subscribe.getChannel().id().asShortText(), subscribe);
    }

    public static void removeSubscribe(String topic, String clientId) {
        subScribeMap.get(topic).remove(clientId);
    }

    public static void removeSubscribe(String clientId) {
        for (Map<String, MqttSubscribe> item : subScribeMap.values()) {
            item.remove(clientId);
        }
    }

    /**
     * 搜索主题
     *
     * @param topicFilter
     * @return
     */
    public static List<MqttSubscribe> searchTopic(String topicFilter) {
        List<MqttSubscribe> subscribes = new ArrayList<>();
        for (String topic : subScribeMap.keySet()) {
            if (matchTopic(topic, topicFilter)) {
                subscribes.addAll(subScribeMap.get(topic).values());
            }
        }
        return subscribes;
    }

    /**
     * 判断topic与topicFilter是否匹配，topic与topicFilter需要符合协议规范
     *
     * @param topic:       主题
     * @param topicFilter: 主题过滤器
     */
    private static boolean matchTopic(String topic, String topicFilter) {
        if (topic.contains("+") || topic.contains("#")) {
            String[] topicSplits = topic.split("/");
            String[] filterSplits = topicFilter.split("/");

            if (!topic.contains("#") && topicSplits.length < filterSplits.length) {
                return false;
            }
            String level;
            for (int i = 0; i < topicSplits.length; i++) {
                level = topicSplits[i];
                if (filterSplits.length <= i) {
                    return false;
                }
                if (!level.equals(filterSplits[i]) && !level.equals("+") && !level.equals("#")) {
                    return false;
                }
            }
        } else {
            return topic.equals(topicFilter);
        }
        return true;
    }
}
