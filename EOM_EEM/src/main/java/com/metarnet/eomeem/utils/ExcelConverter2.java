package com.metarnet.eomeem.utils;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.ExcelPageValues;
import com.metarnet.eomeem.model.ExcelPageValues2;
import com.metarnet.eomeem.utils.excel.InputElement;
import com.metarnet.eomeem.utils.excel.SelectInputElement;
import com.metarnet.eomeem.utils.excel.SelectOptionItem;
import com.metarnet.eomeem.vo.NameValue;
import jxl.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.Font;
import jxl.format.*;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.web.util.HtmlUtils;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

//import org.springframework.util.StringUtils;

public class ExcelConverter2 implements IExcelConver {

    private double widthSizeUnit = 32;

    private double heightSizeUnit = 28;

    @Resource
    private IBaseDAO baseDAO;

    private Logger logger = Logger.getLogger(ExcelConverter2.class);

    private String analysis(InputStream in) {
        Workbook wb = null;
        try {
            WorkbookSettings wbs = new WorkbookSettings();
            wbs.setEncoding("utf-8");
            wb = Workbook.getWorkbook(in, wbs);
            Sheet sheet = wb.getSheet(0);
            return analysisSheet(sheet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wb != null)
                wb.close();
        }
        return "";
    }

    private String analysisSheet(Sheet sheet) {
        // 总 行、列 数
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();

        // 访问 巨阵, 长、宽巨阵
        Point[][] matrix = this.createAccessFlagMatrix(rowCount, colCount);
        int[][] rowHeights = new int[rowCount][colCount];
        int[][] colWidths = new int[rowCount][colCount];

        // 设置巨阵
        // 更改访问巨阵标志
        Range[] ranges = sheet.getMergedCells();
        for (int i = 0; i < ranges.length; i++) {
            changeAccessFlag(matrix, ranges[i]);
        }
        changeSizeMatrixValue(sheet, matrix, rowHeights, colWidths);

        // 创建FORM
        JobForm jobForm = new JobForm();
        for (int i = 0; i < rowCount; i++) {
            // 增加行
            jobForm.createAndAppendFormRow(i);
            for (int j = 0; j < colCount; j++) {
                if (matrix[i][j] == null)
                    continue;

                Cell cell = sheet.getCell(j, i);
                JobFormCell formCell = analysisCell(cell, j);

                // //设置CELL 长宽
                formCell.setHeight((int) (rowHeights[i][j] / this.heightSizeUnit));
                formCell.setWidth((int) (colWidths[i][j] / this.widthSizeUnit));
                formCell.setRowspan((int) matrix[i][j].getX());
                formCell.setColspan((int) matrix[i][j].getY());
                // 增加CELL
                jobForm.appendFormCell(i, formCell);
            }

        }
        return jobForm.toString();
    }

    private JobFormCell analysisCell(Cell cell, int j) {
        // 创建一个新的 JobFormCell
        JobFormCell formCell = new JobFormCell(j);
        if (cell == null) {
            return formCell;
        }

        // 获取格 内容，设置内容
        String cellText = cell.getContents();
        if (cellText != null && !cell.getContents().trim().equals("")) {
            // 获取CELL内容种类，如果是文本型，进行分析
            if (cell.getType() == CellType.LABEL)
                analysisCellContents(formCell, cellText);
            else {
                if (cell.getType() == CellType.NUMBER) {
                    NumberCell numc = (NumberCell) cell;
                    formCell.setBeforeInputText(cellText);
                } else {
                    if (cell.getType() == CellType.DATE
                            || cell.getType() == CellType.DATE_FORMULA) {
                        DateCell dc = (DateCell) cell;
                        SimpleDateFormat sdf = new SimpleDateFormat("");
                        formCell.setBeforeInputText(sdf.format(new Date()));
                    } else {
                        formCell.setBeforeInputText(cellText);
                    }
                }
            }

            CellFormat cellFormat = cell.getCellFormat();

            // 获取格 字体
            Font font = cellFormat.getFont();
            // 设置 字体

            String fontName = font.getName();
            formCell.setFont(fontName);

            // 获取字体大小, 设置字体大小

            formCell.setSize(font.getPointSize());

            // 获取格 颜色
            Colour color = font.getColour();

            if (color != null)
                formCell.setColor(color.getDefaultRed(), color
                        .getDefaultGreen(), color.getDefaultBlue());

            // 获取格 背景颜色
            color = cellFormat.getBackgroundColour();

            if (color != null)
                formCell.setBackgroundColor(color.getDefaultRed(), color
                        .getDefaultGreen(), color.getDefaultBlue());

            // 获取 并 设置 是否是斜体

            formCell.setItalic(font.isItalic());

            // 获取 并 设置 是否是粗体体

            int bold = font.getBoldWeight();
            if (bold > 600)
                formCell.setBold(true);
            // 获取格 风格 -- 对齐、长宽）
            Alignment align = cellFormat.getAlignment();
            VerticalAlignment valign = cellFormat.getVerticalAlignment();
            // 设置水平风格
            if (align.getValue() == Alignment.CENTRE.getValue())
                formCell.setAlign(JobFormCell.ALIGN_CENTER);
            else if (align.getValue() == Alignment.RIGHT.getValue())
                formCell.setAlign(JobFormCell.ALIGN_RIGHT);
            else
                formCell.setAlign(JobFormCell.ALIGN_LEFT);
            // 设置垂直风格
            if (valign.getValue() == VerticalAlignment.TOP.getValue())
                formCell.setValign(JobFormCell.VALIGN_TOP);
            else if (valign.getValue() == VerticalAlignment.BOTTOM.getValue())
                formCell.setValign(JobFormCell.VALIGN_BOTTOM);
            else
                formCell.setValign(JobFormCell.VALIGN_CENTER);

        }
        return formCell;
    }

