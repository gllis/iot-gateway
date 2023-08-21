package com.gllis.gateway.log.biz;

import com.gllis.gateway.server.domain.DeviceLog;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 写入日志
 *
 * @author glli
 * @date 2023/8/21
 */
@Slf4j
@Component
public class LogWriter {
    private final static int maxElements = 400000;

    private BlockingDeque<DeviceLog> deviceLogQueue = new LinkedBlockingDeque<>();

    @Autowired
    KafkaListenerEndpointRegistry registry;

    @Autowired
    private InfluxDBClient influxDBClient;

    @Value("${iot.mq.consumer.listenerId}")
    private String listenerId;

    @PostConstruct
    public void init() {
        ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1);
        scheduled.scheduleAtFixedRate(() -> {
            log.info("当前队列中有{}条数据", deviceLogQueue.size());
            List<DeviceLog> logs = new ArrayList<>();
            deviceLogQueue.drainTo(logs, maxElements);
            if (logs.size() > 0) {
                writeLog(logs);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        log.info("退出程序");
        if (registry != null) {
            registry.getListenerContainer(listenerId).stop();
        }

        log.info("当前队列中有{}条数据", deviceLogQueue.size());
        if (deviceLogQueue.size() > 0) {
            List<DeviceLog> logs = new ArrayList<>();
            deviceLogQueue.drainTo(logs);
            if (logs.size() > 0) {
                writeLog(logs);
            }
        }
        log.info("退出时队列中有{}条数据", deviceLogQueue.size());
    }

    public void addQueue(DeviceLog log) {
        deviceLogQueue.add(log);
    }

    /**
     * 批量写入日志
     *
     * @param logs
     */
    @Async("doAsyncExecutor")
    public void writeLog(List<DeviceLog> logs) {
        List<Point> points = new ArrayList<>();
        for (DeviceLog log : logs) {

            Point point = Point.measurement("device_log")
                    .addTag("sn", log.getSn())
                    .addField("type", log.getType())
                    .addField("msg", log.getMsg())
                    .addField("hosts", log.getHosts())
                    .time(log.getCreateTime().getTime(), WritePrecision.MS);

            points.add(point);

        }
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            writeApi.writePoints(points);
        }

    }
}
