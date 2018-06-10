package com.metarnet.core.common.controller.editor;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InitUtil {

    /**
     * 版本号
     */
    public static final Integer RECORD_VERSION = 1;

	/**
	 * 返回初始化为 2099-12-31 的 archiveBaseDate
	 * @return
	 */
	public static Date getArchiveBaseDate(){
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		Date archiveBaseDate = null;
		try {
			archiveBaseDate = sdf1.parse("2099-12-31");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return archiveBaseDate;
	}
	
	/**
	 * 返回唯一的实体表编号：sessionID + 当前时间（格式：yyyyMMddHHmmss.SSSSS）
	 * @param request
	 * @return
	 */
	public static String getUniqueNo(HttpServletRequest request){
		String sessionID = request.getSession(true).getId();
		SimpleDateFormat sdf2 = new SimpleDateFormat(
				"yyyyMMddHHmmss.SSSSS");
		String strNow = sdf2.format(new Date());
		String uniqueNo = strNow + sessionID;
		return uniqueNo;	
	}
	
	/**
	 * 将当前时间返回成Timestamp
	 * @return
	 */
	public static Timestamp getTimestampNow(){
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str_time = datetimeFormat.format(new Date());
		return Timestamp.valueOf(str_time);	
	}
}
