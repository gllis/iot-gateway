package com.gllis.gateway.server.serialization;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

/**
 * protostuff 工具类
 *
 * @author glli
 * @date 2023/8/16
 */
public class ProtostuffSerializer<T> implements Serializer<T> {
    @Override
    public byte[] serialize(String topic, T obj) {
        LinkedBuffer buffer = LinkedBuffer.allocate();
        Schema schema = ProtostuffUtil.getSchema(obj.getClass());
        byte[] serializeData = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        buffer.clear();
        return serializeData;
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        if (data == null) {
            return null;
        }
        headers.add(new RecordHeader("clazz", data.getClass().getName().getBytes(StandardCharsets.UTF_8)));
        return serialize(topic, data);
    }
}
