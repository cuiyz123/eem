package com.metarnet.core.common.utils;

import com.primeton.ucloud.workflow.impl.Global;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-1-17
 * Time: 上午11:33
 * 系统常量，共分四个区域
 * #1  框架基本常量
 * #2  数据库常量 与应用相关
 * #3  流程引擎基本常量 与应用无关
 * #4  流程引擎运行常量 与应用相关
 * #5  其他
 */
public class Constants {

    public static boolean debug = false;

    /**
     * ------------------#1 框架基本常量 ---------------
     */
    //调单类名
    public static String APP_MODEL;
    public static String DIS_MODEL;
    public static String FDBK_MODEL;             //反馈实体
    public String appModel; //申请单实体
    public String disModel; //调度单实体
    public String fdbkModel;//反馈单实体

    public static final String Y = "Y";
    public static final String N = "N";
    //系统名称
    public static final String APPLICATION_NAME = "EOM";
    //转派枚举后缀
    public static final String TURN_DIS = "TURN_DIS";
    //转办枚举后缀
    public static final String FORWARDTASK = "FORWARDTASK";
    //会签枚举后缀
    public static final String COUNTERSIGN = "COUNTERSIGN";
    //审核信息
    public static final String PASS_DISPLAY = "通过";
    public static final String REJECT_DISPLAY = "驳回";
    public static final String PASS_CODE = "0";
    public static final String REJECT_CODE = "1";
    //催办方式为短信
    public static final String REMIND_TYPE_SMS = "sms";
    //催办方式为邮件
    public static final String REMIND_TYPE_MAIL = "mail";
    //TEomGenProcessingInfoRec记录的功能-->业务信息
    public static final String PROCESS_INFO_TYPE_OPER = "b";
    //TEomGenProcessingInfoRec记录的功能-->日志信息
    public static final String PROCESS_INFO_TYPE_LOG = "l";

    //报表平台地址
    public static String REPORT_DEVELOP_ENVIRONMENT;
    /**
     * ------------------#2 数据库常量 与应用相关-------------
     */

    //删除标记
    public static final String DELETED_FLAG = "deletedFlag";
    //删除日期
    public static final String DELETION_TIME = "deletionTime";
    //删除人
    public static final String DELETED_BY = "deletedBy";
    //创建人
    public static final String CREATED_BY = "createdBy";
    //版本
    public static final String RECORD_VERSION = "recordVersion";
    //组织编号
    public static final String ORG_ID = "orgId";
    //创建时间
    public static final String CREATION_TIME = "creationTime";
    //更新人
    public static final String LAST_UPDATED_BY = "lastUpdatedBy";
    //更新时间
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    //归档日期
    public static final String ARCHIVE_BASE_DATE = "archiveBaseDate";
    //归档日期数据库列名
    public static final String ARCHIVE_BASE_DATE_COLUMN_NAME = "ARCHIVE_BASE_DATE";
    //归档日期默认值
    public static final String ARCHIVE_BASE_DATE_VALUE = "2099-12-31 00:00:00";
    //营销区域
    public static final String MAINTENANCE_AREA_ID = "maintenanceAreaId";
    //维护区域
    public static final String MARKETING_AREA_ID = "marketingAreaId";
    //ADB中分片ID字段
    public static final String ADB_SHARDING_ID = "shardingId";
    public static final String FLOWING_OBJECT_SHARDING_ID = "flowingObjectShardingId";
    public static final String DIS_OBJECT_SHARDING_ID = "disObjectShardingId";
    public static final String IFT_SHARDING_ID = "iftShardingId";


