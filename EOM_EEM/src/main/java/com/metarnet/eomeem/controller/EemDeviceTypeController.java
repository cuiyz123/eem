package com.metarnet.eomeem.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.PagerPropertyUtils;
import com.metarnet.eomeem.model.DeviceTypeEntity;
import com.metarnet.eomeem.service.IEemDeviceTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Administrator on 2016/7/25.
 */
@Controller
public class EemDeviceTypeController extends BaseController {
    @Resource
    private IEemDeviceTypeService eemDeviceTypeService;

    @RequestMapping(value = "/eemDeviceTypeController.do", params = "method=queryDeviceTypeList")
    @ResponseBody
    public void queryDeviceTypeList(Long objectId, String param, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            List list = eemDeviceTypeService.queryDeviceTypeList(objectId,param, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(list, ser, SerializerFeature.WriteNullListAsEmpty), "queryVendorList");
        } catch (Exception e) {
            throw new UIException("queryDeviceTypeList", e);
        }
    }

    @RequestMapping(value = "/eemDeviceTypeController.do", params = "method=saveDeviceType")
    @ResponseBody
    public void saveDeviceType(DeviceTypeEntity deviceTypeEntity, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            String result = eemDeviceTypeService.saveDeviceType(deviceTypeEntity, getUserEntity(request));
            if (StringUtils.isBlank(result)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", result);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        endHandle(request, response, jsonObject, "");
    }


    @RequestMapping(value = "/eemDeviceTypeController.do", params = "method=deleteDeviceType")
    @ResponseBody
    public void deleteDeviceType(DeviceTypeEntity deviceTypeEntity, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            String result = eemDeviceTypeService.deleteDeviceType(deviceTypeEntity, getUserEntity(request));
            if (StringUtils.isBlank(result)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", result);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        endHandle(request, response, jsonObject, "");
    }
}
