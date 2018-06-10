package com.metarnet.eomeem.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.EemNoticeEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;

import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public interface IEemNoticeService {
    Pager queryNoticeList(Pager pager, UserEntity userEntity) throws ServiceException;

    List<EemNoticeEntity> findNoticeList(UserEntity userEntity) throws ServiceException;

    String saveNotice(EemNoticeEntity eemNoticeEntity, String deptCodes, UserEntity userEntity) throws ServiceException;

    EemNoticeEntity showNotice(Long objectID, UserEntity userEntity) throws ServiceException;

    void deleteNotice(Long objectID, UserEntity userEntity) throws ServiceException;

    void updateTopNotice(Long objectID, UserEntity userEntity) throws ServiceException;

    void updateNoticeStatus() throws ServiceException;
}