    /**
     * ------------------#3  流程引擎基本常量 与应用无关-------------
     */
    //流程开始环节活动定义名称
    public final static String START_ACTIVITY = "startActivity";
    //流程连接重新启动子流程环节的自动环节活动定义名称
    public final static String RESTART_ACTIVITY = "reStartActivity";
    //流程实例状态--运行
    public static final String PIS_RUN = "1";
    //流程实例状态--等待
    public static final String PIS_WAIT = "2";
    //流程实例状态--挂起
    public static final String PIS_SLEEP = "3";
    //流程实例状态--完成
    public static final String PIS_FINISH = "4";
    //流程实例状态--终止
    public static final String PIS_STOP = "5";
    //流程实例状态--异常
    public static final String PIS_EXCEPTION = "6";
    //活动实例状态--未启动
//    public static final String ACT_NOTSTART="1";
    //活动实例状态--运行
    public static final String ACT_RUN = "1";
    //活动实例状态--运行
    public static final String ACT_WAIT = "2";
    //活动实例状态--挂起
    public static final String ACT_SLEEP = "3";
    //活动实例状态--完成
    public static final String ACT_FINISH = "4";
    //活动实例状态--终止
    public static final String ACT_STOP = "5";
    //活动实例状态--异常
    public static final String ACT_PIS_EXCEPTION = "6";
    //活动定义类型--开始活动
    public final static String ACT_TYPE_START = "start";
    //活动定义类型--人工活动
    public static final String ACT_TYPE_MANUAL = "manual";
    //活动定义类型--子流程活动
    public final static String ACT_TYPE_SUBFLOW = "subflow";
    //业务表参数
    //业务表键
    public static final String BIZ_TABLE_NAME = "bizTableName";
    //业务表值
    public static final String BIS_TABLE_NAME_VALUE = Global.BIZ_TABLE_NAME;
    //JobId的键名称
    public static final String JOB_ID = Global.JOB_ID;
    //JobCode的键名称
    public static final String JOB_CODE = Global.JOB_CODE;
    public static final String ACTION_TYPE = "actionType";
    //JobTitle的键名称
    public static final String JOB_TITLE = Global.JOB_TITLE;
    public static final String JOB_STARTTIME = Global.JOB_STARTTIME; //业务参数 工单开始时间
    public static final String JOB_ENDTIME = Global.JOB_ENDTIME; //业务参数 工单结束时间
    public static final String RE_BACKTIME = Global.RE_BACKTIME; //业务参数 反馈时间
    public static final String DA_COLUMN1 = Global.DA_COLUMN1; //业务参数 预留扩展时间字段1
    public static final String DA_COLUMN2 = Global.DA_COLUMN2; //业务参数 预留扩展时间字段2
    public static final String BUSINESS_ID = Global.BUSINESS_ID; //工单涉及的业务
    //待办扩展字段
    public static final String BIZ_SENDERID = Global.BIZ_SENDERID;
    public static final String BIZ_RECEIVERID = Global.BIZ_RECEIVERID;
    public static final String BIZ_STRCOLUMN1 = Global.BIZ_STRCOLUMN1;
    public static final String BIZ_STRCOLUMN2 = Global.BIZ_STRCOLUMN2;
    public static final String BIZ_STRCOLUMN3 = Global.BIZ_STRCOLUMN3;
    public static final String BIZ_STRCOLUMN4 = Global.BIZ_STRCOLUMN4;
    public static final String BIZ_STRCOLUMN5 = Global.BIZ_STRCOLUMN5;
    public static final String BIZ_STRCOLUMN6 = Global.BIZ_STRCOLUMN6;
    public static final String BIZ_STRCOLUMN7 = Global.BIZ_STRCOLUMN7;
    public static final String BIZ_DATCOLUMN1 = Global.BIZ_DATCOLUMN1;
    public static final String BIZ_DATCOLUMN2 = Global.BIZ_DATCOLUMN2;
    public static final String BIZ_NUMCOLUMN1 = Global.BIZ_NUMCOLUMN1;
    public static final String BIZ_NUMCOLUMN2 = Global.BIZ_NUMCOLUMN2;
    public static final String BIZ_ROOTVCCOLUMN1 = Global.BIZ_ROOTVCCOLUMN1;
    public static final String BIZ_ROOTVCCOLUMN2 = Global.BIZ_ROOTVCCOLUMN2;
    public static final String BIZ_ROOTNMCOLUMN1 = Global.BIZ_ROOTNMCOLUMN1;
    public static final String BIZ_ROOTNMCOLUMN2 = Global.BIZ_ROOTNMCOLUMN2;
    /**
     * ------------------#4  流程引擎运行常量 与应用相关-------------
     */
    //是否是根流程
    public static final String IS_ROOT = "isRoot";
    //开始环节路由(保存)
    public static final String ROUTE_START_SAVE = "save";
    //开始环节路由(提交)
    public static final String ROUTE_START_SUBMIT = "submit";
    //路由
    public static final String ROUTE = "route";
    //来源
    public static final String SOURCE = "source";
    //工单对应组件
    public static final String COMPONENT = "component";
    //流程来源为申请
    public static final String PROCESS_START_UP = "up";
    //流程来源为上级调度
    public static final String PROCESS_START_ISSUE = "issue";
    //流程来源为上级调度,且此级为调度到人的情况
    public static final String PROCESS_START_NISSUE = "nIssue";
    //流程来源为上级调度,且此级为调度部门的情况
    public static final String PROCESS_START_DEPT = "DEPT";
    //流程来源为上级调度,且此级为调度到分公司的情况
    public static final String PROCESS_START_ORG = "ORG";
    //当前流程对应的组织名称
    public static final String ORG_NAME = "orgName";
    //当前流程对应的组织编码
    public static final String ORG_CODE = "orgCode";
    //上级部门审核人参与者
    public static final String PARENTDISPATCHER = "parentDispatcher";
    //用于流程挂接时传递上级部门审核人参与者
    public static final String SUBPARENTDISPATCHER = "subParentDispatcher";

