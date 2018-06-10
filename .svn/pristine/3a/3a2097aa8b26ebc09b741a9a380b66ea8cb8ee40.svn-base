package com.metarnet.eomeem.time;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.eomeem.controller.EemGatherController;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.EvaluationReportExcel;
import com.metarnet.eomeem.model.EvaluationReportTime;
import com.metarnet.eomeem.service.IEemCommonService;
import com.metarnet.eomeem.service.IEemGatherService;
import com.metarnet.eomeem.service.IEemNoticeService;
import com.metarnet.eomeem.service.impl.EemGatherServiceImpl;
import com.metarnet.eomeem.utils.EemConstants;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import jxl.write.WritableWorkbook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by admin on 2016/7/12.
 */

//@Component
@Controller
public class AutoReport {

    @Resource
    private IEemGatherService gatherService;
    @Resource
    private IEemNoticeService eemNoticeService;

    Logger logger = LogManager.getLogger(EemGatherServiceImpl.class);
    public  void saveJob(){
        if(EemConstants.AUTO_REPORT_LOCK){
            logger.info("+++++++++++++++++++++++++++++++++++start");
            gatherService.saveAutoGather();
            logger.info("+++++++++++++++++++++++++++++++++++end");
        }

    }


    public  void saveAppoint(){
            logger.info("+++++++++++++++++++++++++++++++++++saveAppoint");
//        gatherService.saveAppoint();
            logger.info("+++++++++++++++++++++++++++++++++++saveAppoint");

    }

    public  void updateNoticeStatus(){
        try {
            eemNoticeService.updateNoticeStatus();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * 超时提醒
     */
    public void timeoutAlert(){
        gatherService.timeoutAlert();
    }


    @RequestMapping(value = "/autoReport.do", params = "method=test")
    @ResponseBody
    public void test(){
        this.timeoutAlert();
    }
}
