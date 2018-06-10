package com.metarnet.eomeem.service;


import com.metarnet.core.common.exception.DAOException;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.ExcelPage;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface IEemReportService {
    public ExcelPage initReport(UserEntity user, HttpServletRequest request, TaskInstance taskInstance);

    public String excelToPage(MultipartFile file);

    public String excelToHtmlByID(String objectId);

    public HSSFWorkbook downReportData(String objectId);

    public ExcelPage getExcelPage(Long formId) throws DAOException;

    public boolean checkReport(ExcelPage excelPage, UserEntity userEntity);

    String saveReportData(byte[] byteArr, ExcelPage excelPage, String sheetName, String withdraw, UserEntity userEntity);

    String saveReportData(byte[] byteArr, ExcelPage excelPage, UserEntity userEntity) ;

    String hasPowerToSave(ExcelPage excelPage, UserEntity userEntity);

    String hasPowerToSave2(ExcelPage excelPage, UserEntity userEntity);

    void importEvaluationFileDataForCable(long fileDataTid, byte[] fileByteArray, EemTempEntity eemTempEntity, UserEntity user, String reportCode, String reportDate, String reportYear) throws Exception;
}