    //调单下发是子流程中存放派发表的ID
    public static final String OBJ_DISASSIGN = "disassignObj";
    //本级流程所关联调单ID
    public static final String RELATEDDISPATCHID = "relatedDispatchId";
    public static final String RELATED_APP_ID = "RELATED_APP_ID";
    //下级流程所关联的掉单ID
    public static final String SUB_RELATEDDISPATCHID = "subRelatedDispatchId";
    //默认关联的调单ID
    public static final String DEFAULT_RELATEDDISPATCHID = "-1";

    //此流程为反馈
    public static final String PROCESS_START_FDBK = "fdbk";
    //用于出发公用挂接流程
    public static final String DEPARTMENTS = "departments";
    //用于设置抄送对象
    public static final String COPY = "copy";
    //审核结果
    public static final String approvalStatus = "approvalStatus";
    //子流程审核结果
    public static final String approvalStatusSub = "approvalStatusSub";
    //签发结果
    public static final String ausstellungResult = "ausstellungResult";

    //转办、转派、会签等通用处理类型操作需要保存相关主流程的activityDefID
    public static final String ROOT_ACTIVITY_DEF_ID = "ROOT_ACTIVITYDEFID";
    //转办、转派、会签等通用处理类型操作需要保存相关主流程的processInstID
    public static final String ROOT_PROCESS_INST_ID = "ROOT_PROCESSINSTID";
    //流程引擎中的变量名称：当前流程对应的表名
    public static final String OBJECT_TABLE = "OBJECT_TABLE";
    //流程引擎中的变量名称：当前流程对应的对象ID
    public static final String OBJECT_ID = "OBJECT_ID";
    //流程引擎中的变量名称：分片ID
    public static final String SHARDING_ID = "shard";
    //上一步环节参与者
    public static final String CREATE_USER = "create_user";

