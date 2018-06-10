package com.metarnet.core.common.service;


import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import com.metarnet.core.common.model.Pager;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;

import java.util.List;

/**
 * Created by Administrator on 2015/7/13.
 */
public interface IDraftService {

    /**
     * 查询草稿信息
     *
     * @param entityName
     * @param userEntity
     * @param page
     * @return
     * @throws com.metarnet.core.common.exception.ServiceException
     */
    public Pager queryDraftList(String entityName, UserEntity userEntity, Pager page) throws ServiceException;

    /**
     * 删除草稿
     *
     * @param entityName
     * @param idProperty
     * @param entityId
     */
    public void delDraft(String entityName, String idProperty, String entityId);
}
