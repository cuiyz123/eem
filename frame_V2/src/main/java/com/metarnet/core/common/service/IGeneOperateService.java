package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.BaseForm;
import com.metarnet.core.common.model.FdbkCommonModel;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.model.Pager;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.TaskInstance;

import java.util.List;
import java.util.Map;

/**
 * 业务处理
 */
public interface IGeneOperateService {

    public void initForm(String processCode, BaseForm baseForm, UserEntity userEntity) throws ServiceException;


    /**
     * 新建流程业务
     *
     * @param baseForm     业务实体
     * @param processCode  业务流程编码
     * @param route        路由选择
     * @param businessCode 业务类型
     * @param userEntity   当前用户
     * @throws ServiceException
     */
    public String start(BaseForm baseForm, String processCode, String route, String businessCode,Map relaDataMap,
                        UserEntity userEntity, Integer operTypeEnumId) throws ServiceException;

    /**
     * 提交流程业务
     *
     * @param baseForm   业务实体
     * @param route      路由选择
     * @param userEntity 当前用户
     * @throws ServiceException
     */
    public void submit(TaskInstance taskInstance, BaseForm baseForm, String processCode, String route,
                       UserEntity userEntity, Integer operTypeEnumId) throws ServiceException;

    public void submit(TaskInstance taskInstance, BaseForm baseForm, String processCode, String route,
                       UserEntity userEntity, List<String> list, Integer operTypeEnumId) throws ServiceException;

    /**
     * 流程挂接前处理，与流程挂接后处理（addSubPostProcess）配合使用
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体（已知的有TEomApprovalInfoRecord、com.unicom.ucloud.common.model.TEomGenProcessingInfoRec）
     * @param params       参数,目前来源只是建模时配置
     * @throws ServiceException
     */

//    public void addSubPreProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
//                                 String params) throws ServiceException;

    /**
     * 流程挂接后处理，与流程挂接前处理（addSubPreProcess）配合使用
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体（已知的有TEomApprovalInfoRecord、com.unicom.ucloud.common.model.TEomGenProcessingInfoRec）
     * @param params       参数,目前来源只是建模时配置
     * @throws ServiceException
     */

//    public void addSubPostProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
//                                  String params) throws ServiceException;

    /**
     * 保存草稿信息
     *
     * @param initForm
     * @param userEntity
     * @throws ServiceException
     */
    public void saveDraft(BaseForm initForm, UserEntity userEntity) throws ServiceException;



    /**
     * 记录流程日志
     *
     * @param taskInstance 待办信息
     * @param entity       业务实体
     * @param userEntity   当前用户
     * @throws ServiceException
     */
    public void recordLog(TaskInstance taskInstance, Object entity, UserEntity userEntity) throws ServiceException;


}