    //派到人的子流程相关数据区存用户的键【流程建模使用】
    public static final String USER_CODE = "userCode";
    //五个维度之组织
    public static final String DIMENSION_ORG_CODE = "orgcode";
    //五个维度之专业
    public static final String DIMENSION_MAJOR_CODE = "majorcode";
    //五个维度之产品
    public static final String DIMENSION_PRODUCT_CODE = "productcode";
    //五个维度之角色
    public static final String DIMENSION_ROLE_CLASS = "roleclass";
    //五个维度之区域
    public static final String DIMENSION_AREA_CODE = "areacode";

    //业务流程编码
    public static final String PROCESS_CODE = "processCode";
    //业务类型
    public static String BUSINESS_CODE;
    public String businessCode;
    //转派的派发类型
    public static final String DISPATCH_TYPE = "operationAction";

    //查找下一步操作者使用【4A使用】
    //五个维度之组织
    public static final String DIMENSION_ORG_ID = "ORG_ID";
    //五个维度之专业
    public static final String DIMENSION_MAJOR_ID = "MAJOR_ID";
    //五个维度之产品
    public static final String DIMENSION_PRODUCT_ID = "PRODUCT_ID";
    //五个维度之角色
    public static final String DIMENSION_ABSTRACT_ROLE_ID = "ABSTRACT_ROLE_ID";
    //五个维度之区域
    public static final String DIMENSION_AREA_ID = "AREA_ID";
    //第一步参与者
    public static final String FIRST_STEP_USER = "firstStepUser";
    //第一个参与者的orgCode
    public static final String FIRST_ORG_CODE = "firstOrgCode";
    //第一个参与者的accountId
    public static final String FIRST_ACCOUNTID = "firstAccountId";
    //子流程启动参数名称
    public static final String DIMENSION_JSON = "dimensionJson";
    //流程启动时 传入的第一个环节的参与者
    public static final String NEXT_PARTICIPANT_RDPATH = "nextParticipant";
    //抄送列表
    public static final String CC_PARTICIPANT = "CCParticipant";
    //原始orgCode，审核时提升或降低orgCode
    public static final String ORIGINAL_ORG_CODE = "originalOrgcode";
    //派发对象类型，是人还是部门
    public static final String DISPATCH_OBJECT_TYPE = "dispatchObjectType";
    public static final String DISPATCH_OBJECT_TYPE_ORG = "ORG";
    public static final String DISPATCH_OBJECT_TYPE_MEMBER = "MEMBER";
    //签发和审核进子流程的时候用于记录前台提示的字符串
    public static final String ERROR_NAME_FOR_DISPATCH = "errorNameForDispatch";

    public final static String ROOT_APP_ID = "ROOT_APP_ID";

    public final static String ROOT_DISPATCH_ID = "ROOT_DISPATCH_ID";
    public final static String PARENT_DISPATCH_ID = "PARENT_DISPATCH_ID";
    public final static String DISPATCH_ID = "DISPATCH_ID";
    public final static String APPLY_ID = "APPLY_ID";

    public static String SESSIONURL;
    public static String POWERURL;
    public static String ENUMURL;
    //是否需要升级处理
    public static final String REQUIRE_UP = "requireUp";
    //是否分片
    public static Boolean IS_SHARDING = false;
    //流程实例
    public static String STORAGE_NAME;
    //流程模板
    public static String PROCESS_MODELS;
    //归档工单表名
    public static String ARCHIVE_TABLE_NAMES;
    //调单业务类型
    public static String BUSINESS_TYPE;
    //业务类型编码
    public static String BUSINESS_TYPE_CODE;
    //页面是否指显示第一个area
    public static Boolean SHOW_FIRST = false;
    //流程引擎API参与者类型：用户
    public static String PARTICIPANT_USER_TYPE = "1";
    //流程引擎API参与者类型：组织
    public static String PARTICIPANT_ORG_TYPE = "3";
    //中国联通总部的orgId
    public static Integer GROUP_COMPANY_ORG_ID;
    //租户名称
    public static String WORKFLOW_LESSEE;
    //应用ID
    public static String APP_ID;
    //模块名称
    public static String MODEL_NAME;
    //模块编码
    public static String MODEL_CODE;
    //消息监听目的地
    public static String DESTINATION;