    private void analysisCellContents(JobFormCell formCell, String cellText) {

        JobConfiguration jobConfiguration = (JobConfiguration) SpringContextUtils
                .getBean("jobConfiguration");

        cellText = StringUtils
                .replace(cellText, "&", HtmlUtils.htmlEscape("&"));
        cellText = StringUtils
                .replace(cellText, "<", HtmlUtils.htmlEscape("<"));
        cellText = StringUtils
                .replace(cellText, ">", HtmlUtils.htmlEscape(">"));
        String[] temp = cellText.split(jobConfiguration.getInputPromptBegin(),
                2);
        if (temp.length > 1 && temp[1] != null) {
            formCell.setBeforeInputText(temp[0]);
            temp = temp[1].split(jobConfiguration.getInputPromptEnd(), 2);
            if (temp.length > 1) {
                formCell.setAfterInputText(temp[1]);
            }
            formCell.setInput(temp[0]);
            formCell.setStyleClass("form_content");
        } else {
            formCell.setBeforeInputText(cellText);
            formCell.setStyleClass("excel_table_name");
        }

    }

    private Point[][] createAccessFlagMatrix(int r, int c) {
        Point[][] matrix = new Point[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                matrix[i][j] = new Point(1, 1);
        return matrix;
    }

    private void changeAccessFlag(Point[][] matrix, Range range) {
        int r = range.getTopLeft().getRow();
        int c = range.getTopLeft().getColumn();
        int rowspan = range.getBottomRight().getRow() - r + 1;
        int colspan = range.getBottomRight().getColumn() - c + 1;
        this.changeAccessFlag(matrix, r, c, rowspan, colspan);

    }

    private void changeAccessFlag(Point[][] flags, int r, int c, int rowspan,
                                  int colspan) {

        for (int i = 0; i < rowspan && (r + i) < flags.length; i++) {
            for (int j = 0; j < colspan && (c + j) < flags[i].length; j++) {

                flags[r + i][c + j] = null;
            }
        }
        flags[r][c] = new Point(rowspan, colspan);
    }

    private void changeSizeMatrixValue(Sheet sheet, Point[][] matrix,
                                       int[][] rowHeights, int[][] colWidths) {
        if (matrix == null) {
            return;
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {

                if (matrix[i][j] == null)
                    continue;
                // 设置该点长宽值
                int rows = (int) matrix[i][j].getX();
                int cols = (int) matrix[i][j].getY();
                for (int ti = 0; ti < rows; ti++) {
                    rowHeights[i][j] += sheet.getRowView(i + ti).getSize();
                }
                for (int ti = 0; ti < cols; ti++) {
                    colWidths[i][j] += sheet.getColumnView(j + ti).getSize();
                }
            }
        }
    }

    /*
     * 根据导入excel的sheet获得ExcelPageValues的集合
     * byteArray:模板格式
     * jobContentExcelSheet：excel对应sheet
     * formId:模板Id
     * pageId:pageValue对应的pageId
     */
    public Set analysisJobContentDonePoi(byte[] byteArray, HSSFSheet jobContentExcelSheet, long formId, long pageId)throws Exception {
        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");
        int rowCount = 0;
        int colCount = 0;
        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);
            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");
            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();
                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                String text = td.getText();
                if (StringUtils.isNotEmpty(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        jobContentExcelSheet.setForceFormulaRecalculation(true);
                        HSSFRow row = jobContentExcelSheet.getRow(rowCount);
                        HSSFCell cell = null;
                        try{
                            cell = row.getCell(currentColCount);//此时需要捕获异常，否则此处空指针异常不抛出不捕获，会一直卡在汇总环节
                        }catch (Exception e){
//							e.printStackTrace();
                            logger.info("***************此处有异常,currentColCount---> " + currentColCount);
                            logger.error(e);
                        }

                        if (cell == null) {
                            continue;
                        }
                        String cellText = "";
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            cellText = cell.getStringCellValue();
                            //日期和数值都是CELL_TYPE_NUMERIC
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否是日期格式
                                DateCell dc = (DateCell) cell;
                                if (dc != null && dc.getDateFormat() != null && dc.getDate() != null) {
                                    Date comparedDate = new Date();
                                    try {
                                        comparedDate = new SimpleDateFormat("yyyy-MM-dd").parse("1949-01-01");
                                    } catch (Exception e) {
                                        comparedDate = new Date(1901, 1, 1);
                                    }
                                    if (dc.getDate().before(comparedDate)){
                                        cellText = dc.getDateFormat().format(dc.getDate());
                                    }else{
                                        cellText = new SimpleDateFormat("yyyy-MM-dd").format(dc.getDate());
                                    }

                                }
                            } else {// 数值格式
                                double intcellText = cell.getNumericCellValue();
                                cellText = String.valueOf(intcellText);
                            }
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                            try {
                                cellText = String.valueOf(cell.getNumericCellValue());
                            } catch (Exception e) {
                                cellText = String.valueOf(0);
                            }
                        } else if (cell.getCellType()== HSSFCell.CELL_TYPE_ERROR) {
                            cellText = String.valueOf(cell.getErrorCellValue());
                        }
						/*EvaluationPageValues epv=new EvaluationPageValues();
						epv.setRowindex(String.valueOf(rowCount));
						epv.setColindex(String.valueOf(currentColCount));
						if (text.contains("_server")) {
							epv.setAttribute1("server");
						}
						if (cellText != null) {
							cellText = cellText.replace(",", "");
						}
						epv.setTxtvalue(cellText);
						epv.setFormid(formId);
						epv.setPageid(pageId);
						set.add(epv);*/
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount: colCount;
            }
        }

        return set;
    }

    // 按权重解析文件
    public Set analyJobContentByWeight(byte[] byteArray,
                                       HSSFSheet jobContentExcelSheet, HSSFSheet pvSheet, long formId,
                                       long pageId) throws Exception {
        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        if (doc == null)
            return set;

        // 获取所有TR

        Iterator trList = doc.getRootElement().elementIterator("tr");

        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();

            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");

            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();

                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                String text = td.getText();
                if (text != null && !"".equals(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        jobContentExcelSheet.setForceFormulaRecalculation(true);
                        HSSFRow row = jobContentExcelSheet.getRow(rowCount);
                        HSSFCell cell = row.getCell(currentColCount);
                        if (cell == null) {
                            continue;
                        }

                        String cellText = "";
                        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            cellText = cell.getStringCellValue();
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {// 日期和数值都是CELL_TYPE_NUMERIC
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否是日期格式
                                DateCell dc = (DateCell) cell;
                                if (dc != null && dc.getDateFormat() != null && dc.getDate() != null) {
                                    Date comparedDate = new Date();
                                    try {
                                        comparedDate = new SimpleDateFormat("yyyy-MM-dd").parse("1949-01-01");
                                    } catch (Exception e) {
                                        comparedDate = new Date(1901, 1, 1);
                                    }

                                    if (dc.getDate().before(comparedDate)){
                                        cellText = dc.getDateFormat().format(dc.getDate());
                                    }
                                    else{
                                        cellText = new SimpleDateFormat("yyyy-MM-dd").format(dc.getDate());
                                    }

                                }
                            } else {// 数值格式
                                if (text.contains("_server")) {
                                    HSSFRow pvRow = pvSheet.getRow(rowCount);
                                    HSSFCell cellPv = pvRow
                                            .getCell(currentColCount);
                                    if (cellPv == null) {
                                        continue;
                                    }
                                    double pvText = 0;
                                    pvText = cellPv.getNumericCellValue();
                                    double intcellText = cell.getNumericCellValue();
                                    intcellText = intcellText * 0.5 + pvText* 0.5;
                                    cell.setCellValue(intcellText);
                                    cellText = intcellText + "";
                                } else {
                                    cellText = String.valueOf(cell
                                            .getNumericCellValue());
                                }

                            }
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {

                            if (text.contains("_server")) {
                                HSSFRow pvRow = pvSheet.getRow(rowCount);
                                HSSFCell cellPv = pvRow
                                        .getCell(currentColCount);
                                if (cellPv == null) {
                                    continue;
                                }
                                double pvText = 0;
                                double intcellText = 0;
                                try {
                                    pvText = cellPv.getNumericCellValue();
                                    intcellText = cell.getNumericCellValue();
                                } catch (Exception e) {
                                }

                                intcellText = intcellText * 0.5 + pvText * 0.5;
                                cell.setCellValue(intcellText);
                                cellText = intcellText + "";
                            } else {
                                try {
                                    cellText = cell.getNumericCellValue() + "";
                                    cell
                                            .setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                                    // cell.setCellFormula(cell.getCellFormula());
                                } catch (Exception e) {
                                    cellText = String.valueOf(0);
                                }
                            }

                        } else if (cell.getCellStyle().equals(
                                HSSFCell.CELL_TYPE_ERROR)) {
                            cellText = String.valueOf(cell.getErrorCellValue());
                        }

                        // 判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null
                                    && ismust.equals("true")
                                    && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }
						/*EvaluationPageValues epv = new EvaluationPageValues();
						epv.setRowindex(String.valueOf(rowCount));
						epv.setColindex(String.valueOf(currentColCount));

						if (cellText != null) {
							cellText = cellText.replace(",", "");
						}
						epv.setTxtvalue(cellText);
						epv.setFormid(formId);
						epv.setPageid(pageId);
						set.add(epv);*/
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount: colCount;
            }
        }

        return set;
    }

    // 加权预览

    public HSSFSheet analyJobByWeight(byte[] byteArray,
                                      HSSFSheet jobContentExcelSheet, HSSFSheet pvSheet) throws Exception {
        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");
        int rowCount = 0;
        int colCount = 0;
        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);
            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");
            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();
                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                String text = td.getText();
                if (text != null && !"".equals(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        jobContentExcelSheet.setForceFormulaRecalculation(true);
                        HSSFRow row = jobContentExcelSheet.getRow(rowCount);
                        HSSFCell cell = row.getCell(currentColCount);
                        if (cell == null) {
                            continue;
                        }

                        String cellText = "";
                        if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            cellText = cell.getStringCellValue();
                        } else if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {// 日期和数值都是CELL_TYPE_NUMERIC
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否是日期格式
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
                            } else {// 数值格式
                                if (text.contains("_server")) {
                                    HSSFRow pvRow = pvSheet.getRow(rowCount);
                                    HSSFCell cellPv = pvRow.getCell(currentColCount);
                                    if (cellPv == null) {
                                        continue;
                                    }
                                    double pvText = 0;
                                    pvText = cellPv.getNumericCellValue();
                                    double intcellText = cell.getNumericCellValue();
                                    intcellText = intcellText * 0.5 + pvText* 0.5;
                                    cell.setCellValue(intcellText);
                                    cellText = intcellText + "";
                                } else {
                                    cellText = String.valueOf(cell.getNumericCellValue());
                                }

                            }
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {

                            if (text.contains("_server")) {
                                HSSFRow pvRow = pvSheet.getRow(rowCount);
                                HSSFCell cellPv = pvRow.getCell(currentColCount);
                                if (cellPv == null) {
                                    continue;
                                }
                                double pvText = 0;
                                double intcellText = 0;
                                try {
                                    pvText = cellPv.getNumericCellValue();
                                    intcellText = cell.getNumericCellValue();
                                } catch (Exception e) {
                                }

                                intcellText = intcellText * 0.5 + pvText * 0.5;
                                cell.setCellValue(intcellText);
                                cellText = intcellText + "";
                            } else {
                                try {
                                    cellText = cell.getNumericCellValue() + "";
                                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                                } catch (Exception e) {
                                    cellText = String.valueOf(0);
                                }
                            }

                        } else if (cell.getCellStyle().equals(
                                HSSFCell.CELL_TYPE_ERROR)) {
                            cellText = String.valueOf(cell.getErrorCellValue());
                        }

						/*// 判断是否有一些必填字段需要检查
						Properties prop = ie.getKeyValueProps();
						if (prop != null) {
							String ismust = prop.getProperty("ismust");

							if (ismust != null
									&& ismust.equals("true")
									&& (cellText == null || cellText.equals("")))
								throw new Exception("有一些必填字段没有填写，请填写后再提交!");
						}*/
                    }
                }

                colCount = (currentColCount > colCount) ? currentColCount: colCount;
            }
        }

        return jobContentExcelSheet;
    }

    public String fromExcelFileByteArrayToXml(InputStream in) {
        return this.analysis(in);
    }

    // 一个excel对应多个sheet的情况2012 0322
    public List<NameValue> fromExcelFileByteArrayToXmlList(InputStream in,
                                                           byte[] byteArray) {
        return this.analysisSheets(in, byteArray);
    }

    private List<NameValue> analysisSheets(InputStream in,
                                           byte[] byteArray) {

        Workbook wb = null;
        try {
            WorkbookSettings wbs = new WorkbookSettings();
            wbs.setEncoding("GB2312");
            wbs.setWriteAccess(null);
            wb = Workbook.getWorkbook(in, wbs);
            List<NameValue> nameValueList = new ArrayList<NameValue>(); // name 解析后的xml value输入的模板remark sheet的name
            Sheet[] sheetArray = wb.getSheets();
            Workbook wbForInput = Workbook.getWorkbook(
                    new ByteArrayInputStream(byteArray), wbs);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            wbs.setEncoding("GB2312");
            wbs.setWriteAccess(null);
            WritableWorkbook aa = Workbook.createWorkbook(os, wbForInput, wbs);
            aa.write();
            aa.close();
            os.flush();
            byte[] eachSheet = os.toByteArray();
            os.close();
            for (int i = 0; i < sheetArray.length; i++) {
                String sheetName = sheetArray[i].getName();

                String excelName = sheetArray[i].getCell(0, 0).getContents();
                String[] str = excelName.split(EemConstants.SPLIT_STR);
                if (str.length > 1) {
                    String version = str[1]; // 版本号
                    NameValue nv = new NameValue(analysisSheet(sheetArray[i]),
                            version, sheetName, eachSheet);
                    nameValueList.add(nv);
                } else {
                    NameValue nv = new NameValue(analysisSheet(sheetArray[i]),
                            null, sheetName, eachSheet);
                    nameValueList.add(nv);
                }

            }
            return nameValueList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wb != null)
                wb.close();
        }
        return null;
    }

    // 从workbook中删除名字不是sheetName的sheet
    private boolean removeDeffSheet(String sheetName, WritableWorkbook workbook) {
        WritableSheet[] sheetArray = workbook.getSheets();
        boolean hasDeff = false;
        for (int i = 0; i < sheetArray.length; i++) {
            String sheetName2 = sheetArray[i].getName();
            if (!sheetName.equals(sheetName2)) {
                workbook.removeSheet(i);
                hasDeff = true;
                break;
            }
        }
        if (hasDeff == true) {
            return removeDeffSheet(sheetName, workbook);
        } else {
            return hasDeff;
        }

    }

    /**
     * @param doc
     * @return
     */
    // //
    public String visit(Document doc, List list, boolean isStatic) {
        if (doc == null)
            return null;
        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");

        int rowCount = 0;
        int colCount = 0;
        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            rowCount++;
            // 获取该TR的ID
            String trId = tr.attributeValue("id");

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");

            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();

                currentColCount++;

                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                String text = td.getText();

                if (text != null && text.length() > 0) {
                    if (text.indexOf(":") >= 0)
                        text += ";readonly;disabled";
                    else
                        text += ":;readonly;disabled";
                    if (isStatic == true) {
                        td.setText(transformToHtmlTagStatis(text, trId,
                                tdId,list));
                    } else {
                        td.setText(transformToHtmlTag(text, trId,
                                tdId,list));
                    }
                    colCount = (currentColCount > colCount) ? currentColCount
                            : colCount;

                }
            }
        }
        String ret = doc.asXML();

        // 格式转换
        ret = StringUtils.replace(ret, "&lt;", "<");
        ret = StringUtils.replace(ret, "&gt;", ">");
        ret = StringUtils.replace(ret, "&amp;", "&");

        ret = ret.substring(ret.indexOf("?>") + 2);

        ret += "<input type='hidden' value='" + rowCount
                + "' name='rowCount'/><input type='hidden' value='" + colCount
                + "' name='colCount'/>";
        return ret;
    }

    //预览查看
    public String fromDBByteArrayToHTMLTableEvaluation(byte[] byteArray,
                                                       List<ExcelPageValues> list, String type)
            throws UnsupportedEncodingException {

        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return visitEvaluation(doc, list, false);

    }


    //预览查看
    public String fromDBByteArrayToHTMLTableEvaluation2(byte[] byteArray,
                                                        List<ExcelPageValues2> list, String type)
            throws UnsupportedEncodingException {

        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        List<ExcelPageValues> list2 = new ArrayList<ExcelPageValues>();
        for(ExcelPageValues2 ex:list){
            ExcelPageValues exs = new ExcelPageValues();
            exs.setPageID(ex.getPageID());
            exs.setColIndex(ex.getColIndex());
            exs.setRowIndex(ex.getRowIndex());
            exs.setDataType(ex.getDataType());
            exs.setObjectId(ex.getObjectId());
            ex.setTxtValue(ex.getTxtValue());
            list2.add(exs);
        }
        return visitEvaluation(doc, list2, false);

    }

    //预览查看
    public String visitEvaluation(Document doc, List list, boolean isStatic) {
        if (doc == null)
            return null;

        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");
        int rowCount = 0;
        int colCount = 0;
        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();

            rowCount++;
            // 获取该TR的ID
            String trId = tr.attributeValue("id");

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");

            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();

                currentColCount++;

                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                String text = td.getText();

                if (text != null && text.length() > 0) {
					/*
					 * if(text.indexOf(":")>=0) text += ";readonly;disabled";
					 * else text += ":;readonly;disabled";
					 */
                    if (isStatic == true) {
                        td.setText(transformToHtmlTagStatis(text, trId,
                                tdId,list));
                    } else {
                        td.setText(transformToHtmlTagEvaluation(text, trId,
                                tdId, list));
                    }
                    colCount = (currentColCount > colCount) ? currentColCount
                            : colCount;

                }
            }
        }
        String ret = doc.asXML();

        // 格式转换
        ret = StringUtils.replace(ret, "&lt;", "<");
        ret = StringUtils.replace(ret, "&gt;", ">");
        ret = StringUtils.replace(ret, "&amp;", "&");

        ret = ret.substring(ret.indexOf("?>") + 2);


        ret += "<input type='hidden' value='" + rowCount
                + "' name='rowCount'/><input type='hidden' value='" + colCount
                + "' name='colCount'/>";
        return ret;
    }

    protected String transformToHtmlTagEvaluation(String cellText, String trId,
                                                  String tdId, List<ExcelPageValues> list) {

        if (cellText != null && cellText.length() > 0) {
            InputElement ie = createInputElement(cellText);
            ie.addKeyValueProp("id", trId + "-" + tdId);
            ie.addKeyValueProp("name", trId + "-" + tdId);
            if (list != null) {
                for (ExcelPageValues item : list) {
                    if (item != null) {
                        String dbrow = item.getRowIndex() + "";
                        String dbcol = item.getColIndex() + "";
                        if (trId.equals(dbrow) && tdId.equals(dbcol)) {
                            String value = item.getTxtValue();
                            ie.addKeyValueProp("value", value == null ? ""
                                    : value);
                        }
                    }
                }
            }
            return this.inputElementToHtmlTag(ie, trId, tdId);
        } else {
            return "";
        }

    }

    protected String transformToHtmlTag1(String cellText, String trId,
                                         String tdId, Map<Long, Map<Long, Float>> rowMap) {
        if (cellText != null && cellText.length() > 0) {
            InputElement ie = createInputElement(cellText);
            ie.addKeyValueProp("id", trId + "-" + tdId);
            ie.addKeyValueProp("name", trId + "-" + tdId);
            Map<Long, Float> rowValueMap = new HashMap();
            if (rowMap != null) {
                for (Long lg : rowMap.keySet()) {
                    if (lg != 0) {
                        rowValueMap = rowMap.get(lg);
                        if (rowValueMap != null) {
                            for (Long lr : rowValueMap.keySet()) {
                                String dbrow = lg + "";
                                String dbcol = lr + "";
                                if (trId.equals(dbrow) && tdId.equals(dbcol)) {
                                    Float value = rowValueMap.get(lr);
                                    String value1 = value + "";
                                    ie.addKeyValueProp("value",
                                            value1 == null ? "" : value1);
                                }
                            }
                        }

                    }
                }
            }
            return this.inputElementToHtmlTag(ie, trId, tdId);
        }
        return "";

    }

    public Object fromStringTo(Object str) {

        if (str != null && !"".equals(str)) {
            if (str instanceof Double) {
                double res = Double.parseDouble(String.valueOf(str));
                return res;
            }

        }

        return str;

    }

    public InputElement createInputElement(String txt) {

        if (txt == null)
            return null;

        // 风格类型
        String[] temp = txt.split(":", 2);

        JobConfiguration jobConfiguration = (JobConfiguration) SpringContextUtils.getBean("jobConfiguration");
        InputElementDefinition def = jobConfiguration.getInputElementDef(temp[0]);

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

    protected String inputElementToHtmlTag(InputElement ie, String trId,
                                           String tdId) {
        return ie == null ? "" : ie.toString();
    }

    public String fromDBByteArrayToHTMLTableStaticEvalu(byte[] array,
                                                        List<List<ExcelPageValues>> list) {
        InputStream stream = (new ByteArrayInputStream(array));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return visit(doc, list, true);
    }

    // 当作业Excel被导入时，如果选择完成，那么需要验证有些字段是否必填
    public Set analysisJobContentDone(EemTempEntity tempEntity,
                                      Sheet jobContentExcelSheet, long formId, long pageId,String strType)
            throws Exception {
        //----------------------------------------------
        boolean flag=false;
        //----------------------------------------------
        byte[] byteArray = tempEntity.getTemplateExcelByteData().getXmlFileData();
        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        if (doc == null)
            return set;

        // 获取所有TR

        Iterator trList = doc.getRootElement().elementIterator("tr");

        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();

            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");
            int currentColCount = 0;
            /**
             * 获取所有“总分项”
             */
            //----------------------------------------------
            Cell cell1 = null;
            try{
                cell1 = jobContentExcelSheet.getCell(
                        0, rowCount);
                if(cell1.getContents()!=null){
                    String str=cell1.getContents();
                    if(str.trim().equals("总分"))
                    {
                        flag=true;
                    }
                }
            }catch (Exception e){
            }
            List strList=new ArrayList();
            //-------------------------------------------------
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();
                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                // String text=td.getStringValue();
                String text = td.getText();
                if (StringUtils.isNotBlank(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        Cell cell = null;
                        try{
                            cell = jobContentExcelSheet.getCell(
                                    currentColCount, rowCount);
                        }catch (Exception e){

                        }

                        String cellText="";
                        // 获取格 内容
                        if(cell!=null){
                            cellText  = cell.getContents();
                            //-------------------------------------------------
                            if(flag){
                                strList.add(cellText);
                            }
                            //-------------------------------------------------
                        }

                        // 判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null
                                    && ismust.equals("true")
                                    && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }

                        if (cell!=null&&(cell.getType() == CellType.DATE
                                || cell.getType() == CellType.DATE_FORMULA)) {

                            DateCell dc = (DateCell) cell;
                            if (dc != null && dc.getDateFormat() != null
                                    && dc.getDate() != null) {
                                Date comparedDate = new Date();
                                try {
                                    comparedDate = new SimpleDateFormat(
                                            "yyyy-MM-dd").parse("1949-01-01");
                                } catch (Exception e) {
                                    comparedDate = new Date(1901, 1, 1);
                                }

                                if (dc.getDate().before(comparedDate))
                                    cellText = dc.getDateFormat().format(
                                            dc.getDate());
                                else
                                    cellText = new SimpleDateFormat(
                                            "yyyy-MM-dd").format(dc.getDate());
                            }
                        }
                        if(cellText.equals("#DIV/0!")){
                            cellText="";
                        }
                        ExcelPageValues epv = new ExcelPageValues();
                        epv.setRowIndex(rowCount);
                        epv.setColIndex(currentColCount);

                        if (cellText != null) {
                            cellText = cellText.replace(",", "");
                        }

                        epv.setTxtValue(cellText);
                        epv.setTpID(formId);
                        epv.setPageID(pageId);
                        set.add(epv);
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount
                        : colCount;
            }
            //-------------------------------------------------
            if(flag && strType != null && !strType.equals("") && strType.equals("甲")){
                for (int i=0;i<strList.size();i++){
                    for(int j=i;j<strList.size()-1;j++){
                        if(strList.get(i) != null && strList.get(j+1) != null
                                && !strList.get(i).equals("") && !strList.get(j+1).equals("")
                                && !strList.get(i).equals("0") && !strList.get(j+1).equals("0")
                                && strList.get(i).equals(strList.get(j+1)))
                        {
                            //return "后评价总分有重复项，请查证后手动上报！";
                            return null;
                        }

                    }
                }
            }
            //-------------------------------------------------
        }

        return set;
    }


    // 当作业Excel被导入时，如果选择完成，那么需要验证有些字段是否必填
    public Set analysisJobContentDone3(byte[] byteArray,
                                       Sheet jobContentExcelSheet, long formId, long pageId)
            throws Exception {

        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        if (doc == null)
            return set;

        // 获取所有TR

        Iterator trList = doc.getRootElement().elementIterator("tr");

        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();

            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");

            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();

                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                // String text=td.getStringValue();
                String text = td.getText();
                if (StringUtils.isNotBlank(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        Cell cell = jobContentExcelSheet.getCell(
                                currentColCount, rowCount);
                        // 获取格 内容
                        String cellText = cell.getContents();

                        // 判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null
                                    && ismust.equals("true")
                                    && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }

                        if (cell.getType() == CellType.DATE
                                || cell.getType() == CellType.DATE_FORMULA) {

                            DateCell dc = (DateCell) cell;
                            if (dc != null && dc.getDateFormat() != null
                                    && dc.getDate() != null) {
                                Date comparedDate = new Date();
                                try {
                                    comparedDate = new SimpleDateFormat(
                                            "yyyy-MM-dd").parse("1949-01-01");
                                } catch (Exception e) {
                                    comparedDate = new Date(1901, 1, 1);
                                }

                                if (dc.getDate().before(comparedDate))
                                    cellText = dc.getDateFormat().format(
                                            dc.getDate());
                                else
                                    cellText = new SimpleDateFormat(
                                            "yyyy-MM-dd").format(dc.getDate());
                            }
                        }
                        if(cellText.equals("#DIV/0!")){
                            cellText="";
                        }
                        ExcelPageValues2 epv = new ExcelPageValues2();
                        epv.setRowIndex(rowCount);
                        epv.setColIndex(currentColCount);

                        if (cellText != null) {
                            cellText = cellText.replace(",", "");
                        }

                        epv.setTxtValue(cellText);
                        epv.setTpID(formId);
                        epv.setPageID(pageId);
                        set.add(epv);
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount
                        : colCount;
            }
        }

        return set;
    }



    // 当作业Excel被导入时，如果选择完成，那么需要验证有些字段是否必填
    public List analysisJobContentDone2(byte[] byteArray,
                                        Sheet jobContentExcelSheet, long formId, long pageId)
            throws Exception {

        InputStream stream = (new ByteArrayInputStream(byteArray));
        Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(
                    new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<List<String>> list = new ArrayList();

        if (doc == null)
            return list;

        // 获取所有TR

        Iterator trList = doc.getRootElement().elementIterator("tr");

        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            List<String> list1 = new ArrayList();
            // 获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);

            // 获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");

            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();

                // 获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                // String text=td.getStringValue();
                String text = td.getText();
                if (StringUtils.isNotBlank(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        Cell cell = jobContentExcelSheet.getCell(
                                currentColCount, rowCount);
                        // 获取格 内容
                        String cellText = cell.getContents();

                        // 判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null
                                    && ismust.equals("true")
                                    && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }

                        if (cell.getType() == CellType.DATE
                                || cell.getType() == CellType.DATE_FORMULA) {

                            DateCell dc = (DateCell) cell;
                            if (dc != null && dc.getDateFormat() != null
                                    && dc.getDate() != null) {
                                Date comparedDate = new Date();
                                try {
                                    comparedDate = new SimpleDateFormat(
                                            "yyyy-MM-dd").parse("1949-01-01");
                                } catch (Exception e) {
                                    comparedDate = new Date(1901, 1, 1);
                                }

                                if (dc.getDate().before(comparedDate))
                                    cellText = dc.getDateFormat().format(
                                            dc.getDate());
                                else
                                    cellText = new SimpleDateFormat(
                                            "yyyy-MM-dd").format(dc.getDate());
                            }
                        }
                        if(cellText.equals("#DIV/0!")){
                            cellText="";
                        }
//                        ExcelPageValues epv = new ExcelPageValues();
//                        epv.setRowIndex(rowCount);
//                        epv.setColIndex(currentColCount);

                        if (cellText != null) {
                            cellText = cellText.replace(",", "");
                        }
                        logger.info("rowCount:"+rowCount+"--currentColCount:"+currentColCount+"==cellText:"+cellText);
                        list1.add(cellText);
//                        epv.setTxtValue(cellText);
//                        epv.setTpID(formId);
//                        epv.setPageID(pageId);
//                        set.add(epv);
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount
                        : colCount;
            }
            if(list1!=null&&list1.size()>0){
                list.add(list1);
            }
        }

        return list;
    }


    protected String transformToHtmlTagStatis(String cellText, String trId, String tdId, List<List<ExcelPageValues>> list) {

        if (cellText != null && cellText.length() > 0) {

            boolean isPercent = false;
            InputElement ie = createInputElement(cellText);
            ie.addKeyValueProp("id", trId + "-" + tdId);
            ie.addKeyValueProp("name", trId + "-" + tdId);
            double staticvalue = 0;
            if (list != null) {

                //
                /*Sheet sheet =null;
                     if(list.size()>0){
                         List<ExcelPageValues> tlist = list.get(0);
                         ExcelPageValues v= tlist.get(0);
                         long formId = v.getFormid();
                         ExcelService excelService = (ExcelService) SpringContextUtils.getBean("excelService");

                         ExcelTemplet et = excelService.findExcelTempletbyId(formId+"", "bdcsw");//本地传输网数据
                         if(et!=null){
                             ExcelTempletInputExcel etie = excelService.findExcelTempletInputByFormId(formId+"");
                             InputStream is = new ByteArrayInputStream(etie.getForm());
                            WorkbookSettings wbs = new WorkbookSettings();

                            wbs.setEncoding("GB2312");
                            try {
                                Workbook wb = Workbook.getWorkbook(is);
                            sheet =wb.getSheet(0);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                         }
                     }*/
                ///


                for (List<ExcelPageValues> eplist : list) {
                    for (ExcelPageValues item : eplist) {
                        if (item != null) {
                           /* String dbrow = item.getRowindex() + "";
                            String dbcol = item.getColindex() + "";
                            if (trId.equals(dbrow) && tdId.equals(dbcol)) {
                                String value = item.getTxtvalue();
                                double valueInt = 0;

                                if (!"".equals(value)&&value != null) {
                                    try {
                                        if (value.contains("%")) {
                                            isPercent = true;
                                            value = value.replaceAll("%", "");
                                        }
                                        value = value.trim();
                                        valueInt = Double.parseDouble(value);

                                    } catch (Exception ex) {
                                        System.out.println(value);
                                        ex.printStackTrace();
                                    }
                                }
                                staticvalue = staticvalue + valueInt;
                            }*/
                        }
                    }
                }
            }
            long tmp = Math.round((staticvalue * 100));
            DecimalFormat df = new DecimalFormat("0.00");
            String res = df.format(staticvalue);
            if (isPercent == true) {
                res = res + "%";
            }
            ie.addKeyValueProp("value", res);
            // ie.addKeyValueProp("value",  staticvalue+"");
            return this.inputElementToHtmlTag(ie, trId, tdId);
        } else {
            return "";
        }

    }
    protected String transformToHtmlTag(String cellText, String trId, String tdId,List<ExcelPageValues> list) {

        if(cellText != null && cellText.length()>0) {
            InputElement ie = createInputElement(cellText);
            ie.addKeyValueProp("id", trId+"-"+tdId);
            ie.addKeyValueProp("name", trId+"-"+tdId);
            if(list!=null){
                for(ExcelPageValues item:list){
                    if(item!=null){
                       /* String dbrow=item.getRowindex()+"";
                        String dbcol=item.getColindex()+"";
                        if(trId.equals(dbrow)&&tdId.equals(dbcol)){
                            String value=item.getTxtvalue();
                            ie.addKeyValueProp("value", value==null?"":value);
                        }*/
                    }
                }
            }
            return this.inputElementToHtmlTag(ie, trId, tdId);
        } else {
            return "";
        }

    }
}
