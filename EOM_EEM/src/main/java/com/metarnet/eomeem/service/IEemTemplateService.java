package com.metarnet.eomeem.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.ReportEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

/**
 * Created by Administrator on 2016/6/2.
 */
public interface IEemTemplateService {
    Pager queryTemplateList(Pager pager, UserEntity userEntity) throws ServiceException;

    String saveUploadFiles(EemTempEntity eemTempEntity, MultipartHttpServletRequest multipartRequest, UserEntity userEntity, Long relID) throws ServiceException;
//厂商设备关系
    String saveUploadFiles(ReportEntity reportEntity, UserEntity userEntity) throws ServiceException;

    EemTempEntity findTempByID(Long id) throws ServiceException;

    void deleteTemp(Long objectID, UserEntity userEntity) throws ServiceException;

    void updateRel(Long tempID, Long relTempID, UserEntity userEntity) throws ServiceException;

    List<EemTempEntity> findAllTempEntity(String type) throws ServiceException;

    List<EemTempEntity> findEportTempByCollectID(Long formId);
}