    //派发树是否过滤人
    public static boolean filterMember;
    //暂定新反馈单上级审核环节
    public static String suCheckActivityDefID;

    /**
     * ------------------#5  其他常量 与应用相关-------------
     */
    //是否上报
    public static String WHETHER_REPORT = "whetherReport";
    //流程挂接流程类型_申请单
    public static String PROCESS_EXTEND_ATTRIBUTE_APP = "apply";
    //流程挂接流程类型_调度单
    public static String PROCESS_EXTEND_ATTRIBUTE_DIS = "dispatch";
    //流程挂接流程类型_反馈单
    public static String PROCESS_EXTEND_ATTRIBUTE_FDBK = "fdbk";
    //处理类型
    public static final String PROCESSING_TYPE = "processingType";
    //处理类型-撤回
    public static final String PROCESSING_TYPE_RETURN = "RETURN";
    //处理类型-撤单
    public static final String PROCESSING_TYPE_CANCEL = "CANCEL";
    //是否分片
    private Boolean isSharding;
    //流程实例
    private String storageName;
    //流程模板
    private String processModels;
    //归档工单表名称
    private String archiveTableNames;
    //调单业务类型
    private String businessType;
    //页面是否指显示第一个area
    private Boolean showFirst = false;
    //中国联通总部的orgId
    private Integer groupCompanyOrgId;
    //租户名称
    private String workflowLessee;
    //应用ID
    private String appID;
    //模块名称
    private String modelName;
    //模块编码
    private String modelCode;
    //消息监听目的地
    private String destination;
    //报表服务器地址
    private String reportDevelopEnvironment;

    //业务类型编码
    private String businessTypeCode;

    private String sessionUrl;

    private String powerUrl;

    private String enumUrl;

    public static String FDBK_ID = "objectId";         //反馈单主键
    public static String REQ_FDBK_DATETIME;         //要求反馈时间
    public static String FDBK_DATETIME;             //实际反馈时间
    public static String FDBK_OPERATOR_ID;             //反馈操作人ID
    public static String FDBK_OPERATOR;             //反馈操作人
    public static String FDBK_SHOW_COMPONENT;             //反馈单详情组件
    public static String FDBK_PROCESS_MODEL;             //反馈流程定义名称
    public static String FDBK_LIST_SHOW_ONLY_ACT_DEF_ID; //显示反馈单审核列表只读形式的活动定义节点ID
    public static Map fdbkListShowOnlyActDefID = new Hashtable();

    public static final String HAVA_FDBK = "已反馈";
    public static final String NO_FDBK = "未反馈";
    public static final String REJECT = "已驳回";

    public static String COPY_SEND_ACT_DEF_ID = "trun_id";
    public static String COPY_SEND_PROC_MODEL_DEF_ID = "FAULT_SENDANDDISPATH";
    private String copySendActDefID;
    private String copySendProcModelDefID;

    //MQ消息队列链接串
    public static String MQ_CONN_URL;
    public static String QUEUE_NAME;
    private String mqConnUrl;
    private String queueName;


    //FTP配置信息
    public static String ftpServer;
    public static Integer ftpPort;
    public static String ftpUsername;
    public static String ftpPassword;
    public static String ftpUpDirectory;
    public static String ftpDownDirectory;

    //是否分组查询
    private String isGroupby;
    //分组字段
    private String groupbyColumn;

    /**
     * 待办、已办、待阅、已阅是否需要显示专业
     */
    private Boolean showMajor;

    private Boolean showForm;

    private String feedBackMethod;
    private String appMethod;
    private String disMethod;

    private Boolean showFeedBackList;

