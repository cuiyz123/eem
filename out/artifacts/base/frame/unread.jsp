<%@ page import="com.metarnet.core.common.utils.Constants" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<head>
    <title>待阅列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <style>

    </style>
</head>
<body>

<div class="big-title-metar">待阅查询</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
        {id: 'jobCode', title: '工单编号', type: 'string', columnClass: 'text-center',fastQuery:true,fastQueryType:'lk', resolution:function(value, record, column, grid, dataNo, columnNo){
            return '<a href="javascript:;" onclick="bindActionSetNotificationToRead('+record.notificationInstID+' , ' + record.taskinstid+ ')">' + value + '</a>';
        }},
        {id: 'jobTitle', title: '工单主题', type: 'string', columnClass: 'text-center',fastQuery:true, fastQueryType:'lk'},
        {id: 'strColumn1', title: '专业', type: 'string', columnClass: 'text-center' , hide:<%=!Constants.IS_SHOW_MAJOR%>},
        {id: 'senderID', title: '发送人', type: 'string', columnClass: 'text-center'},
        {id: 'deliveryDate', title: '收到时间', type: 'date', format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center',fastQuery:true, fastQueryType:'range'},
        {id: 'activityInstName', title: '工单处理环节', type: 'string', columnClass: 'text-center'}

    ];
    var dtGridOption_2_1_2 = {
        lang : 'zh-cn',
        ajaxLoad : true,
        loadURL: '<%=path%>/workBaseController.do?method=getUnread',
        exportFileName : '待阅列表',
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

    function bindActionSetNotificationToRead(notificationInstID , taskInstID){
            $.ajax({
                url: _PATH + '/workBaseController.do?method=setNotificationToRead',
                method: 'POST',
                async: true,
                data : {notificationInstId : notificationInstID , taskInstID : taskInstID} ,
                dataType: 'json',
                success: function (response) {
                    if(response.success){
                        var record = response.taskInstance;
                        var href = _PATH + '/pageBuild.do?method=build&fromPage=draft&type=unread&buildMethod=build&'+
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
                        '&taskWarning='+record.taskWarning+
                        '&__returnUrl=/base/frame/unread.jsp';
                        location.href = href;
                    } else {
                        alert('阅读待阅失败，请重试');
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {

                }
            })
    }

</script>
</body>
</html>
