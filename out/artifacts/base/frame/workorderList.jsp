<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<head>
    <title>工单查询列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <style>

    </style>
</head>
<body style="height:100%">

<div class="big-title-metar">工单查询</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
        {id: 'workordernumber', title: '工单编号', type: 'string', columnClass: 'text-center' , fastQuery:true, fastQueryType:'lk',resolution:function(value, record, column, grid, dataNo, columnNo){
           /* if(record.issuetime) {
                return '<a href="<%=path%>/workBaseController.do?method=LookFormDetail&show=show&type=already&isRoot=root'+
                        '&processInstID='+record.rootproinstid+
                        '"target="__menu_body">' + value+'</a>';
            }else{
                return '<a href="javascript:;" onclick="turnToPage(' + record.objectid + ' ,\'apply\')">' + value + '</a>';
            }*/
            return '<a href="<%=path%>/workBaseController.do?method=LookFormDetail&show=show&type=already&isRoot=root'+
                    '&processInstID='+record.rootproinstid+
                    '"target="__menu_body">' + value+'</a>';
            }, headerStyle: 'width:160px'
        },
        {id: 'theme', title: '工单主题', type: 'string', columnClass: 'text-center',fastQuery:true, fastQueryType:'lk', headerStyle: 'width:160px'},
        {id: 'operorgname', title: '操作部门', type: 'string', columnClass: 'text-center',fastQuery:false,fastQueryType:'lk',click:'getOrg'},
        {id: 'flowing_object_id', title: '部门id', type: 'string', columnClass: 'text-center',fastQuery:true,hide:true,fqhide:true,fastQueryType:'lk'},
        {id: 'tabletype', title: '工单类型', type: 'string', columnClass: 'text-center',headerStyle: 'width:70px'},
        {id: 'reqfdbktime', title: '要求反馈时间', type: 'date',format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center', headerStyle: 'width:150px'},
        {id: 'issuetime', title: '下发时间', type: 'date', format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center', headerStyle: 'width:150px',resolution: function (value, record, column, grid, dataNo, columnNo) {
            if(value==""){
                return "未下发";
            }else{
                return value;
            }
        }},
        {id: 'issueusertruename', title: '下发人', type: 'date', columnClass: 'text-center', headerStyle: 'width:60px'},
        {id: 'creationtime', title: '创建时间', type: 'date', format:'yyyy-MM-dd hh:mm:ss',columnClass: 'text-center', headerStyle: 'width:150px' }
    ];
    var dtGridOption_2_1_2 = {
        lang : 'zh-cn',
        ajaxLoad : true,
        loadURL: '<%=path%>/workBaseController.do?method=queryWorkOrder',
        exportFileName : '工单查询列表',
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
    function turnToPage(objectID , type){
        $.ajax({
            url: _PATH + '/process.do?method=toPageDetail&showType=showType',
            method: 'POST',
            async: true,
            data : {objectID : objectID , type : type} ,
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
                            '&__returnUrl=/base/frame/workorderList.jsp';
                    location.href = href;
                } else {
                    alert('阅读待阅失败，请重试');
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {

            }
        })
    }

      function getOrg(){
          __open_tree(this.id , 1, '部门树' ,function(selectedNodes){
              for(var x=0;x<selectedNodes.length ;x++){
                  debugger;
                  $('#lk_flowing_object_id').val(selectedNodes[x].id);
                  $('#lk_operorgname').val(selectedNodes[x].fullName);
//                alert(JSON.stringify(selectedNodes[x]))
              }
//            var deptInf = getDeptInfo(selectedNodes)
          } ,'' , '' , 'radio');

      }
</script>
</body>
</html>
