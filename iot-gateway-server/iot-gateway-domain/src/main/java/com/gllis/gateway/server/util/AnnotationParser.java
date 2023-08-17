package com.gllis.gateway.server.util;

import com.gllis.gateway.server.annotation.Handler;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

/**
 * 解析注解
 *
 * @author GL
 * @created 2023/8/15
 */
@Slf4j
public class AnnotationParser {

    /**
     * 通过注解获取对应协议
     *
     * @param clazz
     * @return
     */
    public static ProtocolEnum parseHandlerProtocolType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        Handler handler = clazz.getAnnotation(Handler.class);
        if (handler == null) {
            String erMsg = MessageFormat.format("没有@Handler注解. 类={0}", clazz.getName());
            log.error(erMsg);
            throw new ServiceException(erMsg);
        }
        return handler.protocol();
    }

    /**
     * 通过注解获取指令类型
     *
     * @param clazz
     * @return
     */
    public static String[] parseHandlerCmdType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        Handler handler = clazz.getAnnotation(Handler.class);
        if (handler == null) {
            String erMsg = MessageFormat.format("没有@Handler注解. 类={0}", clazz.getName());
            log.error(erMsg);
            throw new ServiceException(erMsg);
        }
        return handler.cmdType();
    }
}
