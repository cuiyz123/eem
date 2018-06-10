package com.metarnet.core.common.utils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mhma
 * Date: 16-6-22
 * Time: 上午11:02
 */
public class ExcelUtil {
    private static final Logger LOG = Logger.getLogger(ExcelUtil.class);
    //时间类型转换
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //数字类型转换
    private static DecimalFormat df = new DecimalFormat("#.##");
    private ExcelUtil(){

    }

    /**
     *
     * @param file  要读取的excel文件
     * @param fileName 要读取的excel文件名
     * @param index 将第几行作为完整数据行
     * @param sheetIndex 从第几个sheet页开始读取
     * @param rowIndex 从第几行开始读取
     * @return
     */
    public static List<String[]> readExcel(final File file, final String fileName,final Integer index,Integer sheetIndex,Integer rowIndex) {
        List<String []> list = new ArrayList<String[]>();
        Workbook workbook = null;
        String[] values = null;
        int rowNum=0,colNum=0;
        InputStream inputStream=null;
        try {
            sheetIndex=sheetIndex-1;
            rowIndex=rowIndex-1;
            inputStream = new FileInputStream(file);
            if (fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (fileName.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                return list;//文件类型错误，返回Empty List
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex);//第几个Sheet页
            rowNum = sheet.getLastRowNum();//数据总行数
            if(rowNum==0) return list;//总行数为0，返回Empty List
            colNum = sheet.getRow(index-1).getLastCellNum();//以第几行为完整字段数，避免结尾字段为null，不做处理
            // 读取内容区域
            for (int i = rowIndex; i <= rowNum; i++) {
                values = new String[colNum];
                Row row = sheet.getRow(i);
                if(row==null) break;
                // 每一行创建一个字符串数组放此行cell中的值
                for (int j = 0; j < colNum; j++) {
                    Cell cell = row.getCell(j);
                    boolean isMerge=isMergedRegion(sheet,i,j);
                    if(isMerge){
                        values[j]=getMergedRegionValue(sheet,i,j);
                    }else{
                        if (null != cell) {
                            synchronized (cell) {
                                values[j] = getCellValue(cell).trim();
                            }
                        } else {
                            values[j] = "";
                        }
                    }
                }
                list.add(values);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!=null) inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    public static List<String[]> readExcel(InputStream inputStream, final String fileName,final Integer index,Integer sheetIndex,Integer rowIndex) {
        List<String []> list = new ArrayList<String[]>();
        Workbook workbook = null;
        String[] values = null;
        int rowNum=0,colNum=0;
        try {
            sheetIndex=sheetIndex-1;
            rowIndex=rowIndex-1;
            if (fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (fileName.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                return list;//文件类型错误，返回Empty List
            }
            Sheet sheet = workbook.getSheetAt(sheetIndex);//第几个Sheet页
            rowNum = sheet.getLastRowNum();//数据总行数
            if(rowNum==0) return list;//总行数为0，返回Empty List
            colNum = sheet.getRow(index-1).getLastCellNum();//以第几行为完整字段数，避免结尾字段为null，不做处理
            // 读取内容区域
            for (int i = rowIndex; i <= rowNum; i++) {
                values = new String[colNum];
                Row row = sheet.getRow(i);
                if(row==null) break;
                // 每一行创建一个字符串数组放此行cell中的值
                for (int j = 0; j < colNum; j++) {
                    Cell cell = row.getCell(j);
                    boolean isMerge=isMergedRegion(sheet,i,j);
                    if(isMerge){
                        values[j]=getMergedRegionValue(sheet,i,j);
                    }else{
                        if (null != cell) {
                            synchronized (cell) {
                                values[j] = getCellValue(cell).trim();
                            }
                        } else {
                            values[j] = "";
                        }
                    }
                }
                list.add(values);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream!=null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    /**
     *
     * @param file 需要读取的excel文件
     * @param index 以第几行作为完整字段行
     * 默认读取第一个sheet页，从第一行开始读取
     * @return
     */
    public static List<String []> readExcel(final File file,Integer index){
       return readExcel(file,file.getName(),index,1,1);
    }

    public static String getCellValue(final Cell cell) {
        String value = "";
        if(cell == null) return value;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)){
                    value = sdf.format(cell.getDateCellValue());
                }else{
                    value = df.format(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_BLANK:
                value = "";
                break;
            case Cell.CELL_TYPE_ERROR:
                value = "";
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    public static boolean isMergedRegion(Sheet sheet,int row ,int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return true;
                }
            }
        }
        return false;
    }

    public static String getMergedRegionValue(Sheet sheet ,int row , int column){
        int sheetMergeCount = sheet.getNumMergedRegions();
        for(int i = 0 ; i < sheetMergeCount ; i++){
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    return getCellValue(fCell) ;
                }
            }
        }
        return "" ;
    }
}
