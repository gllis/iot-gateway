package com.gllis.gateway.server.core.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gllis.gateway.server.constant.RedisKeyConstant;
import com.gllis.gateway.server.domain.SnModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SN 管理
 *
 * @author glli
 * @created 2023/8/20.
 */
@Slf4j
@Component
public class SnModelManager {

    @Autowired
    private RedisCacheManager redisCacheManager;

    private static Cache<String, Object> caffeineCache = Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite(7200, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(300000)
            .build();

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public SnModel get(String key) {
        SnModel snModel;
        String rkey = RedisKeyConstant.PRE_DEVICE_SN + key;
        snModel = (SnModel) caffeineCache.getIfPresent(rkey);
        if (snModel != null) {
            return snModel;
        }
        Map map = redisCacheManager.hgetAll(rkey);
        snModel = this.exchange(map);
        if (null == snModel) {
            return null;
        }
        caffeineCache.put(rkey, snModel);
        return snModel;
    }

    public void set(String rkey, SnModel snModel) {
        caffeineCache.put(rkey, snModel);
    }

    /**
     * 清空本地缓存
     * @param key
     */
    public void remove(String key) {
        caffeineCache.asMap().remove(key);
    }

    public SnModel exchange(Map<String, String> value) {
        try {
            if (value.size() > 0) {
                SnModel snModel = new SnModel();
                for (Map.Entry<String, String> entry : value.entrySet()) {
                    String field = entry.getKey();
                    String val = entry.getValue();
                    if (field.equals("id")) {
                        snModel.setId(Integer.parseInt(val));
                        snModel.setDeviceId(Integer.parseInt(val));
                    } else if (field.equals("sn")) {
                        snModel.setSn(val);
                    } else if (field.equals("model")) {
                        snModel.setModel(val);
                    }
                }
                return snModel;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
