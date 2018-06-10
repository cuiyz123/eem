package com.metarnet.core.common.utils;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-3-6
 * Time: 下午3:56
 * String类型处理工具类
 */
public class StringUtils {
    /**
     * 字符串连接
     *
     * @param prefix  前缀
     * @param postfix 后缀
     * @return 合并后的字符串
     */
	public StringUtils(){}
    public static String append(String prefix, String postfix) {
        return new StringBuffer(prefix).append(new StringBuffer(postfix)).toString();
    }
}
