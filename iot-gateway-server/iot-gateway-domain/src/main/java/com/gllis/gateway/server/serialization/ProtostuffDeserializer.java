package com.gllis.gateway.server.serialization;

import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
/**
 * Protostuff 反序列化
 *
 * @author glli
 * @date 2023/8/16
 */
public class ProtostuffDeserializer<T> implements Deserializer<T> {
    @Override
    public T deserialize(String topic, byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        // 从header中获取类名
        byte[] clazz = headers.lastHeader("clazz").value();
        if (clazz == null) {
            return null;
        }
        String clazzName = new String(clazz);
        if ("".equals(clazzName)) {
            return null;
        }
        if ("[B".equals(clazzName)) {
            return (T) data;
        }
        T obj;
        try {
            Schema<T> schema = (Schema<T>) ProtostuffUtil.getSchema(Class.forName(clazzName));
            obj = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }
}
