package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.service.IDraftService;
import com.metarnet.core.common.service.IGeneOperateService;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.PagerPropertyUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 公共草稿管理
 * @author zwwang
 */
@Controller
public class CommDraftController extends BaseController {


    @Resource
    private IDraftService draftService;

    @RequestMapping(value = "/commDraftController.do", params = "method=queryDraftList")
    private void queryDraftList(HttpServletResponse response, HttpServletRequest request, String entityName) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = draftService.queryDraftList(entityName, getUserEntity(request), pager);
            pager.setIsSuccess(true);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager,ser, SerializerFeature.WriteNullListAsEmpty), "draft");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/commDraftController.do", params = "method=delDraft")
    private void delDraft(HttpServletRequest request, HttpServletResponse response, String entityName, String idProperty , String entityId) throws UIException {
        draftService.delDraft(entityName , idProperty , entityId);
        endHandle(request, response, "", "draft");
    }
}
