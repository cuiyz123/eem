package com.metarnet.core.common.model;

import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.utils.Constants;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA. User: metarnet Date: 13-3-13 Time: 下午4:00
 * 解析流程引擎扩展信息
 */
public class ExtendNodeCofnig {
    public static final String COMPONENT = "component";
    public static final String LINKS = "links";
    public static final String POSTPROCESSOR = "postProcessor";
    public static final String PREPROCESSOR = "preProcessor";
    public static final String DRAFT = "draft";
    public static final String AREANAME = "areaName";
    public final static String SHOWSUBFLOW = "showSubflow";
    public static final String FEEDBACKABSTRACTROLEID = "feedbackAbstractRoleId";
    public static final String NEEDAPPROVAL = "needApproval";

    private static Map<String, Map<String, ActivityModel>> activityModelMap = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger("ExtendNodeCofnig");

    public static ActivityModel parseActivity(String cloudAccountId, String processModelName,
                                              String activityDefID) throws ServiceException, AdapterException {
        //代码健壮性
        if(StringUtils.isEmpty(activityDefID)){
            activityDefID= Constants.START_ACTIVITY;
        }
        // 先读缓存
        if (activityModelMap.containsKey(processModelName) && activityModelMap.get(processModelName).containsKey(activityDefID)) {
            return activityModelMap.get(processModelName).get(activityDefID);
        }
        //解析单个环节的配置
        ActivityModel activityModel = parseActivity(WorkflowAdapter.getActivityExtendAttributes(cloudAccountId, processModelName,
                activityDefID));
        //解析流程模板全局配置
        AreaModel areaModel = parseProcess(WorkflowAdapter.getActivityExtendAttributes(cloudAccountId, processModelName, null));
        activityModel.getShowLinkList().addAll(areaModel.getShowLinkList());
        activityModel.getEditLinkList().addAll(areaModel.getEditLinkList());
        if (StringUtils.isEmpty(activityModel.getAreaName())) {
            if (null == areaModel.getAreaName() && !Constants.START_ACTIVITY.equals(activityDefID)) {
                logger.info("当前环节没有配置areaName。processModelName=" + processModelName + " activityDefID=" + activityDefID);
            }
            activityModel.setAreaName(areaModel.getAreaName());
        }
        // 加入缓存
        if (!activityModelMap.containsKey(processModelName)) {
            HashMap<String, ActivityModel> modelHashMap = new HashMap();
            activityModelMap.put(processModelName, modelHashMap);
        }
        if (!activityModelMap.get(processModelName).containsKey(activityDefID)) {
            activityModelMap.get(processModelName).put(activityDefID,
                    activityModel);
        }
        return activityModel;
    }

    /**
     * 解析环节配置信息
     *
     * @param extendAttributes 配置信息
     * @return
     * @throws ServiceException
     */
    private static ActivityModel parseActivity(String extendAttributes)
            throws ServiceException {
        Digester digester = new Digester();
        digester.addObjectCreate("extendNodes", ArrayList.class);
        digester.addObjectCreate("extendNodes/extendNode", ExtendNode.class);
        digester.addBeanPropertySetter("extendNodes/extendNode/key");
        digester.addBeanPropertySetter("extendNodes/extendNode/value");
        digester.addBeanPropertySetter("extendNodes/extendNode/desc");
        digester.addSetNext("extendNodes/extendNode", "add");
        try {
            List<ExtendNode> extendNodes = (List<ExtendNode>) digester
                    .parse(new StringReader(extendAttributes));
            ActivityModel activityModel = new ActivityModel();
            for (ExtendNode extendNode : extendNodes) {
                if (StringUtils.isNotEmpty(extendNode.getKey()) && COMPONENT.equals(extendNode.getKey())) {
                    activityModel.setComponent(new ComponentModel(extendNode.getValue(), ComponentModel.SHOW,
                            DRAFT.equals(extendNode.getDesc())));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && LINKS.equals(extendNode.getKey())) {
                    activityModel.addLink(new Links(extendNode.getValue(), StringUtils.isEmpty(extendNode.getDesc()) ? Links.SHOW :
                            extendNode.getDesc()));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && POSTPROCESSOR.equals(extendNode.getKey())) {
                    for (String name : extendNode.getValue().split(",")) {
                        activityModel.getPostProcessorList().add(new ProcessorModel(name, extendNode.getDesc()));
                    }
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && PREPROCESSOR.equals(extendNode.getKey())) {
                    for (String name : extendNode.getValue().split(",")) {
                        activityModel.getPreProcessorList().add(new ProcessorModel(name, extendNode.getDesc()));
                    }
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && SHOWSUBFLOW.equals(extendNode.getKey())) {
                    activityModel.setShowSubflow(extendNode.getValue());
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && AREANAME.equals(extendNode.getKey())) {
                    activityModel.setAreaName(extendNode.getValue());
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && NEEDAPPROVAL.equals(extendNode.getKey())) {

                    activityModel.setNextApproverModel(new NextApproverModel(extendNode.getValue(), extendNode.getDesc()));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && FEEDBACKABSTRACTROLEID.equals(extendNode.getKey())) {
                    activityModel.setFeedbackAbstractRoleId(extendNode.getValue());
                }
            }
            return activityModel;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 解析流程配置信息
     *
     * @param extendAttributes 配置信息
     * @return
     * @throws ServiceException
     */
    public static AreaModel parseProcess(String extendAttributes) throws ServiceException {
        Digester digester = getDigester();
        try {
            List<ExtendNode> extendNodes = (List<ExtendNode>) digester.parse(new StringReader(extendAttributes));
            AreaModel areaModel = new AreaModel();
            for (ExtendNode extendNode : extendNodes) {
                if (StringUtils.isNotEmpty(extendNode.getKey()) && AREANAME.equals(extendNode.getKey()))
                    areaModel.setAreaName(extendNode.getValue());
                if (StringUtils.isNotEmpty(extendNode.getKey()) && LINKS.equals(extendNode.getKey())) {
                    areaModel.addLink(new Links(extendNode.getValue(), StringUtils.isEmpty(extendNode.getDesc()) ? Links.SHOW :
                            extendNode.getDesc()));
                    continue;
                }
            }
            return areaModel;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 获得解析规则
     *
     * @return
     */
    private static Digester getDigester() {
        Digester digester = new Digester();
        digester.addObjectCreate("extendNodes", ArrayList.class);
        digester.addObjectCreate("extendNodes/extendNode", ExtendNode.class);
        digester.addBeanPropertySetter("extendNodes/extendNode/key");
        digester.addBeanPropertySetter("extendNodes/extendNode/value");
        digester.addBeanPropertySetter("extendNodes/extendNode/desc");
        digester.addSetNext("extendNodes/extendNode", "add");
        return digester;
    }

    public static void reload() {
        activityModelMap.clear();
    }

}
