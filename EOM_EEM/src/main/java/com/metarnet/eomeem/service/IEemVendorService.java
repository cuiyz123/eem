package com.metarnet.eomeem.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.VendorEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Created by Administrator on 2016/7/19.
 */
public interface IEemVendorService {
    Pager queryVendorList(Pager pager, UserEntity userEntity) throws ServiceException;

    String saveVendor(VendorEntity vendorEntity, UserEntity userEntity) throws ServiceException;

    VendorEntity showVendor(Long objectID, UserEntity userEntity) throws ServiceException;

    void deleteVendor(String objectIDs, UserEntity userEntity) throws ServiceException;

    String saveUploadFiles(MultipartHttpServletRequest multipartRequest, UserEntity userEntity) throws ServiceException;

    String getVendorCode(UserEntity userEntity) throws ServiceException;
}
