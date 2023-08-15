package com.gllis.gateway.server.core.listener;

/**
 * 监听
 *
 * @author glli
 * @date 2023/8/15
 */
public interface Listener {
    void onSuccess(Object... args);
    void onFailure(Throwable cause);
}
