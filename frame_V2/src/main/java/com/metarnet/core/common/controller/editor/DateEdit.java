package com.metarnet.core.common.controller.editor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateEdit extends PropertyEditorSupport {
	private SimpleDateFormat datetimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				if (text.indexOf(":") == -1 && text.length() == 10) {
					setValue(this.dateFormat.parse(text));
				} else if (text.indexOf(":") > 0 && text.length() == 19) {
					setValue(this.datetimeFormat.parse(text));
				} else {
					throw new IllegalArgumentException(
							"Could not parseActivity date, date format is error ");
				}
			} catch (ParseException ex) {
				IllegalArgumentException iae = new IllegalArgumentException(
						"Could not parseActivity date: " + ex.getMessage());
				iae.initCause(ex);
				throw iae;
			}
		} else {
			setValue(null);
		}
	}

	public String getAsText() {
		if (getValue() instanceof Date) {
			try {
				Calendar cal = Calendar.getInstance();
				Date dateValue = (Date) getValue();
				cal.setTimeInMillis(dateValue.getTime());
				if ((0 == cal.get(Calendar.HOUR_OF_DAY))
						&& (0 == cal.get(Calendar.MINUTE))
						&& (0 == cal.get(Calendar.SECOND))) {
					return dateFormat.format(dateValue);
				} else {
					return datetimeFormat.format(dateValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
