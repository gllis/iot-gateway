package com.gllis.gateway.analysis;

import com.gllis.gateway.server.domain.Packet;
import com.gllis.gateway.server.enums.ProtocolEnum;
import com.gllis.gateway.server.exception.ServiceException;
import com.gllis.gateway.server.util.AnnotationParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author glli
 * date 2023/8/17
 */
@Slf4j
@Component
public class DefaultHandlerFactory implements HandlerFactory, ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

    private static final String HANDLER_KEY = "{0}-{1}";

    private static final Map<String, ProtocolProcessing> HANDLER_MAP = new HashMap<>();

    @PostConstruct
    public void initFactory() {
        // 获取所有的处理实现类
        Map<String, ProtocolProcessing> beansMap = applicationContext.getBeansOfType(ProtocolProcessing.class);
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
                HANDLER_MAP.put(MessageFormat.format(HANDLER_KEY, pType.value, cmdType), handler);
            }
        }
    }

    @Override
    public ProtocolProcessing getHandler(Packet packet) {
        return HANDLER_MAP.get(MessageFormat.format(HANDLER_KEY, packet.getProtocolEnum().value, packet.getCmd()));
    }

    @Override
    public void destroy() throws Exception {
        this.applicationContext = null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
