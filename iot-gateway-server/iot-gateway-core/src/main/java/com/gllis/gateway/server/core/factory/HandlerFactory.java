package com.gllis.gateway.server.core.factory;


import com.gllis.gateway.server.core.handler.ProtocolProcessing;
import com.gllis.gateway.server.core.util.AnnotationParser;
import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.exception.ServiceException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理工厂
 *
 * @author glli
 * @date 2023/8/15
 */
@Slf4j
@Component
public class HandlerFactory {

    private static final String HANDLER_KEY = "{0}-{1}";

    private static Map<String, ProtocolProcessing> handlerMap = new ConcurrentHashMap<>();

    public void init(ApplicationContext ctx) {
        // 获取所有的处理实现类
        Map<String, ProtocolProcessing> beansMap = ctx.getBeansOfType(ProtocolProcessing.class);
        if (beansMap == null || beansMap.isEmpty()) {
            log.error("找不到处理实现类");
            throw new ServiceException("找不到处理实现类");
        }
        log.info("一共找到 {} 个handler类.", beansMap.size());
        Iterator<Map.Entry<String, ProtocolProcessing>> it = beansMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ProtocolProcessing> en = it.next();
            ProtocolProcessing handler = en.getValue();
            // 从注解中解析所处理的设备类型
            ProtocolEnum pType;
            String[] cmdTypes;
            try {
                pType = AnnotationParser.parseHandlerProtocolType(handler.getClass());
                cmdTypes = AnnotationParser.parseHandlerCmdType(handler.getClass());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }

            if (pType == null) {
                continue;
            }
            if (cmdTypes == null) {
                continue;
            }
            for (String cmdType : cmdTypes) {
                handlerMap.put(MessageFormat.format(HANDLER_KEY, pType.value, cmdType), handler);
            }

        }
    }


    /**
     * 获取对应协议的Handler
     *
     * @param packet
     * @return
     */
    public ProtocolProcessing getHandler(Packet packet) {
        return handlerMap.get(MessageFormat.format(HANDLER_KEY, packet.getProtocolEnum().value, packet.getCmd()));
    }
}
