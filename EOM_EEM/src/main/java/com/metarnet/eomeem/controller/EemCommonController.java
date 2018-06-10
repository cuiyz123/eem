package com.metarnet.eomeem.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.metarnet.core.common.utils.PagerPropertyUtils;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.service.IEemCommonService;
import com.metarnet.eomeem.vo.Tempt;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Administrator on 2016/6/2.
 * 主要实现一些数据的查询操作
 */
@Controller
public class EemCommonController extends BaseController {
    @Resource
    private IEemCommonService eemCommonService;
    @Resource
    private IBaseDAO baseDAO;
    private static final int OUTPUT_SIZE = 4096;
    private static final String CHARACTER_GB2312 = "gb2312";

    private static final String CHARACTER_ISO8859 = "ISO8859-1";

    /**
     * 待办页面初始化
     *
     * @param request
     * @param response
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=todo")
    @ResponseBody
    public ModelAndView todo(HttpServletRequest request, HttpServletResponse response,String type,String nodeID) throws UIException {
        try {
            UserEntity userEntity = getUserEntity(request);
            String tempIds = eemCommonService.findTempIdsByNodeId(userEntity.getUserName(),nodeID);
            request.setAttribute("tempIds", tempIds);
            request.setAttribute("tempList", eemCommonService.findTempList("report", userEntity,tempIds));
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH)+1;
            int year = cal.get(Calendar.YEAR);
            if(month>=1 && month<=6){
                request.setAttribute("yearStr", String.valueOf(year-1));
            }else{
                request.setAttribute("yearStr", String.valueOf(year));
            }

            //  jw3.7省份查询框
            OrgEntity orgEntity = AAAAAdapter.getCompany(getUserEntity(request).getOrgID().intValue());
            request.setAttribute("proType", orgEntity.getOrgName());  //jw

          /*  OrgEntity company = null;

            company = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            String sql="select result from t_eem_report_apply where reportOrgCode='" + company.getOrgCode() + "'";
            List<Map> messageList = baseDAO.findNativeSQL(sql.toString(), null);
            for(Map result : messageList){
                Iterator it = result.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry entry = (Map.Entry) it.next();
                    if(entry.getValue().equals("同意")){
                        entry.setValue("1");
                        request.setAttribute("entry",entry);
                       System.out.println(entry+"---------------------3");
                    }
                }
            }*/



    } catch (Exception e) {
            e.printStackTrace();
      /*  } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();*/
        }
        if(type!=null&&type.equals("2")){
            return new ModelAndView(new InternalResourceView("/base/page/aReportApply.jsp"));
        }else {
            return new ModelAndView(new InternalResourceView("/base/page/todo.jsp"));
        }

    }


    /**
     * 已办页面初始化
     *
     * @param request
     * @param response
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=already")
    @ResponseBody
    public ModelAndView already(HttpServletRequest request, HttpServletResponse response,String nodeID) throws UIException {
        try {
            UserEntity userEntity = getUserEntity(request);
            String tempIds = eemCommonService.findTempIdsByNodeId(userEntity.getUserName(),nodeID);
            request.setAttribute("tempIds", tempIds);
            request.setAttribute("tempList", eemCommonService.findTempList("report", getUserEntity(request), tempIds));
//            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH)+1;
            int year = cal.get(Calendar.YEAR);
            if(month>=1 && month<=6){
                request.setAttribute("yearStr", String.valueOf(year-1));
            }else{
                request.setAttribute("yearStr", String.valueOf(year));
            }
            OrgEntity orgEntity = AAAAAdapter.getCompany(getUserEntity(request).getOrgID().intValue());
            request.setAttribute("proType", orgEntity.getOrgName());  //jw   省份查询框
        } catch (Exception e) {
       // } catch (ServiceException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/already.jsp"));
    }

    /**
     * 数据查询初始化
     *
     * @param request
     * @param response
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=initQueryData")
    @ResponseBody
    public ModelAndView initQueryData(HttpServletRequest request, HttpServletResponse response,String nodeID) throws UIException {
        try {
            UserEntity userEntity = getUserEntity(request);
            String tempIds = eemCommonService.findTempIdsByNodeId(userEntity.getUserName(),nodeID);
            request.setAttribute("tempIds", tempIds);
            request.setAttribute("tempList", eemCommonService.findTempList("report", userEntity, tempIds));
//            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH)+1;
            int year = cal.get(Calendar.YEAR);
            if(month>=1 && month<=6){
                request.setAttribute("yearStr", String.valueOf(year-1));
            }else{
                request.setAttribute("yearStr", String.valueOf(year));
            }
            OrgEntity orgEntity = AAAAAdapter.getCompany(getUserEntity(request).getOrgID().intValue());
            request.setAttribute("proType", orgEntity.getOrgName());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/dataQuery.jsp"));
    }

    /**
     * 公布版数据下载初始化
     *
     * @param request
     * @param response
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=initQueryDataAll")
    @ResponseBody
    public ModelAndView initQueryDataAll(HttpServletRequest request, HttpServletResponse response,String nodeID) throws UIException {
        try {
            UserEntity us  = getUserEntity(request);
            String tempIds = "0";
            request.setAttribute("tempList", eemCommonService.findTempList("report",us,tempIds));
            request.setAttribute("tempIds", tempIds);
            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            OrgEntity orgEntity = AAAAAdapter.getCompany(getUserEntity(request).getOrgID().intValue());
            request.setAttribute("proType", orgEntity.getOrgName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/dataQueryAll.jsp"));
    }

    /**
     * 数据查询,已审核  待审核
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=queryDataList")
    @ResponseBody
    public void queryDataList(HttpServletResponse response, HttpServletRequest request, String type,String tempIds,
                              String tempId,String yearStr,String reportDate) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            UserEntity user =getUserEntity(request);
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH)+1;
            int yearCon = 0;
            if(month>=1 && month<=6){
                yearCon = cal.get(Calendar.YEAR)-1;
            }else{
                yearCon = cal.get(Calendar.YEAR);
            }
            String reportDateCon = null;
            if(!pager.getParameters().isEmpty()){
                yearCon = Integer.parseInt(pager.getParameters().get("year").toString());
                reportDateCon = pager.getParameters().get("reportDate").toString();
            }

            long sign = 2L;
            if(user.getCategory().equals("UNI")){
                sign = 1L;
            }
            if (type.equals("already")) {
                pager = eemCommonService.queryDataList2(pager, type, getUserEntity(request),sign,yearCon,reportDateCon);
            } else {
                pager = eemCommonService.queryDataList(pager, type, getUserEntity(request),sign,tempIds,yearCon);
            }
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }


    @RequestMapping(value = "/eemCommonController.do", params = "method=initSumData")
    @ResponseBody
    public ModelAndView initSumData(HttpServletRequest request, HttpServletResponse response,String nodeID) throws UIException {
        try {
            request.setAttribute("tempList", eemCommonService.findTempList("sum", getUserEntity(request),"all"));
            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/dataSum.jsp"));
    }

    /**
     * 汇总数据查询
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=sumDataList")
    @ResponseBody
    public void sumDataList(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.sumDataList(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "sumDataList");
        } catch (Exception e) {
            throw new UIException("sumDataList", e);
        }
    }

    /**
     * 统计分析
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=countAnalysis")
    @ResponseBody
    public void countAnalysis(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.countAnalysis(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "countAnalysis");
        } catch (Exception e) {
            throw new UIException("countAnalysis", e);
        }
    }

    /**
     * 预约汇总
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=OrderReportDate")
    @ResponseBody
    public void OrderReportDate(HttpServletResponse response, HttpServletRequest request, String reportData) throws UIException {
        boolean flag = true;
        try {
            reportData = URLDecoder.decode(reportData,"UTF-8");
            System.out.println(">>>>>>>>>>>>>>>>>>>" + reportData);
            eemCommonService.saveOrderReportDate(reportData, getUserEntity(request), true);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        if (flag) {
            jsonObject.put("success", flag);
            jsonObject.put("msg", "预约汇总成功！");
        } else {
            jsonObject.put("false", flag);
            jsonObject.put("msg", "预约汇总失败！");
        }
        endHandle(request, response, jsonObject, "预约汇总");

    }

    /**
     * 预约汇总
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=cancelOrderReport")
    @ResponseBody
    public void cancelOrderReport(HttpServletResponse response, HttpServletRequest request, String reportData) throws UIException {
        boolean flag = true;
        try {
            reportData = URLDecoder.decode(reportData,"UTF-8");

            eemCommonService.saveOrderReportDate(reportData, getUserEntity(request), false);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        if (flag) {
            jsonObject.put("success", flag);
            jsonObject.put("msg", "取消预约汇总成功！");
        } else {
            jsonObject.put("false", flag);
            jsonObject.put("msg", "取消预约汇总失败！");
        }
        endHandle(request, response, jsonObject, "取消预约汇总");
    }

    /**
     * 数据查询
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=queryReportData")
    @ResponseBody
    public void queryReportData(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            UserEntity user = getUserEntity(request);
            long sign = 2L;
            if(user.getCategory().equals("UNI")){
                sign = 1L;
            }
//            pager = eemCommonService.queryDataList(pager, null, getUserEntity(request),sign);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryReportData", e);
        }
    }

    /**
     * 数据查询
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=queryReportExcel")
    @ResponseBody
    public void queryReportExcel(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.queryReportExcel(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryReportData", e);
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=downCollectExcel")
    @ResponseBody
    public void downCollectExcel(HttpServletRequest request, HttpServletResponse response, Long eid) {
        try {
            EvaluationCollectExcel ere = eemCommonService
                    .findEvaluationCollectExcelById(eid);
            TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
            attachmentRelProc.setAttachmentId(ere.getAttachmentId());
            attachmentRelProc.setAttachmentName(ere.getFileName());
            List<TEomAttachmentRelProc> dataList = new ArrayList<TEomAttachmentRelProc>();
            dataList.add(attachmentRelProc);
            downloadFiles(dataList, response, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=downReportExcel")
    @ResponseBody
    public void batchDown(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/x-msdownload");

        String fileIds = request.getParameter("eid");
        String param = request.getParameter("param");

        List<TEomAttachmentRelProc> dataList = new ArrayList<TEomAttachmentRelProc>();
        if (param == null || param.equals("") || param.equals("null")) {
            String[] fileIdArray = null;
            if (StringUtils.isNotBlank(fileIds)) {
                fileIdArray = fileIds.split(",");
            }
            Set<String> stringSet = new HashSet<String>();
            for (String string : fileIdArray) {
                stringSet.add(string);
            }
            for (String string : stringSet) {
                if (string != null && !"".equals(string)) {
                    EvaluationCollectExcel ere = eemCommonService
                            .findEvaluationCollectExcelById(Long.valueOf(string));
                    TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
                    attachmentRelProc.setAttachmentId(ere.getAttachmentId());
                    attachmentRelProc.setAttachmentName(ere.getFileName());
                    dataList.add(attachmentRelProc);
                }
            }
        } else {
            String[] fileIdArray = null;
            if (StringUtils.isNotBlank(fileIds)) {
                fileIdArray = fileIds.split(",");
            }
            Set<String> stringSet = new HashSet<String>();
            for (String string : fileIdArray) {
                stringSet.add(string);
            }
            for (String string : stringSet) {
                if (string != null && !"".equals(string)) {
                    EvaluationReportExcel ere = eemCommonService
                            .findEvaluationReportExcelByEid(string);
                    TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
                    attachmentRelProc.setAttachmentId(ere.getAttachmentId());
                    attachmentRelProc.setAttachmentName(ere.getFileName());
                    dataList.add(attachmentRelProc);
                }
            }
        }
        downloadFiles(dataList, response, 2);
    }


    @RequestMapping(value = "/eemCommonController.do", params = "method=initOrder")
    @ResponseBody
    public ModelAndView initTempPage(HttpServletRequest request, HttpServletResponse response) throws UIException {
        boolean isButton = true;
        UserEntity user = getUserEntity(request);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String one = "";
        String two = "";
        String three = "";
        if (month == 1 || month == 2 || month == 3) {//上年第四季度
            one = (year - 1) + "第四季度";
            two = (year - 1) + "第三季度";
            three = (year - 1) + "第二季度";
        }
        if (month == 4 || month == 5 || month == 6) {//当年第一季度
            one = year + "第一季度";
            two = (year - 1) + "第四季度";
            three = (year - 1) + "第三季度";
        }
        if (month == 7 || month == 8 || month == 9) {//当年第二季度
            one = year + "第二季度";
            two = year + "第一季度";
            three = (year - 1) + "第四季度";
        }
        if (month == 10 || month == 11 || month == 12) {//当年第三季度
            one = year + "第三季度";
            two = year + "第二季度";
            three = year + "第一季度";
        }
        request.setAttribute("one", "当前周期");
        request.setAttribute("two", two);
        request.setAttribute("three", three);
        try {
            isButton = eemCommonService.isButton("当前周期", user);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        request.setAttribute("isOrder", isButton);
        return new ModelAndView(new InternalResourceView("/base/page/appointmentSum.jsp"));
    }


    /**
     * buton
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=isButton")
    @ResponseBody
    public void isButton(HttpServletResponse response, HttpServletRequest request, String order) throws UIException {
        boolean flag = true;
        boolean isButton = true;
        try {
            UserEntity user = getUserEntity(request);
            order=URLDecoder.decode(order,"UTF-8");
            System.out.println(">>>>>>>>>>>>>>>>>>>" + order);
            isButton = eemCommonService.isButton(order, user);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        if (flag) {
            jsonObject.put("success", flag);
            jsonObject.put("msg", isButton);
        } else {
            jsonObject.put("false", flag);
            jsonObject.put("msg", "请求失败！");
        }
        endHandle(request, response, jsonObject, "isButton");
    }

    /**
     * 上报结果查看
     *
     * @param year
     * @param dateStr
     * @param response
     * @param request
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=queryReportResult")
    @ResponseBody
    public ModelAndView queryReportResult(String year, String dateStr, String backType, HttpServletResponse response, HttpServletRequest request) throws UIException {
        UserEntity userEntity = getUserEntity(request);
//        String endYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH)+1;
        int yearNow = cal.get(Calendar.YEAR);
        String endYear = null;
        if(month>=1 && month<=6){
            endYear = String.valueOf(yearNow-1);
        }else{
            endYear = String.valueOf(yearNow);
        }
        if (dateStr == null) {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String dateStrMon = df.format(date);
            String monthStr = dateStrMon.substring(5, 7);
            String yearStr = dateStrMon.substring(0, 4);
            if (monthStr.startsWith("0")) {
                monthStr = monthStr.replace("0", "").trim();
            }
            int monthInt = Integer.parseInt(monthStr);
            //假设当前日期是2018年1-6月，那么页面应该显示当前上报日期为2017年下半年
            if (monthInt <= 6) {
                dateStr = "下半年";
                //dateStr = "上半年";
                year = String.valueOf((Integer.parseInt(yearStr)));
            } else {
                dateStr = "上半年";
            } /*else if (monthInt >= 7 && monthInt < 10) {
                dateStr = "第二季度";
            } else {
                dateStr = "第三季度";
            }*/
        }else {
            try {
                dateStr = URLDecoder.decode(dateStr,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                dateStr="下半年";
                e.printStackTrace();
            }
        }
        String yearStr = "";
//        if (!"".equals(year) && year != null) {
//            yearStr = year;
//        } else {
//            Calendar cal = Calendar.getInstance();
            yearStr = endYear;
//        }
        //时间下拉框
        List<String> timeList = new ArrayList<String>();
//        timeList.add("第一季度");
//        timeList.add("第二季度");
//        timeList.add("第三季度");
//        timeList.add("第四季度");
        timeList.add("上半年");
        timeList.add("下半年");
        //部门 --start
        List<OrgEntity> groupList = new ArrayList<OrgEntity>();
        List<ExcelPage> pageList = new ArrayList<ExcelPage>();
        try {
            if (userEntity.getCategory().equals("UNI") || "root".equals(userEntity.getUserName())) {
                List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
                for (OrgEntity orgEntity : orgEntityList) {
                    if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                        continue;
                   /* } else {*/    //jw  去除所有子公司
                    }	else if( orgEntity.getOrgCode().startsWith("2"))  {
                        groupList.add(orgEntity);
                    }
                }
                pageList = eemCommonService.findPageList(dateStr, yearStr, null, userEntity);
            } else if (userEntity.getCategory().equals("PRO")) {
                OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
                List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(orgEntity.getOrgId());
                for (OrgEntity org : orgEntityList) {
                    if (org.getShortName()!=null&&org.getShortName().equals("BB")) {
                        continue;
                    } else {
                        groupList.add(org);
                    }
                }
                pageList = eemCommonService.findPageList(dateStr, yearStr, orgEntity, userEntity);
            } else if (userEntity.getCategory().equals("CITY")) {
                OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(AAAAAdapter.getCompany(userEntity.getOrgID().intValue()).getOrgId());
                groupList.add(orgEntity);
                pageList = eemCommonService.findPageList(dateStr, yearStr, orgEntity, userEntity);
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        //部门 --end
        request.setAttribute("timeList", timeList);
        request.setAttribute("userType", userEntity.getCategory());
        request.setAttribute("yearStr", yearStr);// 查询年份
        request.setAttribute("endYear", endYear);// 查询年份
       /* Calendar cal = Calendar.getInstance();// 显示的月份信息
        request.setAttribute("yearStr",String.valueOf(cal.get(Calendar.YEAR)));// 显示的月份信息
*/
        request.setAttribute("dateStr", dateStr);// 显示的月份信息


        request.setAttribute("pageList", pageList);
        request.setAttribute("groupList", groupList);
        request.setAttribute("backType", backType);
        List<EemTempEntity> excelTempletList = new ArrayList<EemTempEntity>();
        if (dateStr.equals("上半年") || dateStr.equals("下半年")) {
            excelTempletList = eemCommonService.getAllUseExcelTempletList(2);
        } else {
            excelTempletList = eemCommonService.getAllUseExcelTempletList(1);
        }

        request.setAttribute("excelTempletList", excelTempletList);
        return new ModelAndView(new InternalResourceView("/base/page/reportResult.jsp"));
    }

    /**
     * 审核
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=saveAudit")
    @ResponseBody
    public void saveAudit(HttpServletResponse response, HttpServletRequest request,GeneralInfoModel generalInfoModel, String staus,String operdesc) throws UIException {
        String msg = "";
        try {
            String fileIds = request.getParameter("eid");
            if(fileIds!=null){
                if(staus.equals("Y")){
                    staus = "Y";
                }else {
                    staus="N";
                }

                for(String string : fileIds.split(",")){
                    if(string !=null && !"".equals(string)){
                        GeneralInfoModel gim = new GeneralInfoModel();
                        gim.setProcessingObjectID(Long.valueOf(string));
                        gim.setProcessingStatus(staus);
                        gim.setOperDesc(operdesc);
                        msg = eemCommonService.saveAudit(gim, getUserEntity(request));
                    }
                }
            }else {
                msg = eemCommonService.saveAudit(generalInfoModel, getUserEntity(request));
            }

        } catch (Exception e) {
            throw new UIException("saveAudit", e);
        } finally {
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isBlank(msg)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", msg);
            }
            endHandle(request, response, jsonObject, "");
        }
    }

    /**
     * 退回信息
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=queryCancelList")
    @ResponseBody
    public void queryCancelList(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.queryCancelList(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryCancelList");
        } catch (Exception e) {
            throw new UIException("queryCancelList", e);
        }
    }

    @RequestMapping(value ="/eemCommonController.do",params = "method=queryDeductList")
    @ResponseBody
    public void queryDeductList(HttpServletRequest request,HttpServletResponse response) throws UIException{
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.queryDeductList(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryCancelList");
        } catch (Exception e) {
            throw new UIException("queryCancelList", e);
        }
    }

    /**
     * 整体打包下载验证
     * 如果有数据才可打包下载
     *
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=checkDownReportExcel")
    @ResponseBody
    public void checkDownReportExcel(HttpServletResponse response, HttpServletRequest request, String reportData) throws UIException {
        try {
            List<EvaluationReportExcel> reportExcelList = new ArrayList<EvaluationReportExcel>();
//            if (reportData.equals("currentYear")) {//当前年度
//                reportExcelList = eemCommonService.findEvaluationReportExcelByYearAndDate(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), "");
//            } else if (reportData.equals("allReportYear")) {//近四个季度
//                reportExcelList = eemCommonService.findEvaluationReportExcelByNew4Q();
//            } else {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            String  reportDateStr="下半年";
            String param = URLDecoder.decode(reportData,"UTF-8");
            if(month<7){
                reportDateStr="下半年";
                year =year-1;
            }else {
                reportDateStr="上半年";
            }
            if (param.equals("上一周期")) {
                if("上半年".equals(reportDateStr)){
                    reportDateStr="下半年";
                    year =year-1;
                }else {
                    reportDateStr="上半年";
                }
            }

            if("年度汇总".equals(param)){
                reportDateStr = "全年";
            }
            if("前推第二周期".equals(param)){
                year =year-1;
            }
//                String reportYear = reportData.substring(0, 4);
//                String reportDate = reportData.substring(4, reportData.length());
                reportExcelList = eemCommonService.findEvaluationReportExcelByYearAndDate(String.valueOf(year), reportDateStr);
           // }
            JSONObject jsonObject = new JSONObject();
            if (reportExcelList.size() > 0) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("false", false);
                jsonObject.put("msg", "无可打包数据，请先预约汇总后再下载！");
            }
            endHandle(request, response, jsonObject, "checkDownReportExcel");
        } catch (Exception e) {
            throw new UIException("queryCancelList", e);
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=downAllCollectExcel")
    @ResponseBody
    public void downAllCollectExcel(HttpServletResponse response, HttpServletRequest request, String reportData) throws UIException {
        String dataString = new Date().getTime() + "";
        try {
            DownloadFileInfo[] downloadFileInfos = new DownloadFileInfo[500];
            List<EvaluationReportExcel> reportExcelList = new ArrayList<EvaluationReportExcel>();
//            File files = new File(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString);
//            if (!files.exists()) {
//                files.mkdirs();
//            }
//            if (reportData.equals("currentYear")) {//当前年度
//                reportExcelList = eemCommonService.findEvaluationReportExcelByYearAndDate(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), "");
//            } else if (reportData.equals("allReportYear")) {//近四个季度
//                reportExcelList = eemCommonService.findEvaluationReportExcelByNew4Q();
//            } else {
//                String reportYear = reportData.substring(0, 4);
//                String reportDate = reportData.substring(4, reportData.length());
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            String  reportDateStr="下半年";
            String param = URLDecoder.decode(reportData,"UTF-8");
            if(month<7){
                reportDateStr="下半年";
                year =year-1;
            }else {
                reportDateStr="上半年";
            }
            if (param.equals("上一周期")) {
                if("上半年".equals(reportDateStr)){
                    reportDateStr="下半年";
                    year =year-1;
                }else {
                    reportDateStr="上半年";
                }
            }

            if("年度汇总".equals(param)){
                reportDateStr = "全年";
            }
            if("前推第二周期".equals(param)){
                year =year-1;
            }
                reportExcelList = eemCommonService.findEvaluationReportExcelByYearAndDate(String.valueOf(year), reportDateStr);
//            }
            FileAdapter fileAdapter = FileAdapter.getInstance();
            int index=0;
            for (EvaluationReportExcel ere : reportExcelList) {
               /* File f = new File(ere.getExcelPath() + File.separator
                        + ere.getFileName());*/
                DownloadFileInfo downloadFileInfo = fileAdapter.download(ere.getAttachmentId());
                downloadFileInfo.setFileName(ere.getFileName());
                downloadFileInfos[index++]=downloadFileInfo;
//                ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
//                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream1);
////                FileOutputStream fos = new FileOutputStream(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString + "/" + ere.getFileName() + ".zip");
//
//                String downloadFileName = ere.getFileName();
//                zipOutputStream.putNextEntry(new ZipEntry(downloadFileName));
//                int len;
//                // 每次写出4M
//                byte[] b = new byte[OUTPUT_SIZE];
//
//                InputStream inputStream = new ByteArrayInputStream(downloadFileInfo.getByteArrayOutputStream().toByteArray());
//                while ((len = inputStream.read(b)) != -1) {
//                    zipOutputStream.write(b, 0, len);
//                }
//                zipOutputStream.closeEntry();
//                inputStream.close();
//
//                zipOutputStream.setEncoding("GBK");
//                byte[] ba = byteArrayOutputStream1.toByteArray();
//                if (ba != null) {
//                    fos.write(ba);
//                }
//                zipOutputStream.flush();
//                zipOutputStream.close();
//                byteArrayOutputStream1.flush();
//                byteArrayOutputStream1.close();
//                fos.flush();
//                fos.close();
                /*if (f.exists()) {
                    FileUtils.fileToZip(downloadFileInfo.getByteArrayOutputStream(), EemConstants.GATHER_DATA_PATH + "/temp/" + dataString, ere.getFileName(),ere.getFileName());
                }*/
            }
