package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.service.IDraftService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/14/0014.
 */
@Service
public class DraftServiceImpl implements IDraftService {

    @Resource
    private IBaseDAO baseDAO;

    @Override
    public Pager queryDraftList(String entityName, UserEntity userEntity, Pager page) throws ServiceException {
        try {
            Map paramsMap = new HashMap();
            paramsMap.put("createdBy", userEntity.getUserId());
            paramsMap.put("draftFlag", true);
            page = baseDAO.getPageByHql("from " + entityName + " where createdBy=:createdBy and draftFlag=:draftFlag order by creationTime desc", page, paramsMap);
        } catch (DAOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return page;
    }

    @Override
    public void delDraft(String entityName, String idProperty, String entityId) {
        Session session = baseDAO.getSessionFactory().openSession();
        Query querydelete = session.createQuery("delete from " + entityName + " t where t." + idProperty + "=?");
        querydelete.setLong(0, Long.valueOf(entityId));
        querydelete.executeUpdate();
        session.close();
    }
}
