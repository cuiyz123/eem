package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.DisCommonModel;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.TaskInstance;

/**
 * Created by Administrator on 2016/4/15/0015.
 */
public interface ICommDispatchService {

    public String submitTurnToDispatch(DisCommonModel disCommonModel, TaskInstance taskInstance, UserEntity userEntity, String dispatchType) throws ServiceException;

    public DisCommonModel initTurnToDispatch(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;

    public DisCommonModel showTurnToDispatch(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;
}
