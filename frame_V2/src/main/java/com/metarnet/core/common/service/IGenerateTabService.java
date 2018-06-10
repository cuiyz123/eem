package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Tab;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-7-6
 * Time: 上午10:58
 * 生成页卡服务
 */
public interface IGenerateTabService {
    /**
     * 获取自定义标签
     *
     * @param userEntity        用户Id
     * @param rootProcessInstId 主流程实例id ，注意是指根流程
     * @param shardingId        分片ID
     * @return
     * @throws ServiceException
     */
    public List<Tab> generateTabs(UserEntity userEntity, String rootProcessInstId,
                                  Integer shardingId) throws ServiceException;
}