    public static String PROCESS_MODEL_NAME;
    private String processModelName;

    public static String DISPATCH_LINK_NAME;
    private String dispatchLinkName;





    public static String IS_GROUPBY;
    public static String GROUPBY_COLUMN;
    public static Boolean IS_SHOW_MAJOR = false;
    private String fdbk_list_show_only_act_def_id;

    //单子详情，是否自己模块根据流程id获取申请单或者调度单form
    public static Boolean IS_SHOW_FORM = false;
    //工单全景图  是否显示反馈列表还是反馈单
    public static Boolean IS_SHOW_FEEDBACK_LIST = true;

    //feedbackShow.do  类似的control的方法参数
    public static String FEEDBACK__METHOD;
    //工单全景图 查看申请单方法 类似的control的方法参数
    public static String APP__METHOD;
    //工单全景图 查看调度单方法 类似的control的方法参数
    public static String DIS__METHOD;

    //通用挂接子流程
    public static String HANGING;
    public String hanging;

    public static String APP_TABLE;
    public static String DIS_TABLE;
    public static String FDBK_TABLE;
    public String appTable;
    public String disTable;
    public String fdbkTable;

    public static String APP_SQL;
    public static String DIS_SQL;
    public String appSql;
    public String disSql;

    public String getFeedBackMethod() {
        return feedBackMethod;
    }

    public void setFeedBackMethod(String feedBackMethod) {
        this.feedBackMethod = feedBackMethod;
        FEEDBACK__METHOD = feedBackMethod;
    }

    public Boolean getShowForm() {
        return showForm;
    }

    public void setShowForm(Boolean showForm) {
        this.showForm = showForm;
        IS_SHOW_FORM = showForm;
    }

    public static Boolean getIsShowForm() {

        return IS_SHOW_FORM;
    }

    public Boolean getShowFeedBackList() {
        return showFeedBackList;
    }

    public void setShowFeedBackList(Boolean showFeedBackList) {
        this.showFeedBackList = showFeedBackList;
        IS_SHOW_FEEDBACK_LIST = showFeedBackList;
    }

    public static void setIsShowForm(Boolean isShowForm) {
        IS_SHOW_FORM = isShowForm;
    }

    public void setIsGroupby(String isGroupby) {
        this.isGroupby = isGroupby;
        IS_GROUPBY = isGroupby;
    }

    public void setGroupbyColumn(String groupbyColumn) {
        this.groupbyColumn = groupbyColumn;
        GROUPBY_COLUMN = groupbyColumn;
    }

