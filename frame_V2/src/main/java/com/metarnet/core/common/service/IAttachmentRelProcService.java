package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Company: Metarnet
 * User: kans
 * Date: 13-4-22
 * Time: 下午5:30
 * Description: 流程附件关联表实体对象服务层接口.
 */
public interface IAttachmentRelProcService {

    /**
     * 实现功能:上传附件到pass平台指定存储区,保存附件件记录信息
     *
     * @param object      保存的附件对象
     * @param request     HttpServletRequest
     * @param storageName 存储区名
     * @param keyWords    关键字
     * @param operator    操作
     * @param userEntity  用户
     * @return 保存的附件对象
     * @throws ServiceException
     */
    public TEomAttachmentRelProc saveFileAndUploadToPass(Object object, HttpServletRequest request,
                                                         String storageName, String keyWords,
                                                         String operator, UserEntity userEntity) throws
            ServiceException;

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
    public String insertAttachmentToPass(MultipartFile file, String storageName, String keyWords,
                                         String operator) throws ServiceException;

    /**
     * 保存流程附件
     *
     * @param attachmentRelProc 附件对象
     * @param userEntity        用户
     * @return 附件对象
     * @throws ServiceException
     *
     */
    public TEomAttachmentRelProc save(TEomAttachmentRelProc attachmentRelProc,
                                      UserEntity userEntity) throws ServiceException;

    /**
     * 删除流程附件
     *
     * @param attachmentRelProcList 附件集合
     * @param userId                用户ID
     * @throws ServiceException
     *
     */
    public void deleteAttachments(List<TEomAttachmentRelProc> attachmentRelProcList,
                                  int userId) throws ServiceException;

    /**
     * 流程附件查询
     *
     * @param attachmentRelProc 查询对象
     * @return
     * @throws ServiceException
     *
     */
    public List<TEomAttachmentRelProc> findByExample(TEomAttachmentRelProc attachmentRelProc) throws ServiceException;

    /**
     * 流程附件查询
     *
     * @param attachmentRelProcList
     * @return 流程附件对象列表
     * @throws ServiceException
     */
    public List<TEomAttachmentRelProc> findByExample(List<TEomAttachmentRelProc> attachmentRelProcList) throws
            ServiceException;

    /**
     * 更新流程附件记录
     *
     * @param attachmentRelProcList 附件记录列表
     * @param attachmentRelProc     附件更新内容对象
     * @param userId                用户ID
     * @throws ServiceException
     *
     */
    public void updateAttachments(List<TEomAttachmentRelProc> attachmentRelProcList,
                                  TEomAttachmentRelProc attachmentRelProc, int userId) throws ServiceException;

    /**
     * 实现功能:根据前台回传附件信息,更新附件列表
     * 使用说明:前台参数attachmentInfo字符串为已上传附件的attachmentId值,多个以逗号分隔
     *
     * @param userEntity              用户实体对象
     * @param attachmentInfo          回传附件信息
     * @param flowingObjectId         关联对象主键
     * @param activityInstanceId      活动(环节)实例ID
     * @param taskInstanceId          任务实例ID
     * @param shardingId              附件关联表分片ID
     * @param flowingObjectShardingId 流转对象分片ID
     * @throws ServiceException
     * @examle :attachmentInfo = "3udf-4iooo-sdsad,3udf-4iooo-sdsad,3udf-4iooo-sdsad"
     */
    public void updateAttachements(UserEntity userEntity, String attachmentInfo, Long flowingObjectId,
                                   String activityInstanceId, String taskInstanceId, Integer shardingId,
                                   Integer flowingObjectShardingId) throws ServiceException;

    /**
     * 批量复制附件关联表记录
     *
     * @param userEntity
     * @param attachments
     * @throws ServiceException
     */
    public void copyAttachments(UserEntity userEntity, List<TEomAttachmentRelProc> attachments, Map<String, Object> updateValues) throws ServiceException;

    /**
     * 复制附件
     * 获取源附件ID获取源附件
     * 复制源附件信息到新附件
     * 调用Paas平台上传文件服务,返回附件ID
     * @param source_attachmentId
     * @return
     * @throws ServiceException
     */
    public String copyFiles(String source_attachmentId) throws ServiceException;

}
