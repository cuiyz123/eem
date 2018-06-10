package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.*;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.filters.TaskFilter;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.ProcessModelParams;
import com.unicom.ucloud.workflow.objects.TaskInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 12-12-12
 * Time: 下午2:59
 * 工作流基本服务接口，负责封装与工作流相关的通用功能，如待办、在办、审核功能等。
 */
public interface IWorkflowBaseService {
    /**
     * 查询待办
     *
     * @param taskFilter 待办查询实体
     * @param accountId  当前用户ID
     * @return 待办列表
     * @throws ServiceException
     */

    public List<TaskInstance> getMyWaitingTasks(TaskFilter taskFilter, String accountId) throws ServiceException;


    /**
     * 启动流程
     *
     * @param userEntity         当前登录用户
     * @param entity             业务实体
     * @param processModelID     流程模型ID
     * @param participant        参与者
     * @param bizModleParams     业务参数
     * @param processModelParams 流程模型对象参数
     * @return
     * @throws ServiceException
     */
    public String startProcess(UserEntity userEntity, Object entity, String processModelID, Participant participant,
                               Map<String, Object> bizModleParams, ProcessModelParams processModelParams) throws
            ServiceException;


    /**
     * 修改功能标题，驳回的工单在标题尾部加“-驳回”
     *
     * @param taskInstance
     * @param userEntity
     * @param rejectTag    驳回标示
     * @throws AdapterException
     */
    public void handleJobTitle(TaskInstance taskInstance, UserEntity userEntity,
                               boolean rejectTag) throws AdapterException;


    /**
     * 根据输入参数获取子流程名称并追加
     *
     * @param businessCode 业务类型
     * @param productCode  产品
     * @param majorCode    专业
     * @param orgCode      组织
     * @param userEntity   操作人
     * @param taskInstance 待办信息
     * @param params       子流程启动参数
     * @return
     */
    public String addSubProcess(String businessCode, String productCode, String majorCode, String orgCode,
                                UserEntity userEntity, TaskInstance taskInstance, String activityDefID,
                                LinkedHashMap params) throws ServiceException;

    /**
     * 根据输入参数获取子流程名称并追加
     *
     * @param businessCode  业务类型
     * @param productCode   产品
     * @param majorCode     专业
     * @param orgCode       组织
     * @param attribute1    扩展属性
     * @param userEntity    操作人
     * @param taskInstance  待办信息
     * @param activityDefID
     * @param params        子流程启动参数
     * @return
     */
    public String addSubProcess(String businessCode, String productCode, String majorCode, String orgCode,
                                String attribute1, UserEntity userEntity, TaskInstance taskInstance,
                                String activityDefID, LinkedHashMap params) throws ServiceException;

    /**
     * 根据活动环节ID获取横向扩展信息
     *
     * @param activityInstId 活动环节ID
     * @param processInstID  流程实例ID
     * @param shard          分片ID
     * @return
     * @throws ServiceException
     */
    public List<Map> getTransverseInfo(String activityInstId, String processInstID, String shard) throws ServiceException;

