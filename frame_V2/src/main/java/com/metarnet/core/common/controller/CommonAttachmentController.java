package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.controller.editor.JsonTimestampToStringUtil;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.metarnet.core.common.service.IAttachmentRelProcService;
import com.metarnet.core.common.service.ICommEntityService;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. Company: Metarnet User: kans Date: 13-4-22 Time:
 * 下午5:30 Description: 流程|非流程附件上传下载控制器: 主要包括附件的上传、查询附件列表、下载附件、删除附件功能.
 */
@Controller
@RequestMapping("/attachment.do")
public class CommonAttachmentController extends BaseController {

    private static final String KEY_WORDS = "keywords";

    private static final String OPERATOR = "operator";

    private static final String CHARACTER_GB2312 = "gb2312";

    private static final String CHARACTER_ISO8859 = "ISO8859-1";

    private static final String DATE_PATTERN = "yyyyMMddHHmmss_SSSSS";

    /**
     * 最多同时下载500个文件
     */
    private static final int MAX_DOWNLOAD_COUNT = 500;

    /**
     * 每次最多写出4MB
     */
    private static final int OUTPUT_SIZE = 4096;

    /**
     * log4J日志记录对象
     */
    private Logger logger = Logger.getLogger(CommonAttachmentController.class);


    @Resource

    private ICommEntityService iCommEntityService;

    /**
     * 流程附件服务层接口
     */
    @Resource
    private IAttachmentRelProcService attachmentRelProcService;

