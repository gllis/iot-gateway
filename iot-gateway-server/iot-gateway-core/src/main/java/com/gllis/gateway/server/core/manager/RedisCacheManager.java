package com.gllis.gateway.server.core.manager;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * redis 缓存管理
 *
 * @author glli
 * @date 2023/8/16
 */
@Component
public class RedisCacheManager {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public <T> T hget(String key, String hashKey, Class<T> clazz) {
        Object value = stringRedisTemplate.opsForHash().get(key, hashKey);
        if (value == null) {
            return null;
        }
        if (clazz == String.class) {
            return (T) value;
        }
        return JSON.parseObject(String.valueOf(clazz), clazz);
    }

    public Boolean del(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Long delSetKey(String key, Object... values) {
        return stringRedisTemplate.opsForSet().remove(key, values);
    }

    public Long hdel(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().delete(key, hashKey);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void hset(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Map<Object, Object> hgetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public Set<String> members(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    public void publish(String topic, Object message) {
        stringRedisTemplate.convertAndSend(topic, message);
    }


}
