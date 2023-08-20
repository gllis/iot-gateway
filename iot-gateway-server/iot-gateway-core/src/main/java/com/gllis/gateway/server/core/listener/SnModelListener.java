package com.gllis.gateway.server.core.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.gllis.gateway.server.constant.RedisKeyConstant;
import com.gllis.gateway.server.core.manager.SnModelManager;
import com.gllis.gateway.server.domain.SnModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * sn缓存更新监听
 *
 * @author glli
 * @date 2023/8/20
 */
@Slf4j
@Component
public class SnModelListener implements MessageListener {
    @Value("{iot.snModel.cacheSync}")
    private String cacheSyncTopic;
    @Autowired
    private SnModelManager snModelManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (pattern == null) {
            return;
        }
        String topic = new String(pattern);
        if (cacheSyncTopic.equals(topic)) {
            Map<String, String> map = JSON.parseObject(new String(message.getBody()), new TypeReference<>() {});
            String data = map.get("data");
            Map<String, String> mapdata = JSON.parseObject(data, new TypeReference<>() {});
            SnModel snModel = snModelManager.exchange(mapdata);
            String rkey = RedisKeyConstant.PRE_DEVICE_SN + snModel.getSn();
            if ("delete".equals(map.get("optType"))) {
                snModelManager.remove(rkey);
            } else {
                if(StringUtils.isBlank( snModel.getModel() )){
                    return;
                }
                if(StringUtils.isBlank( snModel.getProtocol() )){
                    return;
                }
                snModelManager.set(rkey, snModel);
            }
        }

    }
}
