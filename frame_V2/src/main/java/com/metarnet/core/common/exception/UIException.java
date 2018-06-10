package com.metarnet.core.common.exception;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 上午9:57
 */
public class UIException extends BaseException {

    public UIException(String msg) {
        super(msg);
    }

    public UIException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UIException(Throwable cause) {
        super(cause);
    }

}
