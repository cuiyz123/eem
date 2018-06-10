package com.metarnet.core.common.exception;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 上午9:57
 */
public class AdapterException extends BaseException {

    public AdapterException(String msg) {
        super(msg);
    }

    public AdapterException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AdapterException(Throwable cause) {
        super(cause);
    }
}
