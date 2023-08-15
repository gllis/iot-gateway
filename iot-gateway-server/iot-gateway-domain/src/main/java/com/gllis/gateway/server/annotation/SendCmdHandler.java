package com.gllis.gateway.server.annotation;

import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.enums.SendCommandTypeEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 下发指令处理注解
 *
 * @author glli
 * @date 2023/8/15
 */
@Component
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SendCmdHandler {
    // 协议
    ProtocolEnum protocol();
    // 指令类型
    SendCommandTypeEnum cmdType();

}