    /**
     * 实现功能: 附件上传
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws UIException UI异常信息
     */
    @RequestMapping(params = "method=upload")
    @ResponseBody
    public void uploadFile(HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            this.logger.debug("-------------------upload file start !");
            Object object = this.upload(request);
            JSONObject jsonObject = new JSONObject();
            if (object == null) {
                jsonObject.put("success", false);
            } else {
                jsonObject.put("success", true);
//                JsonConfig config = new JsonConfig();
//                config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());
//                config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());
                // 多附件上传时，每次请求其实只上传了一个文件
                jsonObject.put("data", JSON.toJSONString(object));
            }
            this.logger.debug("-------------------upload file success, the return data: " + jsonObject.get("data"));
            this.logger.debug("-------------------upload file end !");
            endHandle(request, response, jsonObject, this.getClass().getName() + "uploaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 实现功能: 查询附件列表
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=query")
    public void queryFiles(HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            this.logger.debug("-------------------query file start !");
            String jsonData = request.getParameter("jsonData");
            String flowingFlag = request.getParameter("flowingFlag");
            this.logger.debug("-------------------query jsonData: " + jsonData);
            this.logger.debug("-------------------query flowingFlag: " + flowingFlag);

            List dataList = this.queryFilesList(jsonData, flowingFlag);
            this.logger.debug("-------------------query result size: " + dataList.size());

            Map map = new HashMap();
            map.put("attachmentList", dataList);
//            JsonConfig config = new JsonConfig();
//            config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());// 时间处理
//            config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());// 时间处理
//            JSONObject jsonObject = JSONObject.fromObject(map, config);
            this.logger.debug("-------------------query file end !");
            endHandle(request, response, JSON.toJSONString(map), this.getClass().getName() + "queryFiles successfully!");
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 实现功能: 根据根流程实例ID查询附件列表
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=queryFilesByRootProId")
    public void queryFilesByRootProId(HttpServletRequest request, HttpServletResponse response) throws UIException {
        String jsonData = request.getParameter("jsonData");
        try {
            List dataList = this.queryFilesList(jsonData, "Y");
            Map map = new HashMap();
            map.put("attachmentList", dataList);
//            JsonConfig config = new JsonConfig();
//            config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());// 时间处理
//            config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());// 时间处理
//            JSONObject jsonObject = JSONObject.fromObject(map, config);
            endHandle(request, response, JSON.toJSONString(map), this.getClass().getName() + "queryFiles successfully!");
        } catch (Exception e) {
            e.printStackTrace();
//    		    throw new UIException(this.getClass().getName(), e.getMessage());
        }

    }

    /**
     * 实现功能: 查询附件列表(目前未使用)
     *
     * @param jsonData    前台查询条件JSON字符串
     * @param flowingFlag 流程标识(流程Y|非流程N)
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=queryFileList")
    public String queryFileList(@RequestParam(value = "jsonData")
                                String jsonData, @RequestParam(value = "flowingFlag")
                                String flowingFlag, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            List dataList = this.queryFilesList(jsonData, flowingFlag);
            Map map = new HashMap();
            map.put("attachmentList", dataList);
//            JsonConfig config = new JsonConfig();
//            config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());// 时间处理
//            config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());// 时间处理
            String resultArray = JSON.toJSONString(map);
            return resultArray;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 下载附件列表
     *
     * @param jsonArr     附件json数组对象
     * @param flowingFlag 流程标识(流程为Y|非流程为N)
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=download")
    @ResponseBody
    public void download(@RequestParam(value = "jsonArr")
                         String jsonArr, @RequestParam(value = "flowingFlag")
                         String flowingFlag, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            this.logger.debug("-------------------download file start !");
            this.logger.debug("-------------------download file jsonArr: " + jsonArr);

            List dataList = this.getAttachmentFilesList(jsonArr, flowingFlag);
            this.logger.debug("-------------------download result size: " + dataList.size());

            this.downloadFiles(dataList, response);

            this.logger.debug("-------------------download file end !");
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 实现功能: 删除附件
     *
     * @param jsonArr     需要删除的附件json数组对象
     * @param flowingFlag 流程标识(流程为Y|非流程为N)
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=delete")
    @ResponseBody
    public void deleteAttachmentList(@RequestParam(value = "jsonArr")
                                     String jsonArr, @RequestParam(value = "flowingFlag")
                                     String flowingFlag, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            List dataList = this.getAttachmentFilesList(jsonArr, flowingFlag);
            this.attachmentRelProcService.deleteAttachments(dataList, this.getUserId(request));
            jsonObject.put("success", true);
        } catch (Exception e) {
            jsonObject.put("success", false);
            e.printStackTrace();
            //	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        endHandle(request, response, jsonObject, this.getClass().getName() + "uploaded successfully!");
    }

    /**
     * 实现功能: 更新附件列表信息
     *
     * @param jsonArr     需要更新的附件记录json数组
     * @param flowingFlag 流程标识(流程为Y|非流程为N)
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @throws UIException
     */
    @RequestMapping(params = "method=updateAttachmentList")
    @ResponseBody
    public void updateAttachmentList(@RequestParam(value = "jsonArr")
                                     String jsonArr, @RequestParam(value = "flowingFlag")
                                     String flowingFlag, HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            List dataList = this.getAttachmentFilesList(jsonArr, flowingFlag);
            // 初始化需要更新的参数
            TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
            this.attachmentRelProcService.updateAttachments(dataList, attachmentRelProc, this.getUserId(request));
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 实现功能: 上传附件、保存附件对象
     *
     * @param request
     * @throws UIException
     */
    protected Object upload(HttpServletRequest request) throws UIException {
        try {
            String uploadedByPersonId = request.getParameter("uploadedByPersonId");// 上传人ID
            this.logger.debug("-------------------upload file param: uploadedByPersonId is: " + uploadedByPersonId);

	    /*
         * UserEntity userEntity =
	     * AAAAAdapter.getInstence().findUserbyUserID(Integer.parseInt(uploadedByPersonId));
	     */
            UserEntity userEntity = this.getUserEntity(request);
            Object object = this.initParams(request);
            this.logger.debug("-------------------upload file Object before: " + object);
            if (object != null) {
                TEomAttachmentRelProc attachmentRelProc = this.attachmentRelProcService.saveFileAndUploadToPass(
                        object, request, Constants.STORAGE_NAME, KEY_WORDS, OPERATOR, userEntity);
                object = attachmentRelProc;
            }
            this.logger.debug("-------------------upload file Object after: " + object);
            return object;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 初始化系统附件上传对象类型:流程类 | 非流程类 使用说明:
     * 流程与非流程附件上传必须传递参数:flowingFlag,参数值Y代表流程类型,N代表非流程类型
     * <p/>
     *
     * @param request HttpServletRequest
     * @return 具体类型附件对象
     * @example: 1.流程类需要传递的参数有[参数中文名-传递参数名]: 流程标识-flowingFlag
     * 附件类型-attachmentTypeEnumId 附件格式-attachmentFormatEnumId
     * 附件关联表分片ID-shardingId 流转对象表名-flowingObjectTable
     * 流转对象ID-flowingObjectId 流转对象分片ID-flowingObjectShardingId
     * 活动(环节)实例ID-activityInstanceId 任务实例ID-taskInstanceId <p/>
     * 2.非流程类需要传递的参数有[参数中文名-传递参数名]: 流程标识-flowingFlag
     * 附件类型-attachmentTypeEnumId 附件格式-attachmentFormatEnumId
     * 附件关联表分片ID-shardingId 关联对象ID-objectId 关联对象的表名-objectTable
     * 引用对象分片ID-objectShardingId <p/>
     */
    protected Object initParams(HttpServletRequest request) throws UIException {
        try {
            UserEntity userEntity = this.getUserEntity(request);
            OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(userEntity.getOrgID());
            Integer uploadedByPersonId = userEntity.getUserId().intValue();
            String uploadedByPersonName = userEntity.getTrueName();
            Integer uploadedByOrgId = orgEntity.getOrgId().intValue();
            String uploadedByOrgName = orgEntity.getOrgName();

            String flowingFlag = request.getParameter("flowingFlag");// 流程标识(流程类附件为Y|非流程类附件为N)
            String attachmentTypeEnumId = request.getParameter("attachmentTypeEnumId");// 附件类型
            String attachmentFormatEnumId = request.getParameter("attachmentFormatEnumId");// 附件格式
            String shardingId = request.getParameter("shardingId");// 附件关联表分片ID
            String attribute1 = request.getParameter("attribute1");// 附加条件


	    /*
	     * String uploadedByPersonId =
	     * request.getParameter("uploadedByPersonId");//上传人ID String
	     * uploadedByPersonName =
	     * request.getParameter("uploadedByPersonName");//上传人名称 String
	     * uploadedByOrgId =
	     * request.getParameter("uploadedByOrgId");//上传人所在组织ID String
	     * uploadedByOrgName =
	     * request.getParameter("uploadedByOrgName");//上传人所在组织名称
	     */

                String flowingObjectTable = request.getParameter("flowingObjectTable");// 流转对象表名
                String flowingObjectId = request.getParameter("flowingObjectId");// 流转对象ID-对应申请单ID、调度单ID、反馈信息ID、电路ID或者产品ID
                String flowingObjectShardingId = request.getParameter("flowingObjectShardingId");// 流转对象分片ID
                String activityInstanceId = request.getParameter("activityInstanceId");// 活动(环节)实例ID
                String taskInstanceId = request.getParameter("taskInstanceId");// 任务实例ID
                String rootProcessInstanceId = request.getParameter("rootProcessInstanceId");// 根流程实例ID

                TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
                attachmentRelProc.setFlowingObjectTable(flowingObjectTable);
                attachmentRelProc.setFlowingObjectId(Long.parseLong(flowingObjectId));
                if (StringUtils.isNotEmpty(flowingObjectShardingId)) {
                    attachmentRelProc.setFlowingObjectShardingId(Integer.parseInt(flowingObjectShardingId));
                }
                attachmentRelProc.setActivityInstanceId(activityInstanceId);
                attachmentRelProc.setTaskInstanceId(taskInstanceId);
                attachmentRelProc.setAttachmentTypeEnumId(Integer.parseInt(attachmentTypeEnumId));
                attachmentRelProc.setAttachmentFormatEnumId(Integer.parseInt(attachmentFormatEnumId));
                if (Constants.IS_SHARDING) {
                    attachmentRelProc.setShardingId(Integer.parseInt(shardingId));
                }
                if (StringUtils.isNotEmpty(rootProcessInstanceId)) {
                    attachmentRelProc.setAttribute1(rootProcessInstanceId); // 根流程实例ID保存到attribute1
                }
                attachmentRelProc.setUploadedByPersonId(uploadedByPersonId);
                attachmentRelProc.setUploadedByPersonName(uploadedByPersonName);
                attachmentRelProc.setUploadedByOrgId(uploadedByOrgId);
                attachmentRelProc.setUploadedByOrgName(uploadedByOrgName);
                attachmentRelProc.setAttribute1(attribute1);
                return attachmentRelProc;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 将JSON字符串转换为指定类型对象
     *
     * @param jsonString JSON字符串
     * @param clazz      转换对象类型
     * @param <T>        类型
     * @return
     * @throws UIException
     */
    protected <T> T parseJsonString(String jsonString, Class<T> clazz) throws UIException {
        try {
            T object = null;
            if (StringUtils.isNotEmpty(jsonString)) {
                object = (T) JSON.parseObject(jsonString, clazz);
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 根据传入的查询条件、流程标识查询附件对象列表 使用说明: 必须传入流程标识参数flowingFlag(流程为Y|非流程为N)
     * <p/>
     *
     * @param jsonData    查询条件(封装的JSON字符串)
     * @param flowingFlag 流程标识
     * @return JSONObject格式的附件对象记录
     * @throws UIException
     */
    protected List queryFilesList(String jsonData, String flowingFlag) throws UIException {
        try {
            List dataList = null;
            if (StringUtils.isNotEmpty(flowingFlag)) {
                List<TEomAttachmentRelProc> attachmentRelProcList = JSON.parseArray(jsonData,
                        TEomAttachmentRelProc.class);
                dataList = this.attachmentRelProcService.findByExample(attachmentRelProcList);
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 根据json对象数组、流程标识进行对象转换 流程标识为Y转换为流程附件对象List,否则转换为非流程对象List
     *
     * @param jsonArray   json数组对象
     * @param flowingFlag 流程标识(流程为Y|非流程为N)
     * @return 对象List集合
     * @throws UIException
     */
    protected List getAttachmentFilesList(String jsonArray, String flowingFlag) throws UIException {
        List dataList = null;
        try {
            if (StringUtils.isNotEmpty(flowingFlag) && StringUtils.isNotEmpty(jsonArray)) {
                dataList = JSON.parseArray(jsonArray, TEomAttachmentRelProc.class);
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName() + ".getDownloadFilesList method exception", e.getMessage());
        }
        return null;
    }

    /**
     * 实现功能: 下载附件
     *
     * @param dataList 需要下载的附件列表
     * @param response HttpServletResponse
     * @throws UIException
     */
    protected void downloadFiles(List dataList, HttpServletResponse response) throws UIException {
        try {
            DownloadFileInfo[] downloadFileInfos = new DownloadFileInfo[MAX_DOWNLOAD_COUNT];
            FileAdapter fileAdapter = FileAdapter.getInstance();
            ServletOutputStream servletOutputStream = response.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // 用户需下载的文件计数
            int index = 0;
            for (Object object : dataList) {
                TEomAttachmentRelProc attachmentRelProc = (TEomAttachmentRelProc) object;
                downloadFileInfos[index] = fileAdapter.download(attachmentRelProc.getAttachmentId());
                index++;
            }

            // 下载一个文件
            if (index == 1) {
                String filename = new String(downloadFileInfos[0].getFileName().getBytes(CHARACTER_GB2312),
                        CHARACTER_ISO8859);
                response.setContentType("octets/stream");
                response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
                /*int len = 0;
                // 每次写出 4MB
                byte[] b = new byte[OUTPUT_SIZE];
                InputStream inputStream = downloadFileInfos[0].getInput();
                while ((len = inputStream.read(b)) != -1) {
                    byteArrayOutputStream.write(b, 0, len);
                }*/
                byteArrayOutputStream = downloadFileInfos[0].getByteArrayOutputStream();
            } else if (index > 1) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
                String dateString = simpleDateFormat.format(new Date());
                String fileName = "附件压缩包" + dateString + ".zip";
                logger.debug(fileName);
                fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                // 将输入流数组压缩包写入字节数组
                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
                for (int i = 0; i < downloadFileInfos.length && downloadFileInfos[i] != null; i++) {
                    String downloadFileName = downloadFileInfos[i].getFileName(); // new
                    // String(downloadFileInfos[i].getFileName().getBytes(CHARACTER_GB2312),
                    // "UTF-8");
                    // 更改文件名，避免同名文件在解压缩时被覆盖
                    downloadFileName = (i + 1) + "_" + downloadFileName;
                    logger.debug(fileName + "(" + i + ") ==> " + downloadFileName);
                    zipOutputStream.putNextEntry(new ZipEntry(downloadFileName));
                    int len;
                    // 每次写出4M
                    byte[] b = new byte[OUTPUT_SIZE];
//                    InputStream inputStream = downloadFileInfos[i].getInput();
                    InputStream inputStream = new ByteArrayInputStream(downloadFileInfos[i].getByteArrayOutputStream().toByteArray());
                    while ((len = inputStream.read(b)) != -1) {
                        zipOutputStream.write(b, 0, len);
                    }
                    zipOutputStream.closeEntry();
                }
                zipOutputStream.setEncoding("GBK");
                zipOutputStream.close();
            }
            byte[] ba = byteArrayOutputStream.toByteArray();
            if (ba != null) {
                servletOutputStream.write(ba);
            }
            servletOutputStream.flush();
            byteArrayOutputStream.close();
            servletOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName() + ".downloadFiles method exception", e.getMessage());
        }
    }

    @RequestMapping(params = "method=Alldownload")
    @ResponseBody
    public void Alldownload(HttpServletRequest request, HttpServletResponse response, String rootProcessInstId) throws UIException {

        try {
            List dataList = iCommEntityService.allDown(request, response, rootProcessInstId, getUserEntity(request));
            this.AlldownloadFiles(dataList, response);
        } catch (ServiceException e) {
            e.printStackTrace();
        }


    }


    @RequestMapping(params = "method=checkAlldown")
    public void checkAlldown(HttpServletRequest request, String rootProcessInstId, HttpServletResponse response) throws UIException {
        try {
            Boolean checkAllDown = iCommEntityService.CheckAllDown(request, response, rootProcessInstId, getUserEntity(request));
            Map map = new HashMap();
            map.put("checkAllDown", checkAllDown);
//            JsonConfig config = new JsonConfig();
//            config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());// 时间处理
//            config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());// 时间处理
//            JSONObject jsonObject = JSONObject.fromObject(map, config);
            endHandle(request, response, JSON.toJSONString(map), this.getClass().getName() + "queryFiles successfully!");
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 实现功能: 下载附件
     *
     * @param dataList 需要下载的附件列表
     * @param response HttpServletResponse
     * @throws UIException
     */
    protected void AlldownloadFiles(List dataList, HttpServletResponse response) throws UIException {
        try {
            DownloadFileInfo[] downloadFileInfos = new DownloadFileInfo[MAX_DOWNLOAD_COUNT];
            FileAdapter fileAdapter = FileAdapter.getInstance();
            ServletOutputStream servletOutputStream = response.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // 用户需下载的文件计数
            int index = 0;
            for (Object object : dataList) {
                TEomAttachmentRelProc attachmentRelProc = (TEomAttachmentRelProc) object;
                downloadFileInfos[index] = fileAdapter.download(attachmentRelProc.getAttachmentId());
                downloadFileInfos[index].setFileName(attachmentRelProc.getUploadedByOrgName() + "_" + attachmentRelProc.getAttachmentName());
                index++;
            }

            // 下载一个文件
            if (index > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
                String dateString = simpleDateFormat.format(new Date());
                String fileName = "附件压缩包" + dateString + ".zip";
                logger.debug(fileName);
                fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                // 将输入流数组压缩包写入字节数组
                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
                for (int i = 0; i < downloadFileInfos.length && downloadFileInfos[i] != null; i++) {
                    String downloadFileName = downloadFileInfos[i].getFileName(); // new
                    // String(downloadFileInfos[i].getFileName().getBytes(CHARACTER_GB2312),
                    // "UTF-8");
                    // 更改文件名，避免同名文件在解压缩时被覆盖
                    downloadFileName = (i + 1) + "_" + downloadFileName;
                    logger.debug(fileName + "(" + i + ") ==> " + downloadFileName);
                    zipOutputStream.putNextEntry(new ZipEntry(downloadFileName));
                    int len;
                    // 每次写出4M
                    byte[] b = new byte[OUTPUT_SIZE];
//                    InputStream inputStream = downloadFileInfos[i].getInput();
                    InputStream inputStream = new ByteArrayInputStream(downloadFileInfos[i].getByteArrayOutputStream().toByteArray());;
                    while ((len = inputStream.read(b)) != -1) {
                        zipOutputStream.write(b, 0, len);
                    }
                    zipOutputStream.closeEntry();
                }
                zipOutputStream.setEncoding("GBK");
                zipOutputStream.close();
            }
            byte[] ba = byteArrayOutputStream.toByteArray();
            if (ba != null) {
                servletOutputStream.write(ba);
            }
            servletOutputStream.flush();
            byteArrayOutputStream.close();
            servletOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName() + ".downloadFiles method exception", e.getMessage());
        }
    }

}
