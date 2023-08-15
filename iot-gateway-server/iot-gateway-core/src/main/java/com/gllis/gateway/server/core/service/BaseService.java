package com.gllis.gateway.server.core.service;

import com.gllis.gateway.server.core.listener.Listener;

import java.util.concurrent.CompletableFuture;

/**
 * 基础服务接口
 *
 * @author glli
 * @date 2023/8/15
 */
public interface BaseService {

    void start(Listener listener);

    void stop(Listener listener);

    CompletableFuture<Boolean> start();
    CompletableFuture<Boolean> stop();

    boolean syncStart();
    boolean syncStop();

    void init();
    boolean isRunning();
}
