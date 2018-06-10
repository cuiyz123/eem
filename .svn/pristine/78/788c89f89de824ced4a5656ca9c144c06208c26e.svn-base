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
import com.metarnet.eomeem.model.EemNoticeEntity;
import com.metarnet.eomeem.model.VendorEntity;
import com.metarnet.eomeem.service.IEemNoticeService;
import com.metarnet.eomeem.service.IEemVendorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
@Controller
public class EemVendorController extends BaseController {
    @Resource
    private IEemVendorService eemVendorService;

    @RequestMapping(value = "/eemVendorController.do", params = "method=queryVendorList")
    @ResponseBody
    public void queryVendorList(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemVendorService.queryVendorList(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryVendorList");
        } catch (Exception e) {
            throw new UIException("queryVendorList", e);
        }
    }

    @RequestMapping(value = "/eemVendorController.do", params = "method=saveVendor")
    @ResponseBody
    public void saveVendor(VendorEntity vendorEntity, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            String result = eemVendorService.saveVendor(vendorEntity, getUserEntity(request));
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

    @RequestMapping(value = "/eemVendorController.do", params = "method=showVendor")
    @ResponseBody
    public void showVendor(Long objectID, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            VendorEntity vendorEntity = eemVendorService.showVendor(objectID, getUserEntity(request));
            jsonObject.put("success", true);
            jsonObject.put("eemNoticeEntity", vendorEntity);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        SerializeConfig ser = new SerializeConfig();
        ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        endHandle(request, response, JSON.toJSONString(jsonObject, ser, SerializerFeature.WriteNullListAsEmpty), objectID.toString());
    }

    @RequestMapping(value = "/eemVendorController.do", params = "method=deleteVendor")
    @ResponseBody
    public void deleteVendor(String objectIDs, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            eemVendorService.deleteVendor(objectIDs, getUserEntity(request));
            jsonObject.put("success", true);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        endHandle(request, response, jsonObject, objectIDs);
    }
    @RequestMapping(value = "/eemVendorController.do", params = "method=getVendorCode")
    @ResponseBody
    public void getVendorCode(HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            String vendorCode = eemVendorService.getVendorCode(getUserEntity(request));
            jsonObject.put("success", true);
            jsonObject.put("vendorCode", vendorCode);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        endHandle(request, response, jsonObject, "");
    }
    @RequestMapping(value = "/eemVendorController.do", params = "method=uploadFile")
    @ResponseBody
    public void saveUploadFiles(HttpServletResponse response, HttpServletRequest request) throws UIException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            String result = eemVendorService.saveUploadFiles(multipartRequest, getUserEntity(request));
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isBlank(result)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", result);
            }
            endHandle(request, response, jsonObject.toJSONString(), "saveUploadFiles");
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(params = "method=downVendorTemp")
    public ModelAndView downVendorTemp(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        String storeName="vendorImport.xls";
        String contentType = "application/octet-stream";
        download(request, response, storeName, contentType,"厂商导入模板.xls");
        return null;
    }

    //文件下载 主要方法
    protected  void download(HttpServletRequest request,HttpServletResponse response,
                             String storeName, String contentType,String fileName) throws Exception {

        request.setCharacterEncoding("UTF-8");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        //获取项目根目录
        String ctxPath = request.getSession().getServletContext()
                .getRealPath("");

        //获取下载文件露肩
        String downLoadPath = ctxPath+"/"+ storeName;

        //获取文件的长度
        long fileLength = new File(downLoadPath).length();

        String userAgent = request.getHeader("User-Agent");
        //针对IE或者以IE为内核的浏览器：
        if (userAgent.contains("MSIE")||userAgent.contains("Trident")) {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } else {
            //非IE浏览器的处理：
            fileName = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
        }

        //设置文件输出类型
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename="
                + fileName);

        //设置输出长度
        response.setHeader("Content-Length", String.valueOf(fileLength));
        //获取输入流
        bis = new BufferedInputStream(new FileInputStream(downLoadPath));
        //输出流
        bos = new BufferedOutputStream(response.getOutputStream());
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }
        //关闭流
        bis.close();
        bos.close();
    }
}