    public void setShowMajor(Boolean showMajor) {
        this.showMajor = showMajor;
        IS_SHOW_MAJOR = showMajor;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public Integer getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(Integer ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpUpDirectory() {
        return ftpUpDirectory;
    }

    public void setFtpUpDirectory(String ftpUpDirectory) {
        this.ftpUpDirectory = ftpUpDirectory;
    }

    public String getFtpDownDirectory() {
        return ftpDownDirectory;
    }

    public void setFtpDownDirectory(String ftpDownDirectory) {
        this.ftpDownDirectory = ftpDownDirectory;
    }

    public String getBusinessTypeCode() {
        return businessTypeCode;
    }

    public void setBusinessTypeCode(String businessTypeCode) {
        this.businessTypeCode = businessTypeCode;
    }

    public Boolean getIsSharding() {
        return isSharding;
    }

    public void setIsSharding(Boolean sharding) {
        isSharding = sharding;
        IS_SHARDING = sharding;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
        STORAGE_NAME = storageName;
    }

    public String getProcessModels() {
        return processModels;
    }

    public void setProcessModels(String processModels) {
        this.processModels = processModels;
        PROCESS_MODELS = processModels;
    }

    public String getArchiveTableNames() {
        return archiveTableNames;
    }

    public void setArchiveTableNames(String archiveTableNames) {
        this.archiveTableNames = archiveTableNames;
        ARCHIVE_TABLE_NAMES = archiveTableNames;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
        BUSINESS_TYPE = businessType;
    }

    public Boolean getShowFirst() {
        return showFirst;
    }

    public void setShowFirst(Boolean showFirst) {
        this.showFirst = showFirst;
        SHOW_FIRST = showFirst;
    }

    public Integer getGroupCompanyOrgId() {
        return groupCompanyOrgId;
    }

    public void setGroupCompanyOrgId(Integer groupCompanyOrgId) {
        this.groupCompanyOrgId = groupCompanyOrgId;
        GROUP_COMPANY_ORG_ID = groupCompanyOrgId;
    }

    public String getWorkflowLessee() {
        return workflowLessee;
    }

    public void setWorkflowLessee(String workflowLessee) {
        this.workflowLessee = workflowLessee;
        WORKFLOW_LESSEE = workflowLessee;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
        APP_ID = appID;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
        MODEL_NAME = modelName;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
        MODEL_CODE = modelCode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
        DESTINATION = destination;
    }

    public String getReportDevelopEnvironment() {
        return reportDevelopEnvironment;
    }

    public void setReportDevelopEnvironment(String reportDevelopEnvironment) {
        this.reportDevelopEnvironment = reportDevelopEnvironment;
        REPORT_DEVELOP_ENVIRONMENT = reportDevelopEnvironment;
    }

    public static boolean isFilterMember() {
        return filterMember;
    }

    public static void setFilterMember(boolean filterMember) {
        Constants.filterMember = filterMember;
    }

    public String getSuCheckActivityDefID() {
        return suCheckActivityDefID;
    }

    public void setSuCheckActivityDefID(String suCheckActivityDefID) {
        Constants.suCheckActivityDefID = suCheckActivityDefID;
    }

    public static void setDebug(boolean debug) {
        Constants.debug = debug;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public void setSessionUrl(String sessionUrl) {
        this.sessionUrl = sessionUrl;
        SESSIONURL = sessionUrl;
    }

    public String getPowerUrl() {
        return powerUrl;
    }

    public void setPowerUrl(String powerUrl) {
        this.powerUrl = powerUrl;
        POWERURL = powerUrl;
    }

    public String getEnumUrl() {
        return enumUrl;
    }

    public void setEnumUrl(String enumUrl) {
        this.enumUrl = enumUrl;
        ENUMURL = enumUrl;
    }

    public void setREQ_FDBK_DATETIME(String REQ_FDBK_DATETIME) {
        Constants.REQ_FDBK_DATETIME = REQ_FDBK_DATETIME;
    }

    public void setFDBK_DATETIME(String FDBK_DATETIME) {
        Constants.FDBK_DATETIME = FDBK_DATETIME;
    }

    public void setFDBK_OPERATOR(String FDBK_OPERATOR) {
        Constants.FDBK_OPERATOR = FDBK_OPERATOR;
    }

    public void setFDBK_OPERATOR_ID(String FDBK_OPERATOR_ID) {
        Constants.FDBK_OPERATOR_ID = FDBK_OPERATOR_ID;
    }

    public void setFDBK_LIST_SHOW_ONLY_ACT_DEF_ID(String FDBK_LIST_SHOW_ONLY_ACT_DEF_ID) {
        Constants.FDBK_LIST_SHOW_ONLY_ACT_DEF_ID = FDBK_LIST_SHOW_ONLY_ACT_DEF_ID;
        if (FDBK_LIST_SHOW_ONLY_ACT_DEF_ID != null) {
            String[] actDefIds = FDBK_LIST_SHOW_ONLY_ACT_DEF_ID.split(",");
            for (int i = 0; i < actDefIds.length; i++) {
                fdbkListShowOnlyActDefID.put(actDefIds[i], "");
            }
        }
    }

    public void setFDBK_SHOW_COMPONENT(String FDBK_SHOW_COMPONENT) {
        Constants.FDBK_SHOW_COMPONENT = FDBK_SHOW_COMPONENT;
    }

    public void setFDBK_ID(String FDBK_ID) {
        if (FDBK_ID != null && !"".equals(FDBK_ID)) {
            Constants.FDBK_ID = FDBK_ID;
        }

    }

    public void setFDBK_PROCESS_MODEL(String FDBK_PROCESS_MODEL) {
        Constants.FDBK_PROCESS_MODEL = FDBK_PROCESS_MODEL;
    }

    public void setCopySendActDefID(String copySendActDefID) {
        if (copySendActDefID != null && !"".equals(copySendActDefID)) {
            COPY_SEND_ACT_DEF_ID = copySendActDefID;
        }
    }

    public void setCopySendProcModelDefID(String copySendProcModelDefID) {
        if (copySendProcModelDefID != null && !"".equals(copySendProcModelDefID)) {
            COPY_SEND_PROC_MODEL_DEF_ID = copySendProcModelDefID;
        }
    }

    public void setMqConnUrl(String mqConnUrl) {
        if (mqConnUrl != null && !"".equals(mqConnUrl)) {
            MQ_CONN_URL = mqConnUrl;
        }
    }

    public void setQueueName(String queueName) {
        if(queueName != null && !"".equals(queueName)){
            QUEUE_NAME = queueName;
        }

    }

    public String getProcessModelName() {
        return processModelName;
    }

    public void setProcessModelName(String processModelName) {
        PROCESS_MODEL_NAME = processModelName;
        this.processModelName = processModelName;
    }

    public String getDispatchLinkName() {
        return dispatchLinkName;
    }

    public void setDispatchLinkName(String dispatchLinkName) {
        DISPATCH_LINK_NAME = dispatchLinkName;
        this.dispatchLinkName = dispatchLinkName;
    }

    public String getAppMethod() {
        return appMethod;
    }

    public void setAppMethod(String appMethod) {
        this.appMethod = appMethod;
        APP__METHOD = appMethod;
    }

    public String getDisMethod() {
        return disMethod;
    }

    public void setDisMethod(String disMethod) {
        this.disMethod = disMethod;
        DIS__METHOD = disMethod;
    }

    public String getAppModel() {
        return appModel;
    }

    public void setAppModel(String appModel) {
        this.appModel = appModel;
        APP_MODEL = appModel;
    }

    public String getDisModel() {
        return disModel;
    }

    public void setDisModel(String disModel) {
        this.disModel = disModel;
        DIS_MODEL = disModel;
    }

    public String getFdbkModel() {
        return fdbkModel;
    }

    public void setFdbkModel(String fdbkModel) {
        this.fdbkModel = fdbkModel;
        FDBK_MODEL = fdbkModel;
    }

    public String getHanging() {
        return hanging;
    }

    public void setHanging(String hanging) {
        this.hanging = hanging;
        HANGING = hanging;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
        BUSINESS_CODE = businessCode;
    }

    public String getAppTable() {
        return appTable;
    }

    public void setAppTable(String appTable) {
        this.appTable = appTable;
        APP_TABLE = appTable;
    }

    public String getDisTable() {
        return disTable;
    }

    public void setDisTable(String disTable) {
        this.disTable = disTable;
        DIS_TABLE = disTable;
    }

    public String getFdbkTable() {
        return fdbkTable;
    }

    public void setFdbkTable(String fdbkTable) {
        this.fdbkTable = fdbkTable;
        FDBK_TABLE = fdbkTable;
    }

    public String getDisSql() {
        return disSql;
    }

    public void setDisSql(String disSql) {
        this.disSql = disSql;
        DIS_SQL = disSql;
    }

    public String getAppSql() {
        return appSql;
    }

    public void setAppSql(String appSql) {
        this.appSql = appSql;
        APP_SQL = appSql;
    }
}
