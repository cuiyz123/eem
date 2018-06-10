package com.metarnet.eomeem.service;


import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.ExcelPage;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import jxl.write.WritableWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface IEemGatherService {

    public HSSFWorkbook downGatherData(String repotYear, String reportDate, EemTempEntity tempEntity, EemTempEntity repotEntity, String deptIds, UserEntity userEntity);
    //根据模版解析sql
    public WritableWorkbook fromDBByteArrayToTable(boolean aa, byte[] array, OutputStream os, String reportDateStr, List orgList, String reportYear, String formId) throws Exception;

    public EemTempEntity findReportEemTempEntity(Long objectID) ;

    public void saveAutoGather();

    void timeoutAlert();

    void saveAppoint();
}
