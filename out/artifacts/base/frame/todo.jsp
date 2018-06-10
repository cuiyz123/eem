<%@ page import="com.metarnet.core.common.utils.Constants" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<head>
    <title>待办列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <style>

    </style>
</head>
<body>

<div class="big-title-metar">待办查询</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
        {id: 'jobCode', title: '工单编号', type: 'string', columnClass: 'text-center' , fastQuery:true, fastQueryType:'lk',resolution:function(value, record, column, grid, dataNo, columnNo){
            return '<a href="<%=path%>/pageBuild.do?method=build&fromPage=todo&type=waiting&buildMethod=build&'+
                    <%--return '<a href="<%=path%>/workBaseController.do?method=pageForwardController&fromPage=draft&type=waiting&buildMethod=build&'+--%>
                    '&processInstID='+record.processInstID+
                    '&processModelId='+record.processModelId+
                    '&processModelName='+record.processModelName+
                    '&activityInstID='+record.activityInstID+
                    '&activityDefID='+record.activityDefID+
                    '&taskInstID='+record.taskInstID+
                    '&activityInstName='+encodeURIComponent(encodeURIComponent(record.activityInstName))+
                    '&jobTitle='+encodeURIComponent(encodeURIComponent(record.jobTitle))+
                    '&jobCode='+encodeURIComponent(encodeURIComponent(record.jobCode))+
                    '&jobID='+record.jobID+
                    '&appID='+record.appID+
                    '&shard='+record.shard+
                    '&businessId='+record.businessId+
                    '&rootProcessInstId='+record.rootProcessInstId+
                    '&createDate='+record.createDate+
//                    '&datColumn1='+record.datColumn1+
                    '&taskWarning='+record.taskWarning+
                    '&strColumn4='+record.strColumn4+
                    '&__returnUrl=/base/frame/todo.jsp'+
                    '">' + value + '</a>';
        }},
        {id: 'jobTitle', title: '工单主题', type: 'string', columnClass: 'text-center',fastQuery:true, fastQueryType:'lk'},
        {id: 'activityInstName', title: '工单处理环节', type: 'string', columnClass: 'text-center'},
        {id: 'strColumn1', title: '专业', type: 'string', columnClass: 'text-center' , hide:<%=!Constants.IS_SHOW_MAJOR%>},
        {id: 'createDate', title: '到单时间', type: 'date', format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center', headerStyle: 'width:160px',fastQuery:true, fastQueryType:'range'},
        {id: 'datColumn1', title: '要求完成时间', type: 'date', format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center' , headerStyle: 'width:160px',fastQuery:true, fastQueryType:'range',resolution:function(value, record, column, grid, dataNo, columnNo){
            if(value && value.indexOf("1970")>=0){
                return "";
            }else{
                return value;
            }
        }}
    ];
    var dtGridOption_2_1_2 = {
        lang : 'zh-cn',
        ajaxLoad : true,
        loadURL: '<%=path%>/workBaseController.do?method=queryTodo',
        exportFileName : '待办列表',
        columns : dtGridColumns_2_1_2,
        gridContainer : 'dtGridContainer_2_1_2',
        toolbarContainer : 'dtGridToolBarContainer_2_1_2',
        tools:'refresh|faseQuery',
        pageSize : 10,
        pageSizeLimit : [10, 20, 50]
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
        grid_2_1_2.load(function(){
            __hide_metar_loading();
        });
    });
</script>
</body>
</html>
