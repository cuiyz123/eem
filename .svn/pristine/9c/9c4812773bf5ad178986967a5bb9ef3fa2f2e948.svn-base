package com.metarnet.eomeem.controller;

import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.TemplateExcelByteData;
import com.metarnet.eomeem.service.ITestService;
import com.metarnet.eomeem.utils.EemConstants;
import com.metarnet.eomeem.utils.ExcelConverter2;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/16.
 */
@Controller
public class TestController extends BaseController {
    private Logger logger = LogManager.getLogger(TestController.class);

    @Resource
    private ITestService testService;
    @Resource
    private IBaseDAO baseDAO;

    /**
     * 批量导入上报模板
     *
     * @param request
     * @param response
     * @throws UIException
     */
    @RequestMapping(value = "/testController.do", params = "method=importReportTemp")
    @ResponseBody
    public void importReportTemp(HttpServletRequest request, HttpServletResponse response) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        File file1 = new File("E:\\联通一级电子运维\\设备后评价(新)\\模板20160816\\上报\\页面");
        File file2 = new File("E:\\联通一级电子运维\\设备后评价(新)\\模板20160816\\上报\\数据");
        File[] tempList1 = file1.listFiles();
        File[] tempList2 = file2.listFiles();
        System.out.println("页面目录下对象个数：" + tempList1.length);
        System.out.println("数据目录下对象个数：" + tempList2.length);
        Map<String, File> fileMap = new HashMap<String, File>();
        for (int i = 0; i < tempList2.length; i++) {
            if (tempList2[i].isFile()) {
                fileMap.put(tempList2[i].getName(), tempList2[i]);
                /*if(reportMap.get(tempList2[i].getName())!=null){
                    System.out.println(tempList2[i].getName()+"|OK");
                }else{
                    System.out.println(tempList2[i].getName()+"|NO");
                }*/
            }
        }
        for (int i = 0; i < tempList1.length; i++) {
            if (tempList1[i].isFile()) {
                testService.saveReportTemps(tempList1[i], fileMap.get(tempList1[i].getName()), userEntity);
            }
        }

    }

    /**
     * 批量导入汇总模板
     *
     * @param request
     * @param response
     * @throws UIException
     */
    @RequestMapping(value = "/testController.do", params = "method=importCollectTemp")
    @ResponseBody
    public void importCollectTemp(HttpServletRequest request, HttpServletResponse response) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        File file = new File("E:\\联通一级电子运维\\设备后评价(新)\\模板20160816\\汇总");
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                testService.saveCollectTemps(tempList[i], tempList[i], userEntity);
            }
        }
    }

    /**
     * 修改上报模板和汇总模板关联关系
     *
     * @param request
     * @param response
     * @throws UIException
     */
    @RequestMapping(value = "/testController.do", params = "method=updateReportRelID")
    @ResponseBody
    public void updateReportRelID(HttpServletRequest request, HttpServletResponse response) throws UIException {
        testService.updateReportRelID();

    }

    @RequestMapping(value = "/testController.do", params = "method=getAllFiles")
    @ResponseBody
    public void getAllFiles2(HttpServletRequest request, HttpServletResponse response) throws UIException {
        try {
            List<EemTempEntity> eemTempEntityList = baseDAO.find("from EemTempEntity where tempType=1");
            for (EemTempEntity eemTempEntity : eemTempEntityList) {
//                System.out.println("idsMap.put(\"" + eemTempEntity.getPrimitiveName() + "\"," + eemTempEntity.getObjectId() + "L);");
                System.out.println("update t_eom_temp_info set TEMP_DATA_ID="+eemTempEntity.getEemTempEntity().getObjectId()+" where OBJECT_ID="+eemTempEntity.getObjectId()+" ;");
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
}
