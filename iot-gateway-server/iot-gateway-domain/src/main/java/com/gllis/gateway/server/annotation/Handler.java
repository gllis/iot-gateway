package com.gllis.gateway.server.annotation;

import com.gllis.gateway.server.enums.ProtocolEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 解包处理注解
 *
 * @author glli
 * @date 2023/8/14
 */
@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    // 协议
    ProtocolEnum protocol();
    // 指令类型
    String[] cmdType();
}
