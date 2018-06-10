package com.metarnet.core.common.controller.editor;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JsonTimestampToStringUtil implements JsonValueProcessor {

    /**
     * datePattern
     */
    private static String datePattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * JsonDateValueProcessor
     */
    public JsonTimestampToStringUtil() {
        super();
    }

    /**
     * @param format
     */
    public JsonTimestampToStringUtil(String format) {
        super();
        this.datePattern = format;
    }

    /**
     * @param value
     * @param jsonConfig
     * @return Object
     */
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        return process(value);
    }

    /**
     * @param key
     * @param value
     * @param jsonConfig
     * @return Object
     */
    public Object processObjectValue(String key, Object value,
                                     JsonConfig jsonConfig) {
        return process(value);
    }

    /**
     * process
     *
     * @param value
     * @return
     */
    private Object process(Object value) {
        try {
            if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.UK);
                return sdf.format((Date) value);
            } else if (value instanceof Timestamp) {
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.UK);
                Timestamp t = (Timestamp) value;
                return sdf.format(t);
            } else if (value == null) {
                return "";
            }
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * @return the datePattern
     */
    public String getDatePattern() {
        return datePattern;
    }

    /**
     * @param pDatePattern the datePattern to set
     */
    public void setDatePattern(String pDatePattern) {
        datePattern = pDatePattern;
    }

    /**
     * 获取当前时间字符串
     *
     * @return 当前时间字符串
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat(datePattern, Locale.UK).format(new Date());
    }


}