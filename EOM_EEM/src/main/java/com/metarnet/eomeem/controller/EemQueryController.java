package com.metarnet.eomeem.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.PagerPropertyUtils;
import com.metarnet.eomeem.model.DeviceEntity;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.service.IEemQueryService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.EemConstants;
import com.metarnet.eomeem.utils.FileUtils;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
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
import java.io.*;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/14.
 */
@Controller
public class EemQueryController extends BaseController {
    @Resource
    private IEemQueryService eemQueryService;
    @Resource
    private IEemTemplateService eemTemplateService;
    private static final int OUTPUT_SIZE = 4096;
    private static final String CHARACTER_GB2312 = "gb2312";

    private static final String CHARACTER_ISO8859 = "ISO8859-1";

    /**
     * 数据查询
     *
     * @param response
     * @param request
     * @param type
     * @throws UIException
     */
    @RequestMapping(value = "/eemQueryController.do", params = "method=queryDataList")
    @ResponseBody
    public void queryDataList(HttpServletResponse response, HttpServletRequest request, String type,String tempIds) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            UserEntity userEntity = getUserEntity(request);
            pager = eemQueryService.queryDataList(pager, type, userEntity, tempIds);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }

    /**
     * 公布版数据下载查询
     *
     * @param response
     * @param request
     * @param type
     * @throws UIException
     */
    @RequestMapping(value = "/eemQueryController.do", params = "method=queryDataListAll")
    @ResponseBody
    public void queryDataListAll(HttpServletResponse response, HttpServletRequest request, String type,String tempIds) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemQueryService.queryDataListAll(pager, type, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }


    /**
     * 数据打包下载的方法
     * @param response
     * @param request
     * @param reportYear
     * @param reportData
     * @param tempId
     * @throws UIException
     */
    @RequestMapping(value = "/eemQueryController.do", params = "method=downAllProReportExcel")
    @ResponseBody
    public void downAllProReportExcel(HttpServletResponse response, HttpServletRequest request, String reportYear, String reportData,String tempId,String reportOrgCode) throws UIException {
        String dataString = new Date().getTime() + "";
        File files = null;

        ServletOutputStream servletOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream);
        try {
            reportData = URLDecoder.decode(reportData,"UTF-8");
            servletOutputStream = response.getOutputStream();
            List<ExcelPage> excelPageList = new ArrayList<ExcelPage>();
            files = new File(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString);
            if (!files.exists()) {
                files.mkdirs();
            }
            if (StringUtils.isNotBlank(reportYear) && StringUtils.isNotBlank(reportData)) {//当前年度
                excelPageList = eemQueryService.downAllProReportExcel2(reportYear, reportData, tempId, reportOrgCode);
            }
            List<EemTempEntity> reportTempEntityList = eemTemplateService.findAllTempEntity("report");

            for (EemTempEntity eemTempEntity : reportTempEntityList) {
                String dir = "";
                FileAdapter fileAdapter = FileAdapter.getInstance();

                List<DownloadFileInfo> downloadFileInfoList = new ArrayList<DownloadFileInfo>();
                for (ExcelPage ere : excelPageList) {
                    if (eemTempEntity.getObjectId().equals(ere.getTpInputID())) {
                        if(ere.getAttachmentId() != null){
                            DownloadFileInfo downloadFileInfo = fileAdapter.download(ere.getAttachmentId());
                            String downloadFileName = ere.getOperOrgName() + ere.getFileName() + ".xls"; // new
                            downloadFileInfo.setFileName(downloadFileName);
                            downloadFileInfoList.add(downloadFileInfo);
                        }
                    }
                }
                if (downloadFileInfoList.size() > 0) {
                    ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
                    ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream1);
                    FileOutputStream fos = new FileOutputStream(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString + "/" + eemTempEntity.getShortName() + ".zip");
                    int i = 0;
                    for (DownloadFileInfo downloadFileInfo : downloadFileInfoList) {
                        String downloadFileName = downloadFileInfo.getFileName();
                        zipOutputStream.putNextEntry(new ZipEntry(downloadFileName));
                        int len;
                        // 每次写出4M
                        byte[] b = new byte[OUTPUT_SIZE];

                        InputStream inputStream = new ByteArrayInputStream(downloadFileInfo.getByteArrayOutputStream().toByteArray());
                        while ((len = inputStream.read(b)) != -1) {
                            zipOutputStream.write(b, 0, len);
                        }
                        zipOutputStream.closeEntry();
                        inputStream.close();
                        i++;
                    }
                    zipOutputStream.setEncoding("GBK");
                    byte[] ba = byteArrayOutputStream1.toByteArray();
                    if (ba != null) {
                        fos.write(ba);
                    }
                    zipOutputStream.flush();
                    zipOutputStream.close();
                    byteArrayOutputStream1.flush();
                    byteArrayOutputStream1.close();
                    fos.flush();
                    fos.close();

                } else {
                    continue;
                }
                if (StringUtils.isNotBlank(dir)) {
                    FileUtils.filesToZip(dir, EemConstants.GATHER_DATA_PATH + "/temp/" + dataString, eemTempEntity.getShortName());
                }
            }

            File[] fileArr = files.listFiles();
            String fileName = "打包下载.zip";
            //在服务器端创建打包下载的临时文件
            fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            if (fileArr != null) {
                for (int i = 0; i < fileArr.length; i++) {
                    String downloadFileName = fileArr[i].getName(); // new
                    downloadFileName = (i + 1) + "_" + downloadFileName;
                    zos.putNextEntry(new ZipEntry(downloadFileName));
                    int len;
                    // 每次写出4M
                    byte[] b = new byte[OUTPUT_SIZE];
                    InputStream inputStream = new FileInputStream(fileArr[i]);
                    while ((len = inputStream.read(b)) != -1) {
                        zos.write(b, 0, len);
                    }
                    zos.closeEntry();
                    inputStream.close();
                }
            }
            zos.setEncoding("GBK");
            zos.flush();
            zos.close();
            byte[] ba = byteArrayOutputStream.toByteArray();
            if (ba != null) {
                servletOutputStream.write(ba);
            }
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                servletOutputStream.flush();
                servletOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (files != null) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


