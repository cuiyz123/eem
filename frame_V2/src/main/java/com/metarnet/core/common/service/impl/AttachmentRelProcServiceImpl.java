package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.controller.editor.InitUtil;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.metarnet.core.common.model.UploadFileInfo;
import com.metarnet.core.common.service.IAttachmentRelProcService;
import com.metarnet.core.common.utils.BeanUtils;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA. Company: Metarnet User: kans Date: 13-4-22 Time:
 * 下午5:30 Description: 流程附件关联表实体对象服务层接口实现类.
 */
@Service(value = "attachmentRelProcService")
public class AttachmentRelProcServiceImpl implements IAttachmentRelProcService {

    /**
     * 实现功能:上传附件到pass平台指定存储区,保存附件件记录信息
     *
     * @param object      保存的附件对象
     * @param request     HttpServletRequest
     * @param storageName 存储区名
     * @param keyWords    关键字
     * @param operator    操作
     * @return 保存的附件对象
     * @throws ServiceException
     */
    @Override
    public TEomAttachmentRelProc saveFileAndUploadToPass(Object object,
                                                         HttpServletRequest request, String storageName,
                                                         String keyWords,
                                                         String operator, UserEntity userEntity) throws
            ServiceException {
        try {
            // 转型为org.springframework.web.multipart.MultipartHttpRequest
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            for (Iterator iterator = multipartRequest.getFileNames(); iterator
                    .hasNext(); ) {
                String key = (String) iterator.next();
                MultipartFile file = multipartRequest.getFile(key);
                // if (file != null && !file.isEmpty()) {
                if (file != null) {
                    String attachmentId = this.insertAttachmentToPass(file,
                            storageName, keyWords, operator);
                    if(attachmentId == null){
                        return null;
                    }
                    if (object instanceof TEomAttachmentRelProc) {
                        TEomAttachmentRelProc attachmentRelProc = (TEomAttachmentRelProc) object;
                        attachmentRelProc.setAttachmentId(attachmentId);
                        attachmentRelProc.setAttachmentName(file
                                .getOriginalFilename());
                        attachmentRelProc = this.save(attachmentRelProc,
                                userEntity);
                        return attachmentRelProc;
                    }
                }
            }
            return null;
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 实现功能:保存附件到pass平台指定存储区
     *
     * @param file        spring的MultipartFile
     * @param storageName 存储区名
     * @param keyWords    关键字
     * @param operator    操作
     * @return
     * @throws ServiceException
     */
    @Override
    public String insertAttachmentToPass(MultipartFile file,
                                         String storageName, String keyWords, String operator)
            throws ServiceException {
        try {

            FileAdapter fileAdapter = FileAdapter.getInstance();
            UploadFileInfo uploadFileInfo = new UploadFileInfo();
            uploadFileInfo.setFileName(file.getOriginalFilename());
            uploadFileInfo.setDesc(file.getOriginalFilename());
            uploadFileInfo.setKeywords(keyWords);
            uploadFileInfo.setOperator(operator);
            uploadFileInfo.setFileSize(file.getSize());
            DataInputStream dataInputStream = new DataInputStream(file
                    .getInputStream());

            // 调用Paas平台上传文件服务,返回附件ID
            String attachmentId = fileAdapter.upload(storageName,
                    uploadFileInfo, dataInputStream);
            return attachmentId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    @Resource
    private IBaseDAO baseDAO;

    /**
     * 保存非流程附件
     *
     * @param attachmentRelProc 附件对象
     * @param userEntity        用户ID
     * @return 附件对象
     * @throws ServiceException
     *
     */
    @Override
    public TEomAttachmentRelProc save(TEomAttachmentRelProc attachmentRelProc,
                                      UserEntity userEntity) throws ServiceException {
        try {
            Long nextVal = baseDAO
                    .getSequenceNextValue(TEomAttachmentRelProc.class);// 根据序列获取主键
            if (nextVal != null && nextVal != 0) {
                attachmentRelProc.setObjectId(nextVal);
                attachmentRelProc.setArchiveBaseTime(new Timestamp(InitUtil.getArchiveBaseDate().getTime()));
                this.baseDAO.save(attachmentRelProc, userEntity);
                return attachmentRelProc;
            } else {
                throw new Exception("实体对象TEomAttachmentRelProc序列:" + nextVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 删除非流程附件
     *
     * @param attachmentRelProcList 附件集合
     * @param userId                用户ID
     * @throws ServiceException
     *
     */
    @Override
    public void deleteAttachments(
            List<TEomAttachmentRelProc> attachmentRelProcList, int userId)
            throws ServiceException {
        try {
            if (!attachmentRelProcList.isEmpty()) {
                FileAdapter fileAdapter = FileAdapter.getInstance();
                for (TEomAttachmentRelProc attachmentRelProc : attachmentRelProcList) {
                    List<TEomAttachmentRelProc> attachQueryList = this.baseDAO
                            .findByExample(attachmentRelProc);
                    for (TEomAttachmentRelProc attachQuery : attachQueryList) {
                        // 删除Paas文件、数据库记录
                        fileAdapter.delete(attachQuery.getAttachmentId(),
                                "operator");
                        this.baseDAO.delete(attachQuery, userId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 非流程附件查询
     *
     * @param attachmentRelProc 查询对象
     * @return
     * @throws ServiceException
     *
     */
    @Override
    public List<TEomAttachmentRelProc> findByExample(
            TEomAttachmentRelProc attachmentRelProc) throws ServiceException {
        List<TEomAttachmentRelProc> list = new ArrayList<TEomAttachmentRelProc>();
        try {
            List objList = this.baseDAO.findByExample(attachmentRelProc);
            for (Object obj : objList) {
                if (obj instanceof TEomAttachmentRelProc) {
                    list.add((TEomAttachmentRelProc) obj);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 流程附件查询
     *
     * @param attachmentRelProcList
     * @return 流程附件对象列表
     * @throws ServiceException
     */
    @Override
    public List<TEomAttachmentRelProc> findByExample(
            List<TEomAttachmentRelProc> attachmentRelProcList)
            throws ServiceException {
        List<TEomAttachmentRelProc> resultList = new ArrayList<TEomAttachmentRelProc>();
        try {
            if (attachmentRelProcList != null
                    && attachmentRelProcList.size() > 0) {
                for (TEomAttachmentRelProc attachmentRelProc : attachmentRelProcList) {
                    if(attachmentRelProc.getFlowingObjectId()==null){
                        continue;
                    }
                    List<TEomAttachmentRelProc> tEomAttachmentRelProcList = this
                            .findByExample(attachmentRelProc);
                    if (tEomAttachmentRelProcList != null
                            && tEomAttachmentRelProcList.size() > 0) {
                        resultList.addAll(tEomAttachmentRelProcList);
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 更新非流程附件记录
     *
     * @param attachmentRelProcList 附件记录列表
     * @param attachmentRelProc     附件更新内容对象
     * @param userId                用户ID
     * @throws ServiceException
     *
     */
    @Override
    public void updateAttachments(
            List<TEomAttachmentRelProc> attachmentRelProcList,
            TEomAttachmentRelProc attachmentRelProc, int userId)
            throws ServiceException {
        try {
            if (attachmentRelProc != null) {
                for (TEomAttachmentRelProc attachCondition : attachmentRelProcList) {
                    List<TEomAttachmentRelProc> attachQueryList = this.baseDAO
                            .findByExample(attachCondition);
                    for (TEomAttachmentRelProc attachQuery : attachQueryList) {
                        // 将 paramsAttach 中不为空的属性，赋值给 attachQuery 中对应的属性
                        BeanUtils.copyProperties(attachmentRelProc,
                                attachQuery, false);
                        this.baseDAO.update(attachQuery, userId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * 实现功能:根据前台回传附件信息,更新附件列表
     * 使用说明:前台参数attachmentInfo字符串为已上传附件的attachmentId值,多个以逗号分隔
     *
     * @param userEntity              用户实体对象
     * @param attachmentInfo          回传附件信息
     * @param flowingObjectId         关联对象主键
     * @param activityInstanceId      活动(环节)实例ID
     * @param shardingId              附件关联表分片ID
     * @param flowingObjectShardingId 流转对象分片ID
     * @throws ServiceException
     * @examle :attachmentInfo =
     * "3udf-4iooo-sdsad,3udf-4iooo-sdsad,3udf-4iooo-sdsad"
     */
    @Override
    public void updateAttachements(UserEntity userEntity, String attachmentInfo, Long flowingObjectId,
                                   String activityInstanceId, String attribute1, Integer shardingId,
                                   Integer flowingObjectShardingId) throws ServiceException {
        try {
            if (StringUtils.isNotEmpty(attachmentInfo)) {
                String[] attachmentIds = attachmentInfo.split(",");
                List<TEomAttachmentRelProc> updateQueue = new ArrayList<TEomAttachmentRelProc>();
                for (String attachmentId : attachmentIds) {
                    TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
                    attachmentRelProc.setAttachmentId(attachmentId);
                    List<TEomAttachmentRelProc> attachmentRelProcList = this.findByExample(attachmentRelProc);
                    if (attachmentRelProcList != null && attachmentRelProcList.size() > 0) {
                        TEomAttachmentRelProc eomAttachmentRelProc = attachmentRelProcList.get(0);//
                        // 根据attachmentId查找附件只能有一个
                        if (flowingObjectId != null) {
                            eomAttachmentRelProc.setFlowingObjectId(flowingObjectId);
                        }
                        if (activityInstanceId != null) {
                            eomAttachmentRelProc.setActivityInstanceId(activityInstanceId);
                        }
                        if (attribute1 != null) {
                            eomAttachmentRelProc.setAttribute1(attribute1);
                        }
                        if (shardingId != null) {
                            eomAttachmentRelProc.setShardingId(shardingId);
                        }
                        if (flowingObjectShardingId != null) {
                            eomAttachmentRelProc.setFlowingObjectShardingId(flowingObjectShardingId);
                        }
                        updateQueue.add(eomAttachmentRelProc);
                    }
                }
                this.baseDAO.saveOrUpdateAll(updateQueue, userEntity);
            } /*
                 * else { throw new Exception("attachmentInfo is null or empty : " +
				 * attachmentInfo); }
				 */
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }


    /**
     * 批量复制附件关联表记录
     * @param userEntity
     * @param attachments
     * @param updateValues
     * @throws ServiceException
     */
    public void copyAttachments(UserEntity userEntity, List<TEomAttachmentRelProc> attachments,Map<String,Object> updateValues) throws ServiceException {
        List<TEomAttachmentRelProc> copyResultSet = new ArrayList<TEomAttachmentRelProc>();
        try {
            for (TEomAttachmentRelProc t : attachments) {
                TEomAttachmentRelProc targetAtt = new TEomAttachmentRelProc();
                BeanUtils.copyProperties(t, targetAtt, false);
                Set<Entry<String,Object>> pentrySet=updateValues.entrySet();
                for(Entry<String,Object> p:pentrySet){
                    BeanUtils.setPropertyValueByPName(targetAtt,p.getKey(),p.getValue());
                }
                targetAtt.setObjectId(baseDAO.getSequenceNextValue(TEomAttachmentRelProc.class));
                copyResultSet.add(targetAtt);
            }
            baseDAO.saveOrUpdateAll(copyResultSet, userEntity);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 复制附件
     * 获取源附件ID获取源附件
     * 复制源附件信息到新附件
     * 调用Paas平台上传文件服务,返回附件ID
     * @param source_attachmentId
     * @return
     * @throws ServiceException
     */
    public String copyFiles(String source_attachmentId) throws ServiceException {
        FileAdapter fileAdapter = FileAdapter.getInstance();
        String attachmentId = null;
        try {
            //获取源附件ID获取源附件
            DownloadFileInfo downloadFile = fileAdapter.download(source_attachmentId);

            //复制源附件信息到新附件
            UploadFileInfo uploadFileInfo = new UploadFileInfo();
            uploadFileInfo.setFileName(downloadFile.getFileName());
            uploadFileInfo.setDesc(downloadFile.getDesc());
            uploadFileInfo.setKeywords(downloadFile.getKeywords());
            uploadFileInfo.setOperator("operator");
            uploadFileInfo.setFileSize(downloadFile.getFileSize());
//            DataInputStream dataInputStream = new DataInputStream(downloadFile.getInput());
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(downloadFile.getByteArrayOutputStream().toByteArray()));

            // 调用Paas平台上传文件服务,返回附件ID
            attachmentId = fileAdapter.upload(Constants.STORAGE_NAME,
                    uploadFileInfo, dataInputStream);
        } catch (Exception e) {
            throw  new ServiceException(e);
        }
        return attachmentId;
    }

    private   List<ProcessInstance> getAllSubProcessInstance(String accountId,
                                                             String processInstId) throws AdapterException {
        List<ProcessInstance> feedbackProcessInstList = new ArrayList<ProcessInstance>();
        List<ProcessInstance> feedbackProcessInstList1;
        feedbackProcessInstList1 = WorkflowAdapter.getSubProcessInstance(accountId, processInstId);
        for(ProcessInstance pro:feedbackProcessInstList1){
            feedbackProcessInstList.add(pro);
            feedbackProcessInstList.addAll(getAllSubProcessInstance(accountId,pro.getProcessInstID()));
        }
        return feedbackProcessInstList;
    }
}


