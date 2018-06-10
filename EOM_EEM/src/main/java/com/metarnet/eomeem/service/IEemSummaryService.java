package com.metarnet.eomeem.service;

import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.EvaluationCollectTime;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import jxl.write.WritableWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Administrator on 2016/8/14.
 */
public interface IEemSummaryService {
    HSSFWorkbook fromDBByteArrayToTableForEvaPoi(byte[] inputArray, OutputStream os, String reportDateStr, List<OrgEntity> deps, String reportYear, String formName) throws Exception;

    HSSFWorkbook fromDBByteArrayToTableForEvaCablePoi(byte[] inputArray, OutputStream os, String reportDateStr, List<OrgEntity> deps, String reportYear, String formName, HashSet<String> pageNameList) throws Exception;

    WritableWorkbook fromDBByteArrayToTable(long sumId, byte[] inputArray, OutputStream os, String reportDateStr, String deps, String reportYear, String formId, Boolean flag) throws Exception;

    String saveZBCollectData(String reportYear, String reportDate, EemTempEntity summaryTemp, UserEntity userEntity, String deps, String formIdStr, String deptNames);

    void saveEvaluationFileData(Long objectID, String filePath, String fileName, String reportCode, UserEntity userEntity);

    String checkReportPro(Long formId, String reportYear, String reportDate, String deptIds);

    EvaluationCollectTime saveEvaluationCollectTime(EvaluationCollectTime evaluationCollectTime);
}