//            File[] fileArr = files.listFiles();
            String fileName = "打包下载.zip";
            //在服务器端创建打包下载的临时文件
            fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            ServletOutputStream servletOutputStream = response.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream toClient = new ZipOutputStream(byteArrayOutputStream);
            Properties pro=System.getProperties();
            String osName=pro.getProperty("os.name");
                for (int i = 0; i < downloadFileInfos.length && downloadFileInfos[i] != null; i++) {
                    String downloadFileName = downloadFileInfos[i].getFileName(); // new
//                    downloadFileName = downloadFileName.substring(0,downloadFileName.lastIndexOf("总")+1) + "表.xls";
                    downloadFileName = downloadFileName.substring(0, downloadFileName.lastIndexOf(".")).replaceAll("\\.", "_")
                            +downloadFileName.substring(downloadFileName.lastIndexOf(".")).toString();
                    // String(downloadFileInfos[i].getFileName().getBytes(CHARACTER_GB2312),
                    // "UTF-8");
                    // 更改文件名，避免同名文件在解压缩时被覆盖
                    downloadFileName = (i + 1) + "_" + downloadFileName;
//                    logger.debug(fileName + "(" + i + ") ==> " + downloadFileName);
                    ZipEntry zipEntry = new ZipEntry(downloadFileName);
                    if("Linux".equals(osName)||"linux".equals(osName)){
                        zipEntry.setUnixMode(644);
                    }
                    toClient.putNextEntry(zipEntry);
                    int len;
                    // 每次写出4M
                    byte[] b = new byte[4096];
//                    InputStream inputStream = downloadFileInfos[i].getInput();
                    InputStream inputStream = new ByteArrayInputStream(downloadFileInfos[i].getByteArrayOutputStream().toByteArray());
                    while ((len = inputStream.read(b)) != -1) {
                        toClient.write(b, 0, len);

                    }
                    toClient.closeEntry();
                }
            if("Linux".equals(osName)||"linux".equals(osName)){
                toClient.setEncoding("utf-8");
            }else{
                toClient.setEncoding("GBK");
            }

            toClient.flush();
            toClient.close();
            byte[] ba = byteArrayOutputStream.toByteArray();
            if (ba != null) {
                servletOutputStream.write(ba);
            }
            servletOutputStream.flush();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            servletOutputStream.close();
