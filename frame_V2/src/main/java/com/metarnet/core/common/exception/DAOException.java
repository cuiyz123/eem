package com.metarnet.core.common.exception;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 上午9:57
 */
public class DAOException extends BaseException {

    public DAOException(String msg) {
        super(msg);
    }

    public DAOException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }

}
