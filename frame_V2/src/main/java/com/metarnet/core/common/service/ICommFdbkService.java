package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.DisCommonModel;
import com.metarnet.core.common.model.FdbkCommonModel;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.TaskInstance;

import java.util.List;

/**
 * Created by Administrator on 2016/4/15/0015.
 */
public interface ICommFdbkService {

    public List<FdbkCommonModel> findFdbkListByTaskInst(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;

    public List<FdbkCommonModel> findFdbkListByParentProInstId(String processInstID, UserEntity userEntity) throws ServiceException;

}
