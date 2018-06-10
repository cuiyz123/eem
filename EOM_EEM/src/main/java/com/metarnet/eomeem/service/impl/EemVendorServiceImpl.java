package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.ExcelUtil;
import com.metarnet.eomeem.model.VendorEntity;
import com.metarnet.eomeem.service.IEemVendorService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Administrator on 2016/7/19.
 */
@Service
public class EemVendorServiceImpl implements IEemVendorService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public Pager queryVendorList(Pager pager, UserEntity userEntity) throws ServiceException {
        StringBuffer hql = new StringBuffer("from VendorEntity where deletedFlag=0 ");
        Map<String, Object> queryMap = new HashMap<String, Object>();
        try {
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("vendorCode") != null && StringUtils.isNotBlank(params.get("vendorCode").toString())) {
                    hql.append(" and vendorCode = '" + params.get("vendorCode") + "'");
                }
                if (params.get("vendorName") != null && StringUtils.isNotBlank(params.get("vendorName").toString())) {
                    hql.append(" and vendorName like '%" + params.get("vendorName") + "%'");
                }
                if (params.get("shortName") != null && StringUtils.isNotBlank(params.get("shortName").toString())) {
                    hql.append(" and shortName like '%" + params.get("shortName") + "%'");
                }
            }
            hql.append(" order by lastUpdateTime desc");
            pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public String saveVendor(VendorEntity vendorEntity, UserEntity userEntity) throws ServiceException {
        try {
            String hql = "from VendorEntity where deletedFlag=0 and (vendorName ='" + vendorEntity.getVendorName() + "' or shortName ='" + vendorEntity.getShortName() + "')";
            if (vendorEntity.getObjectId() == null) {//新增
                List<VendorEntity> vendorEntityList = baseDAO.find(hql);
                if (vendorEntityList.size() > 0) {
                    return "厂商名称或英文简称已经存在，请更改";
                }
            } else {//修改
                List<VendorEntity> vendorEntityList = baseDAO.find(hql + " and objectId!=" + vendorEntity.getObjectId());
                if (vendorEntityList.size() > 0) {
                    return "厂商名称或英文简称已经存在，请更改";
                }
            }
            vendorEntity.setOperUserId(userEntity.getUserId());
            vendorEntity.setOperUserPhone(userEntity.getMobilePhone());
            vendorEntity.setOperUserTrueName(userEntity.getTrueName());
            vendorEntity.setOperOrgId(userEntity.getOrgID());
            vendorEntity.setOperOrgCode(userEntity.getOrgCode());
            vendorEntity.setOperOrgName(userEntity.getOrgEntity().getOrgName());
            vendorEntity.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
            baseDAO.saveOrUpdate(vendorEntity, userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public VendorEntity showVendor(Long objectID, UserEntity userEntity) throws ServiceException {
        try {
            return (VendorEntity) baseDAO.get(VendorEntity.class, objectID);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteVendor(String objectIDs, UserEntity userEntity) throws ServiceException {
        try {
            List<VendorEntity> vendorEntityList = baseDAO.find("from VendorEntity where deletedFlag=0 and objectId in(" + objectIDs + ")");
            for (VendorEntity vendorEntity : vendorEntityList) {
                vendorEntity.setDeletedFlag(true);
                vendorEntity.setDeletedBy(userEntity.getUserId());
                vendorEntity.setDeletionTime(new Timestamp(System.currentTimeMillis()));
            }
            baseDAO.saveOrUpdateAll(vendorEntityList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String saveUploadFiles(MultipartHttpServletRequest multipartRequest, UserEntity userEntity) throws ServiceException {
        List<VendorEntity> vendorEntityList = new ArrayList<VendorEntity>();
        for (Iterator<String> iterator = multipartRequest.getFileNames(); iterator.hasNext(); ) {
            String key = iterator.next();
            MultipartFile file = multipartRequest.getFile(key);
            if (file.getSize() > 0) {
                try {
                    List<String[]> stringList = ExcelUtil.readExcel(file.getInputStream(), file.getOriginalFilename(), 1, 1, 2);
                    Map<String, VendorEntity> vendorNameMap = new HashMap<String, VendorEntity>();
                    Map<String, VendorEntity> shortNameMap = new HashMap<String, VendorEntity>();
                    if (stringList.size() > 0) {
                        List<VendorEntity> vendorEntityListOld = baseDAO.find("from VendorEntity where deletedFlag=0");
                        for (VendorEntity vendorEntity : vendorEntityListOld) {
                            vendorNameMap.put(vendorEntity.getVendorName(), vendorEntity);
                            shortNameMap.put(vendorEntity.getShortName(), vendorEntity);
                        }
                    }
                    for (String[] strings : stringList) {
                        if (vendorNameMap.get(strings[1]) != null || shortNameMap.get(strings[2]) != null) {
                            continue;
                        }
                        Long objectID = baseDAO.getSequenceNextValue(VendorEntity.class);
                        VendorEntity vendorEntity = new VendorEntity();
                        vendorEntity.setVendorCode((10000L+objectID)+"");
                        vendorEntity.setVendorName(strings[0]);
                        vendorEntity.setShortName(strings[1]);
                        vendorEntity.setShortName1(strings[2]);
                        vendorEntity.setCategory(strings[3]);
                        vendorEntity.setRemark(strings[4]);
                        vendorEntity.setObjectId(objectID);
                        vendorEntity.setOperUserId(userEntity.getUserId());
                        vendorEntity.setOperUserPhone(userEntity.getMobilePhone());
                        vendorEntity.setOperUserTrueName(userEntity.getTrueName());
                        vendorEntity.setOperOrgId(userEntity.getOrgID());
                        vendorEntity.setOperOrgCode(userEntity.getOrgCode());
                        vendorEntity.setOperOrgName(userEntity.getOrgEntity().getOrgName());
                        vendorEntity.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
                        vendorEntity.setDeletedFlag(false);
                        vendorEntityList.add(vendorEntity);
                    }
                    baseDAO.saveOrUpdateAll(vendorEntityList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    @Override
    public String getVendorCode(UserEntity userEntity) throws ServiceException {
        String vendorCode = "";
        try {
            Long objectID = baseDAO.getSequenceNextValue(VendorEntity.class);
            vendorCode = (10000+objectID)+"";
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return vendorCode;
    }
}
