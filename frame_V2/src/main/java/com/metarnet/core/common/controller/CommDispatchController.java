package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DisCommonModel;
import com.metarnet.core.common.service.ICommDispatchService;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2016/4/15/0015.
 */
@Controller
public class CommDispatchController extends BaseController {

    @Resource(name = "disCommService")
    private ICommDispatchService commDispatchService;

    /**
     * 转派操作
     *
     * @param disCommonModel       调度信息
     * @param taskInstance           待办信息
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/commDispatchController.do", params = "method=turnToDispatch")
    @ResponseBody
    public void turnToDispatch(DisCommonModel disCommonModel, TaskInstance taskInstance,String dispatchType, HttpServletRequest request, HttpServletResponse response) throws UIException {
        String msg = "";
        try {
            msg = commDispatchService.submitTurnToDispatch(disCommonModel, taskInstance, getUserEntity(request), dispatchType);
        } catch (Exception e) {
            throw new UIException("", e);
        } finally {
            JSONObject jsonObject = new JSONObject();
            if(StringUtils.isNotBlank(msg)){
                jsonObject.put("success", false);
                jsonObject.put("msg", msg);
            }else{
                jsonObject.put("success", true);
            }
            endHandle(request, response, jsonObject, taskInstance.getTaskInstID());
        }
    }

    /**
     * 转派初始化操作
     *
     * @param taskInstance           待办信息
     * @throws UIException
     */
    @RequestMapping(value = "/commDispatchController.do", params = "method=initTurnToDispatch")
    @ResponseBody
    public ModelAndView initTurnToDispatch(TaskInstance taskInstance, HttpServletRequest request, HttpServletResponse response) throws UIException {
        DisCommonModel disCommonModel = null;
        try {
            disCommonModel = commDispatchService.initTurnToDispatch(taskInstance , getUserEntity(request));
            request.setAttribute("dispatchType",request.getParameter("dispatchType"));
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("base/page/turnToDispatch.jsp") , "disCommonModel" , disCommonModel);
    }

    @RequestMapping(value = "/showTurnToDispatch.do")
    @ResponseBody
    public ModelAndView showTurnToDispatch(TaskInstance taskInstance, HttpServletRequest request, HttpServletResponse response) throws UIException {
        DisCommonModel disCommonModel = null;
        try {
            disCommonModel = commDispatchService.showTurnToDispatch(taskInstance, getUserEntity(request));
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return new ModelAndView(new InternalResourceView("base/page/turnToDispatchShow.jsp") , "disCommonModel" , disCommonModel);
    }
}