    /**
     * 提交待办，并执行环节自定义的扩展处理
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体
     * @param userEntity   当前登录用户
     * @throws ServiceException
     */
    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity,
                           UserEntity userEntity , Integer operTypeEnumId) throws ServiceException;

    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity,
                           UserEntity userEntity, List<String> list , Integer operTypeEnumId) throws ServiceException;

    /**
     * 提交待办，并执行环节自定义的扩展处理
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体
     * @param route        提交还是保存
     * @throws ServiceException
     */
    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity,
                           UserEntity userEntity, String route , Integer operTypeEnumId) throws ServiceException;

    /**
     * 48.	根据环节实列结束指定环节
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体
     */
    public void finishActivityInstance(TaskInstance taskInstance, List<Participant> participants, Object entity,
                                       UserEntity userEntity) throws ServiceException;

    /**
     * 设置五个维度到相关数据区中，
     *
     * @param areacode      区域
     * @param orgcode       组织
     * @param majorcode     专业
     * @param productcode   产品
     * @param roleclass     角色
     * @param processInstID 流程实例ID
     * @return
     */
    public void setNextPersonMap(String areacode, String orgcode, String majorcode, String productcode,
                                 String roleclass, String processInstID, String accountId) throws ServiceException;


    /**
     * 根据流程实例Id查询任务实例
     *
     * @param processId
     * @param accountId
     * @return
     * @throws ServiceException
     */
    public TaskInstance getTaskInstance(String processId, String accountId) throws ServiceException;


    /**
     * 重新启动已完成的子流程
     *
     * @param subProcessInstIDMap 需要重启的子流程map<子流程实例id, 子流程环节定义名称>
     * @param relativeMap         需要设置的流程相关数据
     * @param taskInstance
     * @param accountId
     * @param participants        参与者
     * @throws ServiceException
     */
    public void restartSubProcess(Map<String, String> subProcessInstIDMap, Map<String,
            Object> relativeMap, TaskInstance taskInstance, String accountId, List<Participant> participants) throws
            ServiceException;

    /**
     * 指定下一步操作人
     *
     * @param ProcessInstId
     * @param participants
     * @param userEntity
     * @throws ServiceException
     */
    public void setNextParticipant(String ProcessInstId, List<Participant> participants, UserEntity userEntity, TaskInstance taskInstance) throws ServiceException;

    public void setNextParticipant(String ProcessInstId, List<Participant> participants, UserEntity userEntity, List<String> list, TaskInstance taskInstance) throws ServiceException;


    public List<GeneralInfoModel> getGeneralInfoByRootProcessId(String rootProcessId) throws ServiceException;

    /**
     * 获得根流程下所有当前处理环节信息，处理人信息,与工单日志公用一个model
     *
     * @param rootProcessId 根流程实例ID
     * @param jobID
     * @return
     */
    public List<GeneralInfoModel> getAllActivityInstanceInfos(String rootProcessId, String jobID , UserEntity user) throws ServiceException;

    /**
     * 生成调度单工单编号
     *
     * @param userEntity    用户Id
     * @param speciality    调单所属专业名称，如"传输"、"综合"
     * @param businessType  调单业务类型,除重保模块外，其他模块传入null,重保分为重保/预案制作
     * @param sequenceValue 工单编号中的序号
     * @return
     * @throws ServiceException
     */

    public String generateDisWorkOrderCode(UserEntity userEntity, String speciality, String businessType, Long sequenceValue) throws ServiceException;

    /**
     * 通用业务处理
     *
     * @param generalInfoModel 通用信息
     * @throws ServiceException
     */
    public void saveGeneralProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity , String participant) throws ServiceException;
    public void saveGeneralProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;


    public void setGeneralInfo(BaseForm baseForm, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;

    public void setBelongInfo(BaseForm baseForm, UserEntity userEntity) throws ServiceException;

    public void addSubPreProcess(TaskInstance taskInstance, List<Participant> participants, UserEntity userEntity, String params) throws ServiceException;

    public void addSubPostProcess(TaskInstance taskInstance, List<Participant> participants, UserEntity userEntity, String params , List<FdbkCommonModel> fdbkList) throws ServiceException;

    /**
     * 保存通用信息处理信息，同时也用于流程监控日志查询
     *
     *
     *
     * @param generalInfoModel  通用信息实例，必须传入processingObjectID、processingObjectTable、operTypeEnumId
     * @param taskInstance
     * @param userEntity
     * @throws ServiceException
     */
    public void saveGeneralInfo(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException;

    public void saveGeneralInfo(BaseForm baseForm, TaskInstance taskInstance, UserEntity userEntity , Integer operTypeEnumId) throws ServiceException;

    public Pager queryWorkOrderList(Pager pager , UserEntity userEntity) throws ServiceException;

    /**
     * 该方法用于工单查询
     * 根据流程实例Id查询任务实例，用与工单查询时
     *
     * @param processId
     * @param accountId
     * @return
     * @throws ServiceException
     */
    public TaskInstance getTaskInstance2(String processId, String accountId) throws ServiceException;

    /***
     * 通过根流程id,h获取相应的申请单和调度单
     * @param request
     * @param rootProcessInstID
     * @throws ServiceException
     * @throws DAOException
     */
    public void queryAppAndDisByRootProcess(HttpServletRequest request,String rootProcessInstID) throws ServiceException, DAOException;

}
