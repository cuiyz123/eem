package com.metarnet.core.common.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 上午9:57
 */
public class BaseException extends Exception {
    Logger logger = LogManager.getLogger(this.getClass().getName());
    public BaseException(String msg) {
        super(msg);
        logger.error(msg);
    }

    public BaseException(String msg, Throwable cause) {
        super(msg, cause);
        logger.error(msg);
    }

    public BaseException(Throwable cause) {
        super(cause);
        logger.error(cause.getMessage(),cause);
    }
}
