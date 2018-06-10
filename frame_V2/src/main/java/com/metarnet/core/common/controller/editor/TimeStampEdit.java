package com.metarnet.core.common.controller.editor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeStampEdit extends PropertyEditorSupport {
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null || text.length()==0) {
			return;
		}
		if (!StringUtils.hasText(text)) {

			setValue(null);
		} else {

			Timestamp timestamp = null;
			try {
				timestamp = new Timestamp(new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").parse(text).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			setValue(timestamp);
		}
	}

	@Override
	public String getAsText() {

		return getValue().toString();
	}
}
