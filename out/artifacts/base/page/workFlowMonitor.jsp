<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--<jsp:include page="../../base/basePage.jsp"/>--%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    String rootProcessInstId = request.getParameter("rootProcessInstId");
    String jobID = request.getParameter("jobID");
    String mode = request.getParameter("mode");
    Boolean showAttachment = "INFINITE".equals(mode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="<%=path%>/component/jquery-treetable/css/jquery.treetable.css" type="text/css">
    <link rel="stylesheet" href="<%=path%>/component/jquery-treetable/css/jquery.treetable.theme.default.css" type="text/css">
</head>
<body>
<ul class="nav nav-tabs" style="position:relative;z-index: 1">
    <li id="record" role="presentation" class="active"><a>日志列表</a></li>
    <li id="graph" role="presentation"><a>流程图</a></li>
</ul>
<div class="view_panel" id="record_view"></div>
<div class="view_panel" id="graph_view" style="display: none;position:absolute;bottom:0;left:10px;right:10px;height:100%;"><iframe frameborder="0" width="100%" height="100%" src="<%=path%>/base/frame/workFlowView.jsp?processInstID=<%=rootProcessInstId%>" /></div>

<script type="text/javascript" src="<%=path%>/component/jquery-treetable/jquery.treetable.js"></script>
<script type="text/javascript" >

$(document).ready(function(){
    __init_metar_tree_table({
        loadUrl:_PATH + '/commWorkFlowMonitorController.do?method=getOrderLog&rootProcessInstId=<%=rootProcessInstId%>&jobID=<%=jobID%>',
        container:'record_view',
        columns:[{
            title:'部门名称',
            column:'operateOrg'
        } , {
            title:'环节名称',
            column:'activityName'
        } , {
            title:'到达时间',
            column:'arriveDateTime'
        } , {
            title:'处理人',
            column:'operator',
            width:150,
            overflowHidden:true,
            wrapFunction:function(treeNode , value){
                if(treeNode.nowActivity){
                    var actionContent = '';
                    var actionParams = '';
                    var personStrArray = value.split(',');
                    for(var i = 0 ; i < personStrArray.length ; i++){
                        var personStr = personStrArray[i];
                        var personProArray = personStr.split('||');
                        if(personProArray.length == 2){
                            actionContent += personProArray[0];
                            actionParams += personProArray[1];
                        }
                        if(i != personStrArray.length - 1){
                            actionContent += ',';
                            actionParams += ',';
                        }
                    }
                    value = '<a id="' + new Date().getTime()+ '" title="' + actionContent + '" onclick="__show_person_info(this.id , \'' + actionParams + '\');">' + actionContent + '</a>';
                }

                return value;
            }
        } , {
            title:'完成时间',
            column:'completeDateTime'
        } , {
            title:'处理类型',
            column:'processType',
            width:80
        } , {
            title:'处理意见',
            column:'processOpinion',
            width:80
        }
        <%if(showAttachment){ %>
            , {
            title:'附件',
            column:'attachment',
            width:80,
            value:'attachment',
            wrapFunction:function(treeNode , value){
                setTimeout(function(){
                    __init_attachment_function('__attachment_for_workflow_' + treeNode.id , treeNode.processingObjectId , treeNode.processingObjectTable , 'show' , '' , 'N');
                } , 500);
                return '<input id="__attachment_for_workflow_' + treeNode.id + '" type="file" style="display:none"/>';
            }
        }
        <%}%>
        ]
    });

    $('.nav li').click(function(){
        var __this = $(this);
        if(!__this.hasClass('active')){
            $('.nav li').removeClass('active');
            __this.addClass('active');
            $('.view_panel').hide();
            $('#' + this.id + '_view').show();
        }
    });
})

function __show_person_info(action_id , person_usernames){
    var user_properties = ['trueName' , 'orgName' , 'mobilePhone' , 'email'];
    __open_metar_window(action_id , '当前处理人' , 960 , 460 , function(window_body){
        __show_metar_loading();
        $.ajax({
            url : _PATH + '/commWorkFlowMonitorController.do?method=getUserEntityByUserNames&userNames=' + person_usernames,
            method : 'POST',
            async : true,
            dataType : 'json',
            success:function(response){
                if(response){
                    debugger;
                    var __table = $('<table class="table"></table>');
                    var __tbody = $('<tbody></tbody>');
                    var __tr_title = $('<tr><th>姓名</th><th>单位</th><th>电话</th><th>邮箱</th></tr>');
                    __tbody.append(__tr_title);
                    for(var i = 0 ; i < response.length ; i++){
                        var __tr = $('<tr></tr>');
                        for(var j = 0 ; j < user_properties.length ; j++){
                            var __td = $('<td><span>' + response[i][user_properties[j]] + '</span></td>');
                            /*if(user_properties[j] == 'orgEntity'){
                                debugger;
                                __td = $('<td><span>' + response[i][user_properties[j]]['orgName'] + '</span></td>');
                            }*/
                            __tr.append(__td);
                        }
                        __tbody.append(__tr);
                    }
                    __table.append(__tbody);
                    $(window_body).append(__table);
                    __hide_metar_loading();
                }
            },
            error:function(XMLHttpRequest, textStatus, errorThrown){
                __hide_metar_loading();
            }
        })
    });

}



</script>
</body>
</html>