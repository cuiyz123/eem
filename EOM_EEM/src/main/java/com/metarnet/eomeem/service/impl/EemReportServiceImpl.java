package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.service.IAttachmentRelProcService;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.EvaluationFileData;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.model.ExcelPageValues;
import com.metarnet.eomeem.service.IEemReportService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.*;
import com.metarnet.eomeem.utils.excel.InputElement;
import com.metarnet.eomeem.utils.excel.SelectInputElement;
import com.metarnet.eomeem.utils.excel.SelectOptionItem;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/17.
 */
@Service("eemReportServiceImpl")
public class EemReportServiceImpl implements IEemReportService {
    private int rowBegin = 4;

    private int colBegin = 4;

    private static final String KEY_WORDS = "keywords";

    private static final String OPERATOR = "operator";

    Logger logger = LogManager.getLogger(EemReportServiceImpl.class);

    @Resource
    private IBaseDAO baseDAO;
    @Resource
    private IEemTemplateService templateService;
    @Resource
    private IAttachmentRelProcService attachmentRelProcService;

    @Override
    public boolean checkReport(ExcelPage excelPage, UserEntity userEntity) {

        boolean re = true;
        ExcelPage ex = new ExcelPage();
        try {
            ex.setReportOrgCode(AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode().toString());
            ex.setReportYear(excelPage.getReportYear());
            ex.setReportDate(excelPage.getReportDate());
            ex.setTpInputID(excelPage.getTpInputID());
            ex.setDisID(0);
            ex.setWillCollect(excelPage.getWillCollect());
            //
//            ex.setTpInputName(excelPage.getTpInputName());
            List<ExcelPage> excelPageList = baseDAO.findByExample(ex);
            if (excelPageList != null && excelPageList.size() > 0) {
                re = false;
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return re;

    }

    @Override
    public ExcelPage initReport(UserEntity user, HttpServletRequest request, TaskInstance taskInstance) {
        ExcelPage excelPage = new ExcelPage();
        try {
            excelPage.setOperOrgName(user.getOrgEntity().getOrgName());
            excelPage.setOperUserPhone(user.getMobilePhone());
            excelPage.setOperUserTrueName(user.getTrueName());
            return excelPage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String excelToPage(MultipartFile file) {
        String result = null;
        try {
//            result = new ExcelConverter2().fromDBByteArrayToHTMLTableEvaluation(file.getBytes(), null, "");
            result = new ExcelToHtml().parseExcelToHtml(file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String excelToHtmlByID(String objectId) {
        String result = null;
        try {
            ExcelPage excelPage = getExcelPage(Long.parseLong(objectId));
            List<ExcelPageValues> pageValuesList = findExcelPageValueByPageID(Arrays.asList(excelPage.getObjectId()));
            EemTempEntity eemTempEntity = getTemEntity(excelPage.getTpInputID());
            ExcelConverter2 converter2 = new ExcelConverter2();
            result = converter2.fromDBByteArrayToHTMLTableEvaluation(eemTempEntity.getTemplateExcelByteData().getXmlFileData(), pageValuesList, "");
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public HSSFWorkbook downReportData(String objectId) {
        try {
            ExcelPage excelPage = getExcelPage(Long.parseLong(objectId));
            List<ExcelPageValues> pageValuesList = findExcelPageValueByPageID(Arrays.asList(excelPage.getObjectId()));
            EemTempEntity eemTempEntity = getTemEntity(excelPage.getTpInputID());
            HSSFWorkbook inputTemp = new HSSFWorkbook(new ByteArrayInputStream(eemTempEntity.getTemplateExcelByteData().getUploadFileData()));
            int sheetCount = inputTemp.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                HSSFSheet sheet = inputTemp.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                int rowCount = sheet.getPhysicalNumberOfRows();//行数
                int sqlStart = 0;
                for (int ii = 0; ii < rowCount; ii++) {
                    //行 循环
                    HSSFRow row = sheet.getRow(ii);
                    int colCount = 0;
                    if (row != null) {
                        colCount = row.getLastCellNum(); //列数
                    }
                    for (int j = 0; j < colCount; j++) {//列 循环
                        sheet.setForceFormulaRecalculation(true);
                        HSSFCell cell = row.getCell(j);
                        String content = "";
                        if (!"".equals(cell) && cell != null) {
                            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                content = cell.getStringCellValue();
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                content = cell.getNumericCellValue() + "";//没有日期格式的，所以没判断
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                                content = cell.getCellFormula();
                            }
                        }
                        if (content.contains("xxxx")) {
                            Timestamp reportDate = new Timestamp(new Date().getTime());
                            int year = reportDate.getYear() + 1900;
                            int month = reportDate.getMonth() + 1;
                            content = content.substring(content.lastIndexOf("x") + 2, content.length() - 1);
                            content = year + "年" + month + "月" + content;
                            cell.setCellValue(content);
                        }
                        for (int jj = 0; jj < pageValuesList.size(); jj++) {
                            if (String.valueOf(ii).equals(String.valueOf(pageValuesList.get(jj).getRowIndex())) && String.valueOf(j).equals(String.valueOf(pageValuesList.get(jj).getColIndex()))) {
                                cell.setCellValue(pageValuesList.get(jj).getTxtValue());
                            }
                        }
                    }
                }
            }
            return inputTemp;
        } catch (Exception e) {
        }
        return null;
    }

    public Set analysisJobContentDonePoi(byte[] byteArray, HSSFSheet jobContentExcelSheet, long formId, long pageId) throws Exception {
        InputStream stream = (new ByteArrayInputStream(byteArray));
        org.dom4j.Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        if (doc == null) {
            return set;
        }
        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");
        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            //获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);
            //获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");
            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();
                //获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                String text = td.getText();
                Element span = null;
                if (text == null || "".equals(text)) {
                    Iterator spanList = td.elementIterator("SPAN");
                    while (spanList != null && spanList.hasNext()) {
                        span = (Element) spanList.next();
                        String spanText = span.getText();
                        if (spanText != null && "text:value=".equals(spanText.trim())) {
                            text = "text:value=";
                            break;
                        }
                    }
                }
                if (text != null && !"".equals(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        jobContentExcelSheet.setForceFormulaRecalculation(true);
                        logger.info("~~~~~row=" + rowCount + "!!!currentColCount=" + currentColCount);
                        HSSFRow row = jobContentExcelSheet.getRow(rowCount);
                        HSSFCell cell = row.getCell(currentColCount);
                        if (cell == null) {
                            continue;
                        }
                        String cellText = "";
                        if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            cellText = cell.getStringCellValue();
                        } else if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {//日期和数值都是CELL_TYPE_NUMERIC
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {//判断是否是日期格式
                                DateCell dc = (DateCell) cell;
                                if (dc != null && dc.getDateFormat() != null && dc.getDate() != null) {
                                    Date comparedDate = new Date();
                                    try {
                                        comparedDate = new SimpleDateFormat("yyyy-MM-dd").parse("1949-01-01");
                                    } catch (Exception e) {
                                        comparedDate = new Date(1901, 1, 1);
                                    }
                                    if (dc.getDate().before(comparedDate))
                                        cellText = dc.getDateFormat().format(dc.getDate());
                                    else
                                        cellText = new SimpleDateFormat("yyyy-MM-dd").format(dc.getDate());
                                }
                            } else {//数值格式
                                double intcellText = cell.getNumericCellValue();
                                cellText = intcellText + "";
                            }
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                            try {
                                cellText = cell.getNumericCellValue() + "";
                            } catch (Exception e) {
                                cellText = String.valueOf(0);
                            }

                        } else if (cell.getCellStyle().equals(HSSFCell.CELL_TYPE_ERROR)) {
                            cellText = String.valueOf(cell.getErrorCellValue());
                        }
                        //判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null && ismust.equals("true") && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }
                        ExcelPageValues epv = new ExcelPageValues();
                        epv.setObjectId(baseDAO.getSequenceNextValue(ExcelPageValues.class));
                        epv.setRowIndex(rowCount);
                        epv.setColIndex(currentColCount);
                        if (cellText != null) {
                            cellText = cellText.replace(",", "");
                        }
                        epv.setTxtValue(cellText);
                        epv.setPageID(pageId);
                        epv.setTpID(formId);
                        set.add(epv);
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount : colCount;
            }
        }
        return set;
    }

    public InputElement createInputElement(String txt) {

        if (txt == null)
            return null;

        // 风格类型
        String[] temp = txt.split(":", 2);

        JobConfiguration jobConfiguration = (JobConfiguration) SpringContextUtils
                .getBean("jobConfiguration");
        InputElementDefinition def = jobConfiguration
                .getInputElementDef(temp[0]);

        // 根据配置文件培植创建一个新的 InputElement 实例
        InputElement ie = def.newInputElement();
        if (ie != null && temp.length > 1) {

            // 设置配置的通用属性g
            String[] props = temp[1].split(";");
            for (int i = 0; i < props.length; i++) {
                String[] kv = props[i].split("=", 2);
                if (kv.length > 1) {

                    if ("value".equalsIgnoreCase(kv[0]) && kv[1].length() >= 2) {
                        if ((kv[1].indexOf("\"") == 0 && kv[1]
                                .lastIndexOf("\"") == (kv[1].length() - 1))
                                || (kv[1].indexOf("'") == 0 && kv[1]
                                .lastIndexOf("'") == (kv[1].length() - 1)))
                            kv[1] = kv[1].substring(1, kv[1].length() - 1);
                    }

                    ie.addKeyValueProp(kv[0], kv[1]);
                } else {

                    ie.addValueProp(props[i]);
                }
            }

            // 设置特殊属性
            if (ie instanceof SelectInputElement) {
                SelectInputElement sie = (SelectInputElement) ie;
                List list = new ArrayList();
                try {
                    Map<String, String> map = jobConfiguration
                            .getProps("option");
                    if (map != null && map.size() > 0) {
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            SelectOptionItem so = new SelectOptionItem(key,
                                    value);
                            list.add(so);
                        }
                    }
                    sie.setOptions(list);
                } catch (Exception ex) {

                }
            }
        }

        return ie;
    }


    public EemTempEntity getTemEntity(Long formId) throws DAOException {
        List<EemTempEntity> list = baseDAO.find("from EemTempEntity a where a.objectId=" + formId);
        EemTempEntity nt = null;
        if (list != null && list.size() > 0) {
            nt = list.get(0);
        }
        return nt;
    }

    @Override
    public ExcelPage getExcelPage(Long formId) throws DAOException {
        return (ExcelPage) baseDAO.get(ExcelPage.class, formId);
    }

    public List findExcelPageValueByPageID(List PageIds) throws ServiceException {
        try {
            return baseDAO.find("from ExcelPageValues where pageID in (?)", PageIds.toArray());
        } catch (DAOException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    //----------------------------------------------------------------------华丽丽的分割线xlzhang------------------------------------------------------------------------------------------

    /**
     * @param byteArr
     * @param excelPage
     * @param sheetName  暂且未使用
     * @param withdraw
     * @param userEntity
     * @return
     */
    @Override
    public String saveReportData(byte[] byteArr, ExcelPage excelPage, String sheetName, String withdraw, UserEntity userEntity) {
        /**
         * 1.判断是普通模板还是光缆模板
         * 2.保存上报的excel文件
         * 3.光缆产品供货和综合评价季度表、蝶形光缆综合评价表、蝶形光缆、光缆测试需要特殊处理（即光缆模板需要特殊处理）
         * 4.OLT设备（含EMS）互通商用、FTTH ONU设备互通商用后评价表  特殊处理
         */
        String result = "";
        File file = null;
        try {
            Long fileDataID = baseDAO.getSequenceNextValue(EvaluationFileData.class);
            OrgEntity orgEntity;
            if(excelPage.getApplyId()==2&&userEntity.getCategory().equals("PRO")){
                orgEntity =userEntity.getOrgEntity();
                excelPage.setAuditSameLevel(true);
                excelPage.setWorkOrderStatus("已审核");
            }else {
                excelPage.setAuditSameLevel(false);
                orgEntity = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            }

            String filePath = EemConstants.EVALUATION_FILE_ABSOLUTE_PATH + File.separator + orgEntity.getOrgCode()
                    + File.separator + DateUtils.getStrFromDateYYYYMM(new Date());//要保存的路径
            String fileName = filePath + File.separator + fileDataID + ".xls";//要保存的excel的路径+名字

            file = new File(fileName);

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArr);
            fos.flush();
            fos.close();
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("",null, dataInputStream);
            dataInputStream.close();
            EemTempEntity eemTempEntity = templateService.findTempByID(excelPage.getTpInputID());
            excelPage.setFileName(eemTempEntity.getTempName());
            excelPage.setFilePath(fileName);
            excelPage.setAttachmentId(attachmentId);
            if(org.apache.commons.lang3.StringUtils.isBlank(excelPage.getReportType())){
                excelPage.setReportType("report");
            }
            excelPage.setIswithdraw("");
//            String withdraw = "";//退回后重新上报 yes 表示退回后 重新上报
            if (eemTempEntity.getLevel() == 3) {//互通商用后评价表
                Workbook wbsy = null;
                InputStream issy = new FileInputStream(file);
                wbsy = Workbook.getWorkbook(issy);
                result = this.moreSheetsCheck(wbsy);//返回值
                if (org.apache.commons.lang3.StringUtils.isNotBlank(result)) {
                    issy.close();
                    wbsy.close();
                }
                result = this.importEvaluationFileDataByWithDraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, withdraw);// 保存具体数据
            } else if (eemTempEntity.getLevel() == 2) {//光缆
                List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + orgEntity.getOrgCode() + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID()+" and applyId="+excelPage.getApplyId());
                Workbook wb = null;
                InputStream is = new ByteArrayInputStream(byteArr);
                wb = Workbook.getWorkbook(is);
                List<String> pagenameList = new ArrayList<String>();
                Sheet[] sheetArray = wb.getSheets();
                for (Sheet sheet : sheetArray) {
                    Cell cell = sheet.getCell(2, 1);
                    String content = cell.getContents();
                    if (pagenameList.contains(content)) {
                        result = "有重复的厂家，请检查后重新上报！";
                    } else {
                        pagenameList.add(content);
                    }
                }
                List<String> withDrawPageName = new ArrayList<String>();
                String isWithDrawStr = "";
                for (String pageName : pagenameList) {
                    if (currentList != null && currentList.size() != 0) {
                        for (ExcelPage page : currentList) {
                            logger.info("!!!!!!!!!!" + page.getPageName());
                            if (pageName.equals(page.getPageName())) {
                                withDrawPageName.add(page.getPageName());
                                break;
                            } else {
                                isWithDrawStr = "err";
                                break;
                            }
                        }
                    }
                    if (isWithDrawStr.equals("err")) {
                        break;
                    }
                }
                if (isWithDrawStr.equals("err")) {
                    result = "数据导入时系统出现异常，请检查导入表格是否与系统提供模板格式一致。";
                }

                result = this.importEvaluationFileDataForType2Withdraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, withdraw, withDrawPageName, isWithDrawStr, currentList);// 保存具体数据
                is.close();
            } else {//普通模板
                result = this.importEvaluationFileDataByWithDraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, withdraw);// 保存具体数据
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(result)) {
                EvaluationFileData efd = new EvaluationFileData();
                efd.setFileID(fileDataID);
                efd.setFilePath(filePath);
                efd.setFileName(file.getName());
                efd.setUploadDate(new Date());
                efd.setReportOrgCode(orgEntity.getOrgCode());
                efd.setReportPersonName(userEntity.getTrueName());
                efd.setReportPersonID(userEntity.getUserId());
                efd.setReportPersonTel(excelPage.getOperUserPhone());
                baseDAO.saveOrUpdate(efd);
            }
        } catch (Exception e) {
            String msg = "";
            result = "系统内部运行异常";
            e.printStackTrace();
        } finally {
            if(file!=null){
                file.delete();
            }
            return result;
        }
    }



    @Override
    public String saveReportData(byte[] byteArr, ExcelPage excelPage, UserEntity userEntity) {
        /**
         * 1.判断是普通模板还是光缆模板
         * 2.保存上报的excel文件
         * 3.光缆产品供货和综合评价季度表、蝶形光缆综合评价表、蝶形光缆、光缆测试需要特殊处理（即光缆模板需要特殊处理）
         * 4.OLT设备（含EMS）互通商用、FTTH ONU设备互通商用后评价表  特殊处理
         */
        String result = "";
        File file = null;
        try {
            Long fileDataID = baseDAO.getSequenceNextValue(EvaluationFileData.class);
            OrgEntity orgEntity;
            if(excelPage.getApplyId()==2&&userEntity.getCategory().equals("PRO")){
                orgEntity =userEntity.getOrgEntity();
            }else {
                orgEntity = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            }

            String filePath = EemConstants.EVALUATION_FILE_ABSOLUTE_PATH + File.separator + orgEntity.getOrgCode()
                    + File.separator + DateUtils.getStrFromDateYYYYMM(new Date());//要保存的路径
            String fileName = filePath + File.separator + fileDataID + ".xls";//要保存的excel的路径+名字

            file = new File(fileName);

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArr);
            fos.flush();
            fos.close();
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("",null, dataInputStream);
            dataInputStream.close();
            EemTempEntity eemTempEntity = templateService.findTempByID(excelPage.getTpInputID());
            excelPage.setFileName(eemTempEntity.getTempName());
            excelPage.setFilePath(fileName);
            excelPage.setAttachmentId(attachmentId);
            if(org.apache.commons.lang3.StringUtils.isBlank(excelPage.getReportType())){
                excelPage.setReportType("report");
            }
//            String withdraw = "";//退回后重新上报 yes 表示退回后 重新上报
            if (eemTempEntity.getLevel() == 3) {//互通商用后评价表
                Workbook wbsy = null;
                InputStream issy = new FileInputStream(file);
                wbsy = Workbook.getWorkbook(issy);
                result = this.moreSheetsCheck(wbsy);//返回值
                if (org.apache.commons.lang3.StringUtils.isNotBlank(result)) {
                    issy.close();
                    wbsy.close();
                }
                result = this.importEvaluationFileDataByWithDraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, "");// 保存具体数据
            } else if (eemTempEntity.getLevel() == 2) {//光缆
                List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + orgEntity.getOrgCode() + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID()+" and applyId="+excelPage.getApplyId());
                Workbook wb = null;
                InputStream is = new ByteArrayInputStream(byteArr);
                wb = Workbook.getWorkbook(is);
                List<String> pagenameList = new ArrayList<String>();
                Sheet[] sheetArray = wb.getSheets();
                for (Sheet sheet : sheetArray) {
                    Cell cell = sheet.getCell(2, 1);
                    String content = cell.getContents();
                    if (pagenameList.contains(content)) {
                        result = "有重复的厂家，请检查后重新上报！";
                    } else {
                        pagenameList.add(content);
                    }
                }
                List<String> withDrawPageName = new ArrayList<String>();
                String isWithDrawStr = "";
                for (String pageName : pagenameList) {
                    if (currentList != null && currentList.size() != 0) {
                        for (ExcelPage page : currentList) {
                            logger.info("!!!!!!!!!!" + page.getPageName());
                            if (pageName.equals(page.getPageName())) {
                                withDrawPageName.add(page.getPageName());
                                break;
                            } else {
                                isWithDrawStr = "err";
                                break;
                            }
                        }
                    }
                    if (isWithDrawStr.equals("err")) {
                        break;
                    }
                }
                if (isWithDrawStr.equals("err")) {
                    result = "数据导入时系统出现异常，请检查导入表格是否与系统提供模板格式一致。";
                }

                result = this.importEvaluationFileDataForType2Withdraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, "", withDrawPageName, isWithDrawStr, currentList);// 保存具体数据
                is.close();
            } else {//普通模板
                result = this.importEvaluationFileDataByWithDraw(orgEntity.getOrgCode(),
                        fileDataID, byteArr, eemTempEntity, userEntity,
                        excelPage, "");// 保存具体数据
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(result)) {
                EvaluationFileData efd = new EvaluationFileData();
                efd.setFileID(fileDataID);
                efd.setFilePath(filePath);
                efd.setFileName(file.getName());
                efd.setUploadDate(new Date());
                efd.setReportOrgCode(orgEntity.getOrgCode());
                efd.setReportPersonName(userEntity.getTrueName());
                efd.setReportPersonID(userEntity.getUserId());
                efd.setReportPersonTel(excelPage.getOperUserPhone());
                baseDAO.saveOrUpdate(efd);
            }
        } catch (Exception e) {
            String msg = "";
            result = "系统内部运行异常";
            e.printStackTrace();
        } finally {
            if(file!=null){
                file.delete();
            }
            return result;
        }
    }




    public String importEvaluationFileDataByWithDraw(String reportCode, Long fileDataTid, byte[] fileByteArray, EemTempEntity tempEntity, UserEntity user, ExcelPage excelPage, String withdraw) throws Exception {
        String result = "";
        Workbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = Workbook.getWorkbook(is);
        ExcelConverter2 ec = new ExcelConverter2();
        Set<ExcelPageValues> set = null;
        String sql = "SELECT t.type from t_eem_report t where  t.shortName like '%" + tempEntity.getShortName() + "%'";
        String strType = null;
        try {
            List<Map> type = baseDAO.findNativeSQL(sql, null);
            if (type.size() > 0) {
                for (Map map : type) {
                    strType = map.get("type").toString();
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        Sheet[] sheetArray = wb.getSheets();
        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(reportCode);

        for (Sheet sheet : sheetArray) {
            String sheetName = sheet.getName();
//            if(sheetName.equals("填表说明")||sheetName.contains("问题说明表")||!sheetName.equals(tempEntity.getShortName())){
            if(sheetName.equals("填表说明")||sheetName.contains("问题说明表")||(sheetName.contains("Sheet")&&tempEntity.getTempPattern()!=2)){
                continue;
            }
            ExcelPage ep = new ExcelPage();
            ep.setApplyId(excelPage.getApplyId());
            ep.setFileName(excelPage.getFileName());
            ep.setTpInputName(tempEntity.getTempName());
            ep.setCreatedBy(user.getUserId());
            ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ep.setReportOrgCode(reportCode);
            Long pageId = baseDAO.getSequenceNextValue(ExcelPage.class);
            ep.setObjectId(pageId);
            ep.setPageName(sheetName);

            ep.setOperUserId(user.getUserId());
            ep.setOperUserTrueName(user.getTrueName());
            ep.setOperOrgName(orgEntity.getOrgName());
            ep.setOperFullOrgName(orgEntity.getFullOrgName());
            ep.setOperUserPhone(excelPage.getOperUserPhone());
            ep.setTpInputID(tempEntity.getObjectId());
            ep.setReportDate(excelPage.getReportDate());
            ep.setReportYear(excelPage.getReportYear());
            ep.setFileDataId(fileDataTid);
            ep.setDeletedFlag(false);
            ep.setFilePath(excelPage.getFilePath());
            ep.setReportType(excelPage.getReportType());
            if(excelPage.getAuditSameLevel()){
                ep.setWorkOrderStatus("已审核");
                ep.setAuditSameLevel(true);
            }else{
                ep.setWorkOrderStatus("未审核");
                ep.setAuditSameLevel(false);
            }
            ep.setAuperiorAudit(false);
            ep.setAttachmentId(excelPage.getAttachmentId());
            set = ec.analysisJobContentDone(tempEntity, sheet, tempEntity.getObjectId(), pageId,strType);
            if (withdraw.equals("yes")) {
                ep.setIswithdraw("");
                ep.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID() + " and tpInputName='" + ep.getTpInputName() + "'"+" and applyId="+ep.getApplyId());
                for (ExcelPage page : currentList) {
                    deletePageValuse(page);
                    page.setDeletedFlag(true);
                }
                baseDAO.deleteAll(currentList, user);
            }else if (!withdraw.equals("yes") && set!=null) {
                List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID()+" and applyId="+excelPage.getApplyId());
                for (ExcelPage page : currentList) {
                    deletePageValuse(page);
                    page.setDeletedFlag(true);
                }
                baseDAO.deleteAll(currentList, user);
            }else{
                return "后评价总分有重复项，请查证后手动上报！";
            }
            result+= importEvaluationFileDataForEachSheet(sheet, tempEntity, ep, "1");
        }
        return result;
    }

    public String importEvaluationFileDataForType2Withdraw(String reportCode, Long fileDataTid, byte[] fileByteArray, EemTempEntity tempEntity, UserEntity user, ExcelPage excelPage, String withdraw, List<String> withDrawPageName, String isWithDrawStr, List<ExcelPage> excelPageList)
            throws Exception {
        String result = "";
        Workbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = Workbook.getWorkbook(is);
        Sheet[] sheetArray = wb.getSheets();
        if (!withdraw.equals("yes")) {
            if (excelPageList != null && excelPageList.size() > 0) {
                for (ExcelPage deletePage : excelPageList) {
                    deletePageValuse(deletePage);
                    deletePage.setDeletedFlag(true);
                }
                baseDAO.deleteAll(excelPageList, user);
            }
        }
        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(reportCode);
        for (Sheet sheet : sheetArray) {
            String pageName = sheet.getCell(2, 1).getContents();
            if (!"".equals(pageName) && pageName != null) {
                ExcelPage ep = new ExcelPage();
                ep.setApplyId(excelPage.getApplyId());
                ep.setFileName(excelPage.getFileName());
                ep.setCreatedBy(user.getUserId());
                ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
                ep.setReportOrgCode(reportCode);
                Long pageId = baseDAO.getSequenceNextValue(ExcelPage.class);
                ep.setTpInputName(tempEntity.getTempName() + "-" + pageName);
                ep.setObjectId(pageId);
                ep.setPageName(pageName);
                ep.setOperUserId(user.getUserId());
                ep.setOperUserTrueName(user.getTrueName());
                ep.setOperOrgName(orgEntity.getOrgName());
                ep.setOperFullOrgName(orgEntity.getFullOrgName());
                ep.setOperUserPhone(excelPage.getOperUserPhone());
                ep.setTpInputID(tempEntity.getObjectId());
                ep.setReportDate(excelPage.getReportDate());
                ep.setReportYear(excelPage.getReportYear());
                ep.setFileDataId(fileDataTid);
                if(excelPage.getAuditSameLevel()){
                    ep.setWorkOrderStatus("已审核");
                    ep.setAuditSameLevel(true);
                }else{
                    ep.setWorkOrderStatus("未审核");
                    ep.setAuditSameLevel(false);
                }
//                ep.setWorkOrderStatus("未审核");
//                ep.setAuditSameLevel(false);
                ep.setAuperiorAudit(false);
                ep.setFilePath(excelPage.getFilePath());
                ep.setReportType(excelPage.getReportType());
                ep.setDeletedFlag(false);
                ep.setAttachmentId(excelPage.getAttachmentId());
                if (withdraw.equals("yes")) {
                    ep.setIswithdraw("");
                    ep.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                    List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID() + " and tpInputName='" + ep.getTpInputName() + "'"+" and applyId="+ep.getApplyId());
                    for (ExcelPage page : currentList) {
                        deletePageValuse(page);
                        page.setDeletedFlag(true);
                    }
                    baseDAO.deleteAll(currentList, user);
                }
                result+= importEvaluationFileDataForEachSheet(sheet, tempEntity, ep, "2");
            } else {
                throw new Exception();
            }
        }
        return result;
    }

    public String importEvaluationFileDataForEachSheet(Sheet sheet, EemTempEntity tempEntity, ExcelPage ep, String formType)
            throws Exception {
        logger.info("##########################" + sheet.getName());
        long pageId = 0;// 获得导入ExcelPage的formid 以及pageid
        pageId = ep.getObjectId();

        if(tempEntity.getLevel()!=3){
            boolean isLicit = this.checkFileIsLicit(sheet, tempEntity.getTemplateExcelByteData().getUploadFileData());
            if (!isLicit) {
                return "与系统提供模板不一致，请下载最新模板上报数据！";
            }
        }
        String sql = "SELECT t.type from t_eem_report t where  t.shortName like '%" + tempEntity.getShortName() + "%'";
        String strType = null;
        try {
            List<Map> type = baseDAO.findNativeSQL(sql, null);
            if (type.size() > 0) {
                for (Map map : type) {
                    strType = map.get("type").toString();
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        // 在保存数据之前先删掉 本部门 该月 改formid的page数据end
//        baseDAO.saveOrUpdate(ep);
        ExcelConverter2 ec = new ExcelConverter2();
        Set<ExcelPageValues> set = null;
        Workbook wb = null;

        set = ec.analysisJobContentDone(tempEntity, sheet, tempEntity.getObjectId(), pageId,strType);// 根据导入excel的sheet获得ExcelPageValues的集合

        if (set != null && set.size() > 0) {
            for (ExcelPageValues excelPageValues : set) {
                excelPageValues.setObjectId(baseDAO.getSequenceNextValue(ExcelPageValues.class));
            }
            baseDAO.saveOrUpdate(ep);
            baseDAO.saveOrUpdateAll(set);
        }else{
            return "后评价总分有重复项，请查证后手动上报！";
        }
        if (wb != null) {
            wb.close();
        }
        return "";
    }

    private void deletePageValuse(ExcelPage excelPage) {
        try {
            baseDAO.executeSql("delete from t_eem_excel_page_values where pageID=" + excelPage.getObjectId());
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param wb
     * @return
     * @throws Exception
     */
    private String moreSheetsCheck(Workbook wb) throws Exception {
        String result = "";
        Sheet[] sheets = wb.getSheets();
        // int colCount=0;
        int rowCount = 0;
        // 有多少个sheet页
        int sheetsCount = sheets.length;
        for (Sheet sheet : sheets) {
            // 填表说明的sheet页不做校验
            if (sheet.getName().equals("填表说明")) {
                continue;
            } else if (sheet.getName().contains("互通商用后评价表")) {
                // colCount=sheet.getColumns();
                // -4是减去4个备注的信息
                rowCount = sheet.getRows() - 4;
                int col = colBegin;
                // 刨去前两个sheet页不算
                for (int j = 2; j < sheetsCount; j++) {
                    for (int i = rowBegin, z = 0; i < rowCount; z++) {
                        Cell cell = sheet.getCell(col, i);
                        String value = cell.getContents();
                        if (!"".equals(value) && value != null) {
                            Sheet tosheet = wb.getSheet(j);
                            if (!sheet.getCell(colBegin - 1, i).getContents()
                                    .trim().equals("总分")) {
                                result += compareWithOtherSheet(tosheet, value,
                                        z);
                            }

                            i = i + 2;
                        } else {
                            i = i + 2;
                        }
                    }
                    col++;

                }

            } else {
                continue;
            }
        }
        return result;
    }

    private String compareWithOtherSheet(Sheet toSheet, String value, int questionNo) throws Exception {
        String result = "";
        // 说明表开始解析的行数
        int rowStart = 4 + 12 * questionNo;
        // 说明表开始解析的列数
        int colStart = 2;
        // 应该填写的问题个数
       // int rowCounts = Integer.parseInt(value);
        int rowCounts=0;
        try{
            rowCounts=Integer.parseInt(value);

        }catch(Exception e){

        }
        // 获得厂家的名字
        // String company = tosheet.getCell(0, rowStart).getContents();

        for (int i = rowStart, j = 0; j < rowCounts; j++, i++) {
            String cellValue = toSheet.getCell(colStart, i).getContents();
            if ("".equals(cellValue) || cellValue == null) {
                result += "请在" + toSheet.getName() + "中填写" + rowCounts
                        + "条问题说明从第" + (rowStart + 1) + "行开始填写;";
            }
        }
        return result;

    }

    // 判断上传的excel是不是我们总部提供的 是否是合法的
    private boolean checkFileIsLicit(Sheet uploadSheet, byte[] sysFile)
            throws IOException, BiffException {
        InputStream sysFileIs = new ByteArrayInputStream(sysFile);
        Workbook sysFilewb = Workbook.getWorkbook(sysFileIs);
        Sheet sysSheet = sysFilewb.getSheet(0);
        // 这里是针对邵岩增加的两个导入模板，因为之前的模板均是一个sheet页，所以程序上直接获取第一个sheet页
        // 现在的上报数据在第二个模板所以如果第一个sheet页 的名为填表说明，则继续向后取第二个sheet页
        if (sysSheet.getName().equals("填表说明")) {
            sysSheet = sysFilewb.getSheet(1);
        }
        String[] testArray = {"0,0", "0,5", "0,10", "0,15", "0,20", "0,25",
                "0,30", "0,35", "0,40", "1,0", "1,5", "1,10", "1,15", "1,20",
                "1,25", "1,30", "1,35", "1,40", "3,0", "7,0", "15,0", "20,0",
                "25,0", "30,0", "35,0"};
        boolean isLicit = true;
        for (String checkPosition : testArray) {
            if (checkPosition != null && !"".equals(checkPosition)) {
                String[] checkPoint = checkPosition.split(",");
                int x = StringUtils.fromStringToInt(checkPoint[0]);
                int y = StringUtils.fromStringToInt(checkPoint[1]);
                String uploadFileContents = "";
                String sysFileContents = "";
                try {
                    Cell uploadFilec = uploadSheet.getCell(x, y);
                    Cell sysFilec = sysSheet.getCell(x, y);
                    uploadFileContents = uploadFilec.getContents();
                    sysFileContents = sysFilec.getContents();
                } catch (ArrayIndexOutOfBoundsException e) {
                    // 检测时出现异常 可以忽略此异常 有可能给的测试值 有问题
                    continue;
                }

                if (sysFileContents != null && !"".equals(sysFileContents)) {
                    if (!uploadFileContents.trim().equals(sysFileContents.trim())&&!sysFileContents.equals("##text:value=#")) {
                        isLicit = false;
                        logger.info("与系统提供模板不一致，请下载最新模板上报数据！");
                        if (sysFilewb != null) {
                            sysFilewb.close();
                        }

                        return isLicit;
                    }
                }
            }
        }
        if (sysFilewb != null) {
            sysFilewb.close();
        }
        return isLicit;
    }

    /**
     * 检查该模板某年某季度/半年 数据是否已经上报
     *
     * @param excelPage
     * @param userEntity
     * @return
     */
    @Override
    public String hasPowerToSave(ExcelPage excelPage, UserEntity userEntity) {
        try {
            String reportCode;
            if(excelPage.getApplyId()==2&&userEntity.getCategory().equals("PRO")){
                reportCode = userEntity.getOrgEntity().getOrgCode();
            }else {
                reportCode  = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            }
//            EemTempEntity eemTempEntity = templateService.findTempByID(excelPage.getTpInputID());
           // List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID()+" and isWithDraw='" + excelPage.getIswithdraw() + "' order by objectId desc");
            List<ExcelPage> currentList = baseDAO.find("from ExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID()+" and DELETED_FLAG = 0 order by objectId desc");

            if (currentList != null && currentList.size() > 0) {//已经上报过或退回了
                String isWithDraw = null;
                for (int i=0; i<currentList.size();i++) {
                    if ("Y".equals(currentList.get(0).getIswithdraw())) {
                        isWithDraw = "Y";
                    }
                    if ("yes".equals(currentList.get(0).getIswithdraw())) {
                        isWithDraw = "Y";
                    }
                    break;
                }
                if (org.apache.commons.lang3.StringUtils.isBlank(isWithDraw)) {
                    if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                        if (hasDatePower()) {
                            return "hasReportedShouldOverride";
                        } else {
                            return "reportDateError";//不在上报日期内
                        }
                    } else {
                        return "hasReportedShouldOverride";
                    }
                } else {
                    return "withdraw";
                }
            } else {//未上报过
                return "ok";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @Override
    public String hasPowerToSave2(ExcelPage excelPage, UserEntity userEntity) {
        String result = "ok";
        try {
            String reportCode = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            List<ExcelPage> currentList = baseDAO.find("from ExcelPage where deletedFlag=0 and reportOrgCode like '" + reportCode + "%' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID());
            List<ExcelPage> cityList = new ArrayList<ExcelPage>();
            List<ExcelPage> proList = new ArrayList<ExcelPage>();
            for (ExcelPage page : currentList) {
                if (page.getReportOrgCode().length() == 3) {
                    proList.add(page);
                } else {
                    cityList.add(page);
                }
            }
            if (proList != null && proList.size() > 0) {//已经上报过或退回了
                String isWithDraw = null;
                for (ExcelPage page : proList) {
                    if ("Y".equals(page.getIswithdraw())) {//判断是否存在退回
                        isWithDraw = "Y";
                        break;
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isBlank(isWithDraw)) {
                    if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                        if (hasDatePower()) {
                            result = "hasReportedShouldOverride";
                        } else {
                            result = "reportDateError";//不在上报日期内
                        }
//                    } else {
//                        result = "hasReportedShouldOverride";
                    }
                } else {
                    result = "withdraw";
                }
            } else {//未上报过
                result = "ok";
        /*        if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                    if (hasDatePower()) {
                        result = "ok";
                    } else {
                        result = "reportDateError";//不在上报日期内
                    }
                } else {
                    result = "ok";
                }*/
            }
          if(org.apache.commons.lang3.StringUtils.isBlank(result)||result.equals("ok")){
              for (ExcelPage page : cityList) {
                  if (!page.getDeletedFlag() && !page.getAuperiorAudit().equals(true)) {
                      result = "有地市数据未审核";
                      break;
                  }
              }
          }
//            if(cityList.size()==0){
//                result = "所有地市都未上报数据，不能进行汇总并上报";
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean hasDatePower() {
        int currentDate = Integer.parseInt(DateUtils.getDateString(new Date(),
                "dd"));
        int currentMonth = Integer.parseInt(DateUtils.getDateString(new Date(),
                "MM"));

        boolean isReportDate = false;

        if (1 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;
            }
        } else if (7 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;
            }
        }
     /*   else if (4 == currentMonth) {
            if (currentDate <= 20) {
                isReportDate = true;
            }
        } else if (7 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;
            }
        } else if (10 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;

            }
        }*/
        return isReportDate;
    }

    // 光缆类型总部汇总导入
    public void importEvaluationFileDataForCable(long fileDataTid,
                                                 byte[] fileByteArray, EemTempEntity eemTempEntity,
                                                 UserEntity user, String reportCode, String reportDate, String reportYear) throws Exception {

        HSSFWorkbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = new HSSFWorkbook(is);
        int sheetCount = wb.getNumberOfSheets();
        //todo  删除上次该季度上报记录
        /*List<EvaluationPage> list = evaluationDao
                .getPageByReportDateAndFormIdAndWillCollect(reportDateStr,
                        reportYear, formidstr, "");
        if (list != null && list.size() > 0) {
            for (EvaluationPage deletePage : list) {
                evaluationDao.deletePageAndValues((int) deletePage.getPageId());
            }
        }*/

        for (int i = 0; i < sheetCount; i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            String pageName = sheet.getSheetName();
            ExcelPage ep = new ExcelPage();
            ep.setOperUserTrueName(user.getTrueName());
            ep.setReportOrgCode(reportCode);
            ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ep.setObjectId(baseDAO.getSequenceNextValue(ExcelPage.class));
            ep.setPageName(pageName);
            ep.setOperUserPhone(user.getMobilePhone());
            ep.setTpInputID(eemTempEntity.getObjectId());
            if (reportDate == null || reportDate.equals("")) {
                ep.setReportDate("全年");
            } else {
                ep.setReportDate(reportDate);
            }
            ep.setReportYear(reportYear);
            ep.setFileDataId(fileDataTid);
            // 总部汇总光缆时设置Iswithdraw，为了在正常汇总时，pageName不包括总部汇总的
            ep.setIswithdraw("static");
            baseDAO.saveOrUpdate(ep);
        }

    }
}
