package com.metarnet.core.common.service;


import com.metarnet.core.common.exception.ServiceException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-11-10
 * Time: 下午2:21
 * To change this template use File | Settings | File Templates.
 */
public interface ICommEntityService {

    /**
     *
     * @param request
     * @param response
     * @param rootProcessInstId
     * @param userEntity
     * @return   需要下载的附件列表
     * @throws ServiceException
     */
    public  List allDown(HttpServletRequest request, HttpServletResponse response, String rootProcessInstId, UserEntity userEntity)throws ServiceException;

    /**
     * 检查附件是否有附件
     * @param request
     * @param response
     * @param rootProcessInstId
     * @param userEntity
     * @return
     * @throws ServiceException
     */
    public  boolean CheckAllDown(HttpServletRequest request, HttpServletResponse response, String rootProcessInstId, UserEntity userEntity)throws ServiceException;

    public  void queryAppAndDisByRootProcess(HttpServletRequest request,String rootProcessInstID) throws ServiceException;
}
