package com.metarnet.core.common.service;

import com.metarnet.core.common.exception.ServiceException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.TaskInstance;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-3-5
 * Time: 上午11:08
 * 流程环节处理扩展接口
 */
public interface IWorkflowProcessor {

    /**
     * 环节自定义的扩展处理
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体（已知的有TEomApprovalInfoRecord、com.unicom.ucloud.common.model.TEomGenProcessingInfoRec）
     * @param params       参数,目前来源只是建模时配置
     * @throws ServiceException
     */

    public void execute(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
                        String params) throws ServiceException;

}
