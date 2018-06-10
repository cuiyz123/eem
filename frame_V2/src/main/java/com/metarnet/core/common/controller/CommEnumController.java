package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSONArray;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 枚举对象
 * 
 * @author
 * 
 */
@Controller
public class CommEnumController extends BaseController {
	private static Logger logger = Logger.getLogger(CommEnumController.class);

	@RequestMapping(value = "/commEnumController.do", params = "method=getEnumByType")
	@ResponseBody
	public void getEnumByType(String enumItemCode, String orgId, String status,
			HttpServletRequest request, HttpServletResponse response)
			throws UIException {
		String j = null;
		List<EnumValue> l = new ArrayList<EnumValue>();
		try {
			if (enumItemCode != null && enumItemCode.length()>0) {
				String[] earray = enumItemCode.split(",");
				for (int i = 0; i < earray.length; i++) {
					l.addAll(EnumConfigAdapter.getInstence().getEnumType(
							earray[i], null, Integer.parseInt(status))
							.getEnumValues());
				}
			}
			j = JSONArray.toJSONString(l);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			endHandle(request, response, j, "获取枚举列表失败 enumItemCode="
					+ enumItemCode, false);
		}
	}

	@RequestMapping(value = "/commEnumController.do", params = "method=getChildEnum")
	@ResponseBody
	public void getChildEnum(String enumItemCode, int enumItemValueId,
			String orgId, String status, HttpServletRequest request,
			HttpServletResponse response) throws UIException {
		String j = null;
		List<EnumValue> l = new ArrayList<EnumValue>();
		try {
			List<EnumType> lent = EnumConfigAdapter.getInstence()
					.getFilterChildEnumType(enumItemCode, enumItemValueId,
							null, 0);
			for (EnumType et : lent) {
				l.addAll(et.getEnumValues());
			}
			j = JSONArray.toJSONString(l);
		} catch (Exception e) {
//			throw new UIException("获取枚举列表失败  enumItemCode=" + enumItemCode
//					+ ";orgId=" + orgId + ";status=" + status, "获取枚举列表失败", "");
		}
		endHandle(request, response, j,
				"获取枚举列表失败 enumItemCode=" + enumItemCode, false);
	}

	/**
	 * 根据枚举值ID获得枚举值对象
	 * 
	 * @param request
	 * @param response
	 * @throws UIException
	 */
	@RequestMapping(value = "/commEnumController.do", params = "method=getEnumValueById")
	@ResponseBody
	public void getEnumValueById(int enumValueId, HttpServletRequest request,
			HttpServletResponse response) throws UIException {
		String j = null;
		try {
			EnumValue ev = EnumConfigAdapter.getInstence().getEnumValueById(
					enumValueId);
			if (ev != null) {
				j = JSONArray.toJSONString(ev);
			}
		} catch (Exception e) {
//			throw new UIException(
//					"根据枚举值ID获得枚举值对象   enumValueId=" + enumValueId, "");
		}
		endHandle(request, response, j, "根据枚举值ID获得枚举值对象  enumValueId="
				+ enumValueId, false);
	}

	/**
	 * 根据上级枚举项编码获得下级枚举类型对象列表
	 * 
	 * @param parentEnumItemCode
	 * @param status
	 * @param orgId
	 * @param request
	 * @param response
	 * @throws UIException
	 */
	@RequestMapping(value = "/commEnumController.do", params = "method=getChildEnumType")
	@ResponseBody
	public void getChildEnumType(String parentEnumItemCode, int status,
			String orgId, HttpServletRequest request,
			HttpServletResponse response) throws UIException {
		String j = null;
		List<EnumValue> l = new ArrayList<EnumValue>();
		try {
			List<EnumType> lent = EnumConfigAdapter.getInstence()
					.getChildEnumType(parentEnumItemCode, null, 1);
			for (EnumType et : lent) {
				l.addAll(et.getEnumValues());
			}
			j = JSONArray.toJSONString(l);
		} catch (Exception e) {
//			throw new UIException("根据上级枚举项编码获得下级枚举类型对象列表   parentEnumItemCode="
//					+ parentEnumItemCode, "");
		}
		endHandle(request, response, j,
				"根据上级枚举项编码获得下级枚举类型对象列表  parentEnumItemCode="
						+ parentEnumItemCode, false);
	}
}
