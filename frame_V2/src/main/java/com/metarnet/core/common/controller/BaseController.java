package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.controller.editor.*;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.ucloud.paas.proxy.aaaa.AAAAService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA. User: metarnet Date: 13-2-26 Time: 下午7:54
 * Controller基类,提供公共方法
 */
public class BaseController {
    Logger logger = Logger.getLogger("BaseController");
//    protected static final JsonConfig config = new JsonConfig();
    /**
     * 工作台定义的当前用户变量名
     */
    public static final String globalUniqueID = "globalUniqueID";

    /**
     * session中当前用户变量对象
     */
    public static final String globalUniqueUser = "globalUniqueUser";
    /**
     * 最多同时下载500个文件
     */
    private static final int MAX_DOWNLOAD_COUNT = 500;

    /**
     * 每次最多写出4MB
     */
    private static final int OUTPUT_SIZE = 4096;
    private static final String CHARACTER_GB2312 = "gb2312";

    private static final String CHARACTER_ISO8859 = "ISO8859-1";

    private static final String DATE_PATTERN = "yyyyMMddHHmmss_SSSSS";
    static {
        /*config.setIgnoreDefaultExcludes(false);
        config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());
        config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());*/
        //	config.registerJsonValueProcessor(Timestamp.class, new JsDateJsonValueProcessor());
        //	config.registerJsonValueProcessor(Date.class, new JsDateJsonValueProcessor());
    }

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws UIException {
        binder.registerCustomEditor(Date.class, new DateEdit());
        binder.registerCustomEditor(int.class, new IntEdit());
        binder.registerCustomEditor(long.class, new LongEdit());
        binder.registerCustomEditor(Timestamp.class, new TimeStampEdit());
    }

    /**
     * 返回当前登录人
     *
     * @param request
     * @return
     * @throws UIException
     */
    public UserEntity getUserEntity(HttpServletRequest request) throws UIException {
        if (null != request.getSession().getAttribute(globalUniqueUser)&&!"".equals(request.getSession().getAttribute(globalUniqueUser))) {
            return (UserEntity) request.getSession().getAttribute(globalUniqueUser);
        }

        String globalUniqueID = (String) request.getSession().getAttribute("globalUniqueID");
        System.out.println("getSession++++++++++++++++"+globalUniqueID);
        if (globalUniqueID == null || "".equals(globalUniqueID)) {
            globalUniqueID = request.getParameter("globalUniqueID");
            System.out.println("getParameter++++++++++++++++"+globalUniqueID);
            System.out.println(request.getSession().getAttribute("globalUniqueID")+"Session++++++++++++++++" +request.getSession().getAttribute(globalUniqueUser));
        }
        try {
            System.out.println("globalUniqueID++++++++++++++++"+globalUniqueID);
            UserEntity userEntity = AAAAAdapter.getInstence().findUserBySessionID(globalUniqueID);
            request.getSession().setAttribute(globalUniqueUser, userEntity);
            request.getSession().setAttribute("globalUniqueID", globalUniqueID);
            return userEntity;
        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//	Integer userId = getUserId(request);
//	logger.debug("userId is '" + userId + "'");
//	if (userId != null && userId != 0) {
//	    try {
//		UserEntity userEntity = AAAAAdapter.getInstence().findUserbyUserID(userId);
//		request.getSession().setAttribute(globalUniqueUser, userEntity);
//		return userEntity;
//	    } catch (PaasAAAAException e) {
////		throw new UIException(null, userId, e);
//	    }
//	}
        return null;
//	throw new UIException(null, userId, new Exception("没有获取到当前用户,userId=" + userId));
    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    protected void endHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, true);
    }

