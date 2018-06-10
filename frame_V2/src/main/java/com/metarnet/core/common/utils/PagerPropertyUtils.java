package com.metarnet.core.common.utils;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.model.Sort;
import com.metarnet.core.common.model.dtgrid.Column;
import com.metarnet.core.common.model.dtgrid.Condition;

import java.util.HashMap;
import java.util.Map;

/**
 * Pager属性映射工具类
 * @author 大连首闻科技有限公司
 * @since 2014-10-13 16:35:56
 */
@SuppressWarnings("rawtypes")
public class PagerPropertyUtils {
	
	/**
	 * 将JSON对象映射为Pager对象
	 * @param object 原JSON对象
	 * @throws Exception
	 */
	public static Pager copy(String object) throws Exception{
//		Map<String, Class> classMap = new HashMap<String, Class>();
//		classMap.put("parameters", Map.class);
//		classMap.put("fastQueryParameters", Map.class);
//		classMap.put("advanceQueryConditions", Condition.class);
//		classMap.put("advanceQuerySorts", Sort.class);
//		classMap.put("exhibitDatas", Map.class);
//		classMap.put("exportColumns", Column.class);
//		classMap.put("exportDatas", Map.class);
//		Pager pager = JSON.pa.toBean(object, Pager.class, classMap);
		Pager pager = JSON.parseObject(object,Pager.class);
		return pager;
	}
	
}