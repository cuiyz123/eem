package com.metarnet.core.common.utils;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateJsonValueProcessor implements JsonValueProcessor {
    public static final String Default_DATE_PATTERN ="yyyy-MM-dd HH:mm:ss";
    private DateFormat dateFormat ;
    public DateJsonValueProcessor(String datePattern){
        try{
            dateFormat  = new SimpleDateFormat(datePattern);

        }catch(Exception e ){
            dateFormat = new SimpleDateFormat(Default_DATE_PATTERN);

        }

    }
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        return process(value);
    }

    public Object processObjectValue(String key, Object value,
                                     JsonConfig jsonConfig) {
        return process(value);
    }
    private Object process(Object value){
        if(value instanceof Date){
        	return dateFormat.format((Date)value);
        }
        else if(value instanceof Timestamp)
        {
            return dateFormat.format(new Date(((Timestamp)value).getTime()));
        }
        else
        {
            return null;
        }

    }
}