package com.metarnet.eomeem.controller;


import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.EvaluationReportExcel;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.service.IEemGatherService;
import com.metarnet.eomeem.service.IEemReportService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.EemConstants;
import com.primeton.das.entity.impl.hibernate.mapping.Array;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class EemGatherController extends BaseController {

    @Resource
    private IEemGatherService gatherService;


    @RequestMapping(value = "/reportController.do", params = "method=autoGather")
    public void autoGather() {
        gatherService.saveAutoGather();
    }

}