    protected void endHandle(HttpServletRequest request, HttpServletResponse response, String json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, true);
    }

    /**
     * Controller结束前处理 ,不记录日志 1.将json输出给log4j 2.response写入json
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    protected void endNotLogHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, false);
    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    private void endHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId,
                           boolean write) throws UIException {

        endHandle(request, response, json == null ? StringUtils.EMPTY : json.toString(), refBizId, write);

    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param result   传给前台的数据
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    public void endHandle(HttpServletRequest request, HttpServletResponse response, String result, String refBizId,
                          boolean write) throws UIException {
        try {
            Logger.getLogger(this.getClass()).debug(result);
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().print(result);
            response.getWriter().close();
            response.getWriter().close();
        /*if (write) {
        LogAdapter.getInstence().writeOperLog(refBizId, getUserEntity(request), this.getClass().getName(),
			Thread.currentThread().getStackTrace()[3].getMethodName(), LogAdapter.SUCCESS);
	    }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回当前人ID
     *
     * @param request
     * @return
     */
    protected Integer getUserId(HttpServletRequest request) {
        try {
            String globalUniqueID = (String) request.getSession().getAttribute("globalUniqueID");
            AAAAService aaaaService = new AAAAService();
            UserEntity userEntity = aaaaService.findUserBySessionID(globalUniqueID);
            if (userEntity != null) {
                return userEntity.getUserId().intValue();
            } else {
                return null;
            }
//	    logger.debug("globalUniqueID is '" + globalUniqueID + "'");
//		HttpClient client = new HttpClient();
//		GetMethod get = new GetMethod(Constants.SESSIONURL+"?sessionId="+globalUniqueID+"&appId="+Constants.MODEL_NAME);
//		get.getParams().setContentCharset("utf-8");
////		get.getParams().setParameter("sessionId",globalUniqueID);
//		client.executeMethod(get);
//		String returnContent = get.getResponseBodyAsString();
//	    /*SessionManagement sm = new SessionManagement(globalUniqueID);
//	    SSOUserInfo ue = (SSOUserInfo) sm.getAttribute("SSOUserInfo");*/
//	    return Integer.parseInt(returnContent);
        } catch (Exception e) {
            return Integer.parseInt(request.getSession().getAttribute(globalUniqueID).toString());
        }
    }

    /**
     * 获取accountId
     *
     * @param request
     * @return
     * @throws UIException
     */
    protected String getAccountId(HttpServletRequest request) throws UIException {
        return getUserEntity(request).getUserName();
    }

    /**
     * 移除空的子节点，将节点子节点属性改为“TreeNode”,调度树使用
     *
     * @param jsonObject
     * @param treeNodes
     */
    protected void removeEmptyTreeNodes(JSONObject jsonObject, JSONArray treeNodes) {
        if (treeNodes.size() > 0) {
            for (int i = 0; i < treeNodes.size(); i++) {
                JSONObject jsonTemp = treeNodes.getJSONObject(i);
                if (jsonTemp.keySet().size() > 0) {
                    JSONArray tempTreeNodes = (JSONArray) jsonTemp.get("treeNode");
                    this.removeEmptyTreeNodes(jsonTemp, tempTreeNodes);
                }
            }
            jsonObject.put("TreeNode", treeNodes);
            jsonObject.remove("treeNode");
        } else {
            jsonObject.remove("treeNode");
        }
    }

    /**
     * 得到当前人能看到部门或人的根部门
     *
     * @param cloudOrgId
     * @return
     */
    protected String getUserRangeRootDeptId(Integer cloudOrgId) {
        // 默认显示全国的
        String rootId = "1";
        try {
//	    OrgStructure orgStructure = AAAAAdapter.getInstence().findCompanysByOrgId(cloudOrgId);
//	    if (orgStructure.getCountyCompany() != null) {
//		// 显示本班组的
//		rootId = orgStructure.getCountyCompany().getOrgId().toString();
//	    } else if (orgStructure.getCityCompany() != null) {
//		// 显示本地市的
//		rootId = orgStructure.getCityCompany().getOrgId().toString();
//	    } else if (orgStructure.getProvinceCompany() != null) {// 所属省公司
//		// 显示本省的
//		rootId = orgStructure.getProvinceCompany().getOrgId().toString();
//	    }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootId;
    }

    /**
     * 根据request获取UserRangeRootDeptId
     *
     * @param request
     * @return
     */
    public String getUserRangeRootDeptId(HttpServletRequest request) {
        String userRangeRootDeptId = null;
        try {
            UserEntity currentUser = this.getUserEntity(request);
            if (currentUser != null) {
                return getUserRangeRootDeptId(Integer.valueOf(currentUser.getOrgID().toString()));
            }
        } catch (UIException e) {
            return null;
        }
        return userRangeRootDeptId;
    }

    protected void downloadFiles(List<TEomAttachmentRelProc> dataList, HttpServletResponse response,Integer type) throws UIException {
        try {
            DownloadFileInfo[] downloadFileInfos = new DownloadFileInfo[MAX_DOWNLOAD_COUNT];
            FileAdapter fileAdapter = FileAdapter.getInstance();
            ServletOutputStream servletOutputStream = response.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // 用户需下载的文件计数
            int index = 0;
            for (TEomAttachmentRelProc attachmentRelProc : dataList) {
                downloadFileInfos[index] = fileAdapter.download(attachmentRelProc.getAttachmentId());
                downloadFileInfos[index].setFileName(attachmentRelProc.getAttachmentName());
                index++;
            }

            // 下载一个文件
            if (type == 1&&index<2) {
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
            } else{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
                String dateString = simpleDateFormat.format(new Date());
                String fileName = "附件压缩包" + dateString + ".zip";
                fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                // 将输入流数组压缩包写入字节数组
                ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
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
                    ZipEntry zipEntry = new ZipEntry(downloadFileName);
                    if("Linux".equals(osName)||"linux".equals(osName)){
                        zipEntry.setUnixMode(644);
                    }
                    zipOutputStream.putNextEntry(zipEntry);
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
                if("Linux".equals(osName)||"linux".equals(osName)){
                    zipOutputStream.setEncoding("utf-8");
                }else{
                    zipOutputStream.setEncoding("GBK");
                }

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
