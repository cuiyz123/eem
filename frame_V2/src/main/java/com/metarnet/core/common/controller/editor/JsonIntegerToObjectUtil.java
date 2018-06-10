package com.metarnet.core.common.controller.editor;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * Created with IntelliJ IDEA.
 * Company: Metarnet
 * User: kans
 * Date: 13-4-19
 * Time: 下午4:39
 * Description: 基础数据转换Json对象处理类
 * 对象属性类型为Integer, 若该属性为空, 则转换为Json对象时也应该为空而不是0.
 */
public class JsonIntegerToObjectUtil implements JsonValueProcessor {

    public JsonIntegerToObjectUtil() {
        super();
    }

    @Override
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        return this.process(value);
    }

    @Override
    public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {
        return this.process(value);
    }

    /**
     * 实现功能: Integer数据类型的处理方式
     *
     * @param value 属性值
     * @return 处理后的属性值
     */
    private Object process(Object value) {
        try {
            if (value instanceof Integer) {
                return value;
            } else if (value == null) {
                return "";
            }
            return value == null ? "" : value;
        } catch (Exception e) {
            return null;
        }
    }
}
