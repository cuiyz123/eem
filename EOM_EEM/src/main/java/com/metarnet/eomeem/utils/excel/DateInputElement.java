package com.metarnet.eomeem.utils.excel;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ChenBo
 */
public class DateInputElement extends InputElement {
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
	protected static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static final String elementBegin = "<input type=\"text\"";
    public static final String elementEnd = ">";

    public String getElementBegin() {
        return elementBegin;
    }
    public String getElementEnd() {
        return elementEnd;
    }
	public String toString() {
		String dateValue = this.keyValueProps.getProperty("value", null);
		if(dateValue != null) {
			if(StringUtils.equals(dateValue, "nowdate") || StringUtils.equals(dateValue, "date")) {
				Date date = new Date(System.currentTimeMillis());
				this.keyValueProps.setProperty("value", dateFormat.format(date));
			} else {
				if(StringUtils.equals(dateValue, "nowtime") || StringUtils.equals(dateValue, "time")) {
					Date date = new Date(System.currentTimeMillis());
					this.keyValueProps.setProperty("value", timeFormat.format(date));
				} else {
					if(StringUtils.equals(dateValue, "nowdatetime") || StringUtils.equals(dateValue, "datetime")) {
						Date date = new Date(System.currentTimeMillis());
						this.keyValueProps.setProperty("value", datetimeFormat.format(date));
					} 					
				}
			}
		}
		return super.toString();
	}
	public void setDateFormat(SimpleDateFormat df) {
		dateFormat = df;
	}
	public void setDatetimeFormat(SimpleDateFormat dtf) {
		datetimeFormat = dtf;
	}
	public void setTimeFormat(SimpleDateFormat tf) {
		timeFormat = tf;
	}
}
