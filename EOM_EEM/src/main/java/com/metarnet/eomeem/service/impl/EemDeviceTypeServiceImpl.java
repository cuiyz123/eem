package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.eomeem.model.DeviceTypeEntity;
import com.metarnet.eomeem.service.IEemDeviceTypeService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Administrator on 2016/7/25.
 */
@Service
public class EemDeviceTypeServiceImpl implements IEemDeviceTypeService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public List queryDeviceTypeList(Long id, String param, UserEntity userEntity) throws ServiceException {
        List<DeviceTypeEntity> deviceTypeEntityList = new ArrayList<DeviceTypeEntity>();
        Set<DeviceTypeEntity> deviceTypeEntitySet = new HashSet<DeviceTypeEntity>();
        try {
            if (StringUtils.isNotBlank(param)) {
                if (id == null) {
                    List<DeviceTypeEntity> deviceTypeEntityListNew = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and name like '%" + param + "%'");
                    for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntityListNew) {
                        if (deviceTypeEntity.getParentID() == -1) {
                            continue;
                        } else {
                            findDeviceTypeByParentID(deviceTypeEntity.getParentID(), deviceTypeEntitySet);
                        }
                    }
                    deviceTypeEntitySet.addAll(deviceTypeEntityListNew);
                    for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntitySet) {
                        if (deviceTypeEntity.getLeaf()) {
                            deviceTypeEntity.setIsParent(false);
                            deviceTypeEntity.setOpen(false);
                        } else {
                            deviceTypeEntity.setOpen(true);
                            deviceTypeEntity.setIsParent(true);
                        }
                    }
                    deviceTypeEntityList.addAll(deviceTypeEntitySet);
                } else {
                    deviceTypeEntityList = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and parentID=" + id);
                }
            } else {
                if (id == null) {
                    deviceTypeEntityList = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and parentID < 0");
                } else {
                    deviceTypeEntityList = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and parentID=" + id);
                }
                for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntityList) {
                    if (deviceTypeEntity.getParentID() < 0L) {
                        deviceTypeEntity.setOpen(true);
                    } else {
                        deviceTypeEntity.setOpen(false);
                    }
                    if (deviceTypeEntity.getLeaf()) {
                        deviceTypeEntity.setIsParent(false);
                    } else {
                        deviceTypeEntity.setIsParent(true);
                    }
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return deviceTypeEntityList;
    }

    @Override
    public String saveDeviceType(DeviceTypeEntity deviceTypeEntity, UserEntity userEntity) throws ServiceException {
        deviceTypeEntity.setLastEditDate(new Date());
        deviceTypeEntity.setLastEditPerson(userEntity.getTrueName());
        List<DeviceTypeEntity> deviceTypeEntityList = new ArrayList<DeviceTypeEntity>();
        String hql = "from DeviceTypeEntity where deletedFlag=0 and name='" + deviceTypeEntity.getName() + "'";
        try {
            if (deviceTypeEntity.getObjectId() == null) {
                List<DeviceTypeEntity> deviceTypeEntities = baseDAO.find(hql);
                if (deviceTypeEntities.size() > 0) {
                    return "设备已存在，请更改";
                }
                deviceTypeEntity.setObjectId(baseDAO.getSequenceNextValue(DeviceTypeEntity.class));
                deviceTypeEntity.setLeaf(true);
                DeviceTypeEntity parentDeviceTypeEntity = (DeviceTypeEntity) baseDAO.get(DeviceTypeEntity.class, deviceTypeEntity.getParentID());
                parentDeviceTypeEntity.setLeaf(false);
                deviceTypeEntityList.add(parentDeviceTypeEntity);
            } else {
                List<DeviceTypeEntity> deviceTypeEntities = baseDAO.find(hql + " and objectId!=" + deviceTypeEntity.getObjectId());
                if (deviceTypeEntities.size() > 0) {
                    return "设备已存在，请更改";
                }
            }
            deviceTypeEntity.setDeletedFlag(false);
            deviceTypeEntityList.add(deviceTypeEntity);
            baseDAO.saveOrUpdateAll(deviceTypeEntityList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String deleteDeviceType(DeviceTypeEntity deviceTypeEntity, UserEntity userEntity) throws ServiceException {
        try {
            List<DeviceTypeEntity> deviceTypeEntityList = new ArrayList<DeviceTypeEntity>();
            deviceTypeEntity = (DeviceTypeEntity) baseDAO.get(DeviceTypeEntity.class, deviceTypeEntity.getObjectId());
            deviceTypeEntity.setDeletedFlag(true);
            deviceTypeEntity.setDeletionTime(new Timestamp(System.currentTimeMillis()));
            deviceTypeEntity.setDeletedBy(userEntity.getUserId());
            deviceTypeEntityList.add(deviceTypeEntity);
            List<DeviceTypeEntity> deviceTypeEntityListNew = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and parentID=" + deviceTypeEntity.getParentID());
            if (!deviceTypeEntity.getLeaf()) {
                deleteDeviceTypeByParentID(deviceTypeEntity, userEntity);
            }
            if (deviceTypeEntityListNew.size() == 0) {
                DeviceTypeEntity parentDeviceTypeEntity = (DeviceTypeEntity) baseDAO.get(DeviceTypeEntity.class, deviceTypeEntity.getParentID());
                parentDeviceTypeEntity.setLeaf(true);
                deviceTypeEntityList.add(parentDeviceTypeEntity);
            }
            baseDAO.saveOrUpdateAll(deviceTypeEntityList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteDeviceTypeByParentID(DeviceTypeEntity deviceTypeEntity, UserEntity userEntity) {
        List<DeviceTypeEntity> deviceTypeEntityLeafList = null;
        try {
            deviceTypeEntityLeafList = baseDAO.find("from DeviceTypeEntity where deletedFlag=0 and parentID=" + deviceTypeEntity.getObjectId());
            if (deviceTypeEntityLeafList != null && deviceTypeEntityLeafList.size() > 0) {
                for (DeviceTypeEntity deviceTypeEntity1 : deviceTypeEntityLeafList) {
                    deviceTypeEntity1.setDeletedFlag(true);
                    deviceTypeEntity.setDeletionTime(new Timestamp(System.currentTimeMillis()));
                    deviceTypeEntity.setDeletedBy(userEntity.getUserId());
                    deleteDeviceTypeByParentID(deviceTypeEntity1, userEntity);
                }
                baseDAO.saveOrUpdateAll(deviceTypeEntityLeafList);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void findDeviceTypeByParentID(Long parentID, Set<DeviceTypeEntity> deviceTypeEntitySet) {
        try {
            DeviceTypeEntity deviceTypeEntity = (DeviceTypeEntity) baseDAO.get(DeviceTypeEntity.class, parentID);
            deviceTypeEntitySet.add(deviceTypeEntity);
            if (deviceTypeEntity.getObjectId() != -1) {
                findDeviceTypeByParentID(deviceTypeEntity.getParentID(), deviceTypeEntitySet);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
}
