package com.gllis.gateway.server.serialization;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * protostuff 工具类
 *
 * @author glli
 * @date 2023/8/10
 */
public class ProtostuffUtil {
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    public static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            schema = RuntimeSchema.getSchema(clazz);
        }
        return schema;
    }
}