//-------
    @RequestMapping(value = "/eemQueryController.do", params = "method=querySheetList")
    @ResponseBody
    public void querySheetList(HttpServletResponse response, HttpServletRequest request,Long id) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = eemQueryService.querySheetList(pager, getUserEntity(request),id);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "queryTemplateList");
        } catch (Exception e) {
            throw new UIException("queryTemplateList", e);
        }
    }
    @RequestMapping(value = "/eemQueryController.do", params = "method=initDevice")
     @ResponseBody
     public ModelAndView initDevice(HttpServletRequest request, HttpServletResponse response,Long id) throws Exception {
        try {
            List<DeviceEntity> deviceEntityList = eemQueryService.findTempListToDevice1(33L,getUserEntity(request));
            request.setAttribute("tempList",deviceEntityList);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("/base/page/deviceReportManager.jsp"));
    }

    /**
     * 整体打包下载的方法
     * @param response
     * @param request
     * @param reportYear
     * @param reportData
     * @param tempId
     * @throws UIException
     */
    @RequestMapping(value = "/eemQueryController.do", params = "method=overAllZipReportExcel")
    @ResponseBody
    public void overAllZipReportExcelExcel(HttpServletResponse response, HttpServletRequest request, String reportYear, String reportData,String tempId) throws UIException {
        String dataString = new Date().getTime() + "";
        File files = null;
        ServletOutputStream servletOutputStream = null;
        FileInputStream  fis = null;
        try {
            reportData = URLDecoder.decode(reportData,"UTF-8");
            servletOutputStream = response.getOutputStream();
            List<ExcelPage> excelPageList = new ArrayList<ExcelPage>();
            files = new File(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString);
            if (!files.exists()) {
                files.mkdirs();
            }
            if (StringUtils.isNotBlank(reportYear) && StringUtils.isNotBlank(reportData)) {
                //获取当前粒度的上报数据
                excelPageList = eemQueryService.downAllProReportExcel(reportYear, reportData,tempId);
            }
            //获取所有上报模板
            List<EemTempEntity> reportTempEntityList = eemTemplateService.findAllTempEntity("report");

            for (EemTempEntity eemTempEntity : reportTempEntityList) {
                String dir = "";
                FileAdapter fileAdapter = FileAdapter.getInstance();
                List<DownloadFileInfo> downloadFileInfoList = new ArrayList<DownloadFileInfo>();
                for (ExcelPage ere : excelPageList) {
                    if (eemTempEntity.getObjectId().equals(ere.getTpInputID())) {
                        if(ere.getAttachmentId()==null||ere.getAttachmentId().equals("")){
                            continue;
                        }
                        DownloadFileInfo downloadFileInfo = fileAdapter.download(ere.getAttachmentId());
                        if(downloadFileInfo.getByteArrayOutputStream()!=null){
                            String downloadFileName = ere.getOperOrgName() + ere.getFileName()  + ".xls"; // new
                            downloadFileInfo.setFileName(downloadFileName);
                            downloadFileInfoList.add(downloadFileInfo);
                        }
                    }
                }
                if (downloadFileInfoList.size() > 0) {
                    OutputStream os = null;
                    File excelDir = new File(EemConstants.GATHER_DATA_PATH + "/temp/" + dataString + "/" + eemTempEntity.getShortName() + reportYear + reportData);
                    if(!excelDir.exists()){
                        excelDir.mkdirs();
                    }
                    for (DownloadFileInfo downloadFileInfo : downloadFileInfoList) {
                        String downloadFileName = downloadFileInfo.getFileName();
                        File excelPageFile = new File(excelDir + "/" + downloadFileName);
                        if(!excelPageFile.exists()){
                            excelPageFile.createNewFile();
                        }
                        os = new FileOutputStream(excelPageFile);
                        int len;
                        // 每次写出4M
                        byte[] b = new byte[OUTPUT_SIZE];
                        InputStream inputStream = new ByteArrayInputStream(downloadFileInfo.getByteArrayOutputStream().toByteArray());
                        while ((len = inputStream.read(b)) != -1) {
                            os.write(b, 0, len);
                        }
                        inputStream.close();
                    }
                    os.flush();
                    os.close();
                } else {
                    continue;
                }
                if (StringUtils.isNotBlank(dir)) {
                    FileUtils.filesToZip(dir, EemConstants.GATHER_DATA_PATH + "/temp/" + dataString, eemTempEntity.getShortName());
                }
            }
            String fileName = reportYear + reportData + ".zip";
            //在服务器端创建打包下载的临时文件
            fileName = new String(fileName.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            //分卷压缩
            ZipFile zipFile = this.splitZip(files.getAbsolutePath() + ".zip", files.getAbsolutePath());
            ArrayList<String> list =  zipFile.getSplitZipFiles();
            ArrayList<File> file_list = new ArrayList<File>();
            if(list.size() > 1){
                ZipFile zipFile_final = new ZipFile(files.getAbsolutePath() + "_final" + ".zip");
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
                for(int i = 0;i < list.size();i++){
                    file_list.add(new File(list.get(i)));
                }
                zipFile_final.addFiles(file_list,parameters);
                //files.getAbsolutePath() + "_final" + ".zip"
                //下载文件
                fis = new FileInputStream(new File(files.getAbsolutePath() + "_final" + ".zip"));
                byte[] b = new byte[fis.available()];
                fis.read(b);
                //获取响应报文输出流对象
                ServletOutputStream  out = response.getOutputStream();
                //输出
                out.write(b);
                out.flush();
                out.close();
            }else{
                //下载文件
                fis = new FileInputStream(new File(files.getAbsolutePath() + ".zip"));
                byte[] b = new byte[fis.available()];
                fis.read(b);
                //获取响应报文输出流对象
                ServletOutputStream  out = response.getOutputStream();
                //输出
                out.write(b);
                out.flush();
                out.close();
            }

/*
            //下载文件
            fis = new FileInputStream(new File(files.getAbsolutePath() + ".zip"));
            byte[] b = new byte[fis.available()];
            fis.read(b);
            //获取响应报文输出流对象
            ServletOutputStream  out = response.getOutputStream();
            //输出
            out.write(b);
            out.flush();
            out.close();
    */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                servletOutputStream.flush();
                servletOutputStream.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (files != null) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ZipFile splitZip(String desFile,String srcFolder){
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(desFile);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        try {
            //20M分割压缩包
            zipFile.createZipFileFromFolder(srcFolder, parameters, true,10485760 * 2);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return zipFile;
    }

    @RequestMapping(value = "/eemQueryController.do", params = "method=showVendorData")
    public void showVendorData(HttpServletRequest request, HttpServletResponse response, String tpInputID) throws UIException {
        //  String tempHTML = reportService.excelToHtmlByID(objectId);
        try {
            List<Map> queryVendors = eemQueryService.queryVenderByDevice(Long.parseLong(tpInputID));
            String namme="";
            for(Map map:queryVendors){
                namme+=map.get("vendername");
                break;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("names",namme);
            endHandle(request, response, jsonObject, "importReportData");

        } catch (Exception e) {
            throw new UIException("queryVendors", e);

        }
    }

}
