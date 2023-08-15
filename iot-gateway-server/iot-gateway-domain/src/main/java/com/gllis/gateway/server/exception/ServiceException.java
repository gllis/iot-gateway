package com.gllis.gateway.server.exception;

/**
 * 服务异常
 *
 * @author glli
 * @date 2023/8/15
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