//            FileUtils.deleteDir(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=initTimelyRate")
    @ResponseBody
    public ModelAndView initTimelyRate(String provinceName, String provinceCode, String reportDate, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            reportDate = URLDecoder.decode(reportDate,"UTF-8");
            request.setAttribute("provinceCode", provinceCode);
            request.setAttribute("provinceName", URLDecoder.decode(provinceName,"UTF-8"));
            request.setAttribute("reportDate", reportDate);
            int year=Calendar.getInstance().get(Calendar.YEAR);
            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
           /* if(reportDate.contains(String.valueOf(year+1))){
                request.setAttribute("yearStr", String.valueOf(year+1));
            }else if(reportDate.contains(String.valueOf(year-1))){
                request.setAttribute("yearStr", String.valueOf(year-1));
            }else{
                request.setAttribute("yearStr", String.valueOf(year));
            }*/  //jw
            if((year+"-上半年").equals(reportDate)){
                request.setAttribute("sel", 1);
            }else if((year+"-下半年").equals(reportDate)){
                request.setAttribute("sel", 2);
            } else if ((year-1+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 3);
            } else if ((year-1+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 4);
            } else if ((year-2+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 5);
            } else if ((year-2+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 6);//2019下半年
            } else {
                request.setAttribute("sel", 7);
            }//jw
            /*if((year-1+"-上半年").equals(reportDate)){
                request.setAttribute("sel", 1);//2017上半年
            }else if((year-1+"-下半年").equals(reportDate)){
                request.setAttribute("sel", 2);//2017下半年
            } else if ((year+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 3);//2018上半年
            } else if ((year+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 4);//2018下半年
            } else if ((year+1+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 5);//2019上半年
            } else if ((year+1+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 6);//2019下半年
            } else {
                request.setAttribute("sel", 7);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/timelyRateInfo.jsp"));
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=showTimelyRate")
    @ResponseBody
    public void showTimelyRate(String provinceCode,String reportDate,  HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            Map<String, Object> params = pager.getParameters();
            reportDate = URLDecoder.decode(reportDate,"UTF-8");
//            int year = Calendar.getInstance().get(Calendar.YEAR);
//            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
//            String  reportDateStr="下半年";
//            if(month<7){
//                reportDateStr="下半年";
//                year =year-1;
//            }else {
//                reportDateStr="上半年";
//            }
//            reportDate = year + "-" + reportDateStr;
            if(params.size() > 0){
                reportDate = params.get("reportDate").toString();
            }
            pager = eemCommonService.findTimelyRateInfo(pager, provinceCode, reportDate);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "showTimelyRate");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=initAccuracyRate")
    @ResponseBody
    public ModelAndView initAccuracyRate(String provinceName, String provinceCode, String reportDate, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            reportDate = URLDecoder.decode(reportDate,"UTF-8");   //jw
            request.setAttribute("provinceCode", provinceCode);
            request.setAttribute("provinceName", URLDecoder.decode(provinceName,"UTF-8"));
            request.setAttribute("reportDate", reportDate);
            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            int year=Calendar.getInstance().get(Calendar.YEAR);
            if((year+"-上半年").equals(reportDate)){
                request.setAttribute("sel", 1);//2017上半年
            }else if((year+"-下半年").equals(reportDate)){
                request.setAttribute("sel", 2);//2017下半年
            } else if ((year-1+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 3);//2018上半年
            } else if ((year-1+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 4);//2018下半年
            } else if ((year-2+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 5);//2019上半年
            } else if ((year-2+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 6);//2019下半年
            } else {
                request.setAttribute("sel", 7);
            }//jw
           /* if ((year+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 1);
            } else if ((year+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 2);
            } else if ((year+1+"-上半年").equals(reportDate)) {
                request.setAttribute("sel", 3);
            } else if ((year+1+"-下半年").equals(reportDate)) {
                request.setAttribute("sel", 4);
            } else {
                request.setAttribute("sel", 5);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/accuracyRateInfo.jsp"));
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=showAccuracyRate")
    @ResponseBody
    public void showAccuracyRate(String provinceCode, String reportDate, HttpServletRequest request, HttpServletResponse response,String deptNames) throws UIException {
        try {
            request.setAttribute("reportDate", URLDecoder.decode(reportDate,"UTF-8"));

            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.findAccuracyRateInfo(pager, provinceCode, reportDate);

            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "showAccuracyRate");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=findExcelPages")
    @ResponseBody
    public void findExcelPages(Long tempID, String deptCode, String reportYear, String reportDate, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            List<ExcelPage> excelPageList = eemCommonService.findExcelPages(tempID, deptCode, reportYear, reportDate);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(excelPageList, ser, SerializerFeature.WriteNullListAsEmpty), "findExcelPages");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 数据查询初始化
     *
     * @param request
     * @param response
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/eemCommonController.do", params = "method=submitOrg")
    @ResponseBody
    public void submitOrg(HttpServletRequest request, HttpServletResponse response, RepotOrg repotOrg) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        String msg = "";
        try {
            msg = eemCommonService.saveOrg(request,repotOrg,userEntity);
        } catch (Exception e) {
            msg = "系统内部运行异常";
            e.printStackTrace();
        } finally {
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isBlank(msg)) {
                jsonObject.put("success", true);
                jsonObject.put("msg", "导入成功！");
            } else {
                jsonObject.put("false", false);
                jsonObject.put("msg", msg);
            }
            endHandle(request, response, jsonObject, "importReportData");
        }
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=initManger")
    @ResponseBody
    public ModelAndView initReport(HttpServletRequest request, HttpServletResponse response,Long type) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        String orgs = eemCommonService.getMangerOrgNams(userEntity);
        request.setAttribute("orgNames", orgs);
        return new ModelAndView(new InternalResourceView("/base/page/managerRepotPow.jsp"));
    }


    @RequestMapping(value = "/eemCommonController.do", params = "method=saveApply")
    @ResponseBody
    public void saveApply(HttpServletResponse response, HttpServletRequest request, String operdesc,String id) throws UIException {
        String msg = "";
        try {
                    msg = eemCommonService.saveApply(operdesc,id,getUserEntity(request));

        } catch (Exception e) {
            throw new UIException("saveAudit", e);
        } finally {


            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isBlank(msg)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", msg);
            }
            endHandle(request, response, jsonObject, "");
        }
    }


    //jw 3.2 添加 方法       ------------------------------------------------------------------------
    @RequestMapping(value = "/eemCommonController.do", params = "method=todoApplyaaa")
    @ResponseBody
    public ModelAndView todoApplyaaa(HttpServletRequest request, HttpServletResponse response,String type,String nodeID) throws UIException {
        try {
            UserEntity userEntity = getUserEntity(request);
            String tempIds = eemCommonService.findTempIdsByNodeId(userEntity.getUserName(),nodeID);
            request.setAttribute("tempIds", tempIds);
            request.setAttribute("tempList", eemCommonService.findTempList("report", userEntity, tempIds));

            OrgEntity orgEntity = AAAAAdapter.getCompany(getUserEntity(request).getOrgID().intValue());
            request.setAttribute("proType", orgEntity.getOrgName());  //jw   省份查询框
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(type!=null&&type.equals("2")){
            return new ModelAndView(new InternalResourceView("/base/page/alreadyApply.jsp"));
        }else {
            return new ModelAndView(new InternalResourceView("/base/page/todoApply.jsp"));
        }

    }
    //---------------------------------------------------------------------------------

    @RequestMapping(value = "/eemCommonController.do", params = "method=queryApplyList")
    @ResponseBody
    public void queryApplyList(HttpServletResponse response, HttpServletRequest request, Integer type,String tempIds ) throws UIException {
        try {//type 1:待办申请  2：已办申请  3：已申请重新上报
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            UserEntity user =getUserEntity(request);
            long sign = 2L;
            if(user.getCategory().equals("UNI")){
                sign = 1L;
            }
                pager = eemCommonService.queryApplyList(pager, type, getUserEntity(request) ,sign ,tempIds);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }


    @RequestMapping(value = "/eemCommonController.do", params = "method=getApplyData")
    @ResponseBody
    public ModelAndView getProblemData(HttpServletRequest request, HttpServletResponse response, String objectId,String type) throws UIException {
        EemApply apply = eemCommonService.getEemApplyById(Long.parseLong(objectId));

        request.setAttribute("type", type);
        request.setAttribute("user", getUserEntity(request));
        apply.setTheme(apply.getReportYear() + "-" + apply.getReportDate());
        request.setAttribute("problem", apply);
        if(type!=null&&"show".equals(type)){
//                Map infoMap = problemReportService.getByUser(Long.parseLong(objectId),getUserEntity(request));
//                request.setAttribute("content", infoMap!=null?infoMap.get("content"):"");
//                request.setAttribute("userName", infoMap!=null?infoMap.get("username"):"");
            return new ModelAndView(new InternalResourceView("base/page/todoApplynfoShow.jsp"));
        }

        return new ModelAndView(new InternalResourceView("base/page/todoApplynfoEdit.jsp"));
        //      return  null;
    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=saveApply2")
    public ModelAndView saveApply2(HttpServletResponse response, HttpServletRequest request, EemApply apply) throws UIException {

        eemCommonService.saveApply(apply,getUserEntity(request));
        return new ModelAndView(new InternalResourceView("base/page/refreshParentPage.jsp"));

    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=initManagerTemplate")
    public ModelAndView initManagerTemplate(HttpServletResponse response, HttpServletRequest request,String temp) throws UIException {

       UserEntity userEntity = getUserEntity(request);
        List<Map> list = eemCommonService.initManagerTemplate("",userEntity);
        List<Tempt> list2 = eemCommonService.initManagerTemplate2(temp,userEntity);
        request.setAttribute("userName",userEntity.getUserName());
//        List<Map> slist = eemCommonService.findSpecialtys(getUserEntity(request));
        request.setAttribute("temps",list);
        request.setAttribute("temps2",list2);
//        request.setAttribute("slist",slist);
        return new ModelAndView(new InternalResourceView("base/page/managerTemplate.jsp"));

    }

    @RequestMapping(value = "/eemCommonController.do", params = "method=queryVengerList")
    @ResponseBody
    public void queryVengerList(HttpServletResponse response, HttpServletRequest request, String type) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemCommonService.queryVengers(pager, type, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }

    @RequestMapping(value ="eemCommonController.do", params = "method=innitManagerTemp")
    public String innitManagerTemp(HttpServletResponse response, HttpServletRequest request,String temp) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        List<Map> list = eemCommonService.initManagerTemplate("",userEntity);
        List<Tempt> list2 = eemCommonService.initManagerTemplate2(temp, userEntity);
        request.setAttribute("userName",userEntity.getUserName());
//        List<Map> slist = eemCommonService.findSpecialtys(getUserEntity(request));
        request.setAttribute("temps",list);
        request.setAttribute("temps2",list2);
        return "forward:/base/page/testJstl.jsp";
    }

    @RequestMapping(value ="eemCommonController.do", params = "method=saveTemptSpecInfo")
public void saveTemptSpecInfo(HttpServletRequest request,HttpServletResponse response,String nodeSet) throws UIException {
    UserEntity userEntity = getUserEntity(request);
    eemCommonService.saveTemptSpecInfo(userEntity,nodeSet);

}


    @RequestMapping(value = "/eemCommonController.do", params = "method=messageRemind")
    @ResponseBody
    public ModelAndView messageRemind(HttpServletRequest request, HttpServletResponse response) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        OrgEntity company = null;
        try {
            company = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            String sql="select result from t_eem_report_apply where reportOrgName='" + company.getOrgCode() + "'";
            List<Map> messageList = baseDAO.findNativeSQL(sql.toString(), null);
            if(messageList.size()>0){
                String result=messageList.get(0).get("result").toString();
                if(result=="同意"){
                    request.setAttribute("result", 1);
                }
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }

        return new ModelAndView(new InternalResourceView("/base/page/todo.jsp"));
    }

}
