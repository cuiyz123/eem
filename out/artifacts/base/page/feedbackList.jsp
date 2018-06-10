<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--<jsp:include page="../../base/basePage.jsp"/>--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    String processInstID = request.getParameter("processInstID");
    String activityDefID = request.getParameter("activityDefID");
    String fromPage = request.getParameter("fromPage");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<div id="__feedback_list_view_table" class="modal-body __feedback_list_view_table">
    <table class="table">
        <tbody>
        <tr>
            <th><input onclick="__checkAllFdbks(this)" id="__checkAll" type="checkbox"/></th>
            <th>反馈单位</th>
            <th>反馈人</th>
            <th>反馈时间</th>
            <th>要求反馈时间</th>
            <th>工单状态</th>
            <th>审核状态</th>
            <th>审核说明</th>
        </tr>
        <%--<c:forEach items="${fdbkList}" var="feedback" >
            <tr>
                <td>
                    <c:if test="${feedback.workOrderStatus == '已反馈'}">
                    <span><input name="__feedback_processsInstID_checkbox" type="checkbox" value="${feedback.processInstId}"/></span>
                    </c:if>
                </td>
                <td><span>${feedback.disAssignObjectName}</span></td>
                <td><span>${feedback.operUserTrueName}</span></td>
                <td><span>${feedback.operTime}</span></td>
                <td><span>${feedback.reqFdbkTime}</span></td>
                <td><span>${feedback.workOrderStatus}</span></td>
            </tr>
        </c:forEach>--%>
        </tbody>
    </table>
</div>

<script type="text/javascript" >
$(document).ready(function(){
    __show_metar_loading();
    $.ajax({
        url : _PATH + '/commFdbkListController.do?method=list',
        data:{processInstID : '<%=processInstID%>' , activityDefID : '<%=activityDefID%>'},
        method : 'POST',
        async : true,
        dataType : 'json',
        success:function(response){
            if(response){
                debugger;
                var __table = $('<table class="table"></table>');
                var __tbody = $('<tbody></tbody>');
                var __tr_title = $('<tr><th><input onclick="__checkAllFdbks(this)" id="__checkAll" type="checkbox"/></th><th>反馈单位</th><th>反馈人</th><th>反馈时间</th><th>要求反馈时间</th><th>工单状态</th><th>审核状态</th><th>审核说明</th></tr>');
                __tbody.append(__tr_title);
                for(var i = 0 ; i < response.length ; i++){
                    var fdbk = response[i];
                    var __tr = $('<tr></tr>');
                    var __check_td = (fdbk.workOrderStatus == '已反馈')? $('<td><span><input name="__feedback_processsInstID_checkbox" type="checkbox" value="'+fdbk.processInstId+'"/></span></td>') : $('<td></td>');
                    var __disAssignObjectName_td = fdbk.operUserTrueName ? $('<td><span><a href="'+_PATH+'/showFeedBack.do?processInstID='+fdbk.processInstId+'" target="_blank">'+fdbk.disAssignObjectName+'</a></span></td>') :$('<td><span>'+fdbk.disAssignObjectName+'</span></td>');
                    var __operUserTrueName_td = fdbk.operUserTrueName ? $('<td><span>'+fdbk.operUserTrueName+'</span></td>') : $('<td></td>');
                    var __operTime_td = fdbk.operTime ? $('<td><span>'+fdbk.operTime+'</span></td>') : $('<td></td>');
                    var __reqFdbkTime_td = $('<td><span>'+fdbk.reqFdbkTime+'</span></td>');
                    var __workOrderStatus_td = $('<td><span>'+fdbk.workOrderStatus+'</span></td>');
                    var __signStatus_td = "";
                    if(fdbk.attribute1==undefined){
                        __signStatus_td=$('<td><span>未审核</span></td>')
                    }else {
                        if(fdbk.attribute1=='N'){
                            __signStatus_td=$('<td><span>驳回</span></td>')
                        }else{
                            __signStatus_td= $('<td><span>通过</span></td>')
                        }
                    }
                    var __signDesc_td = "";
                    if(fdbk.attribute2!=undefined){
                        __signDesc_td=$('<td><span>'+fdbk.attribute2+'</span></td>');
                    }else{
                        __signDesc_td=$('<td><span></span></td>');
                    }
                    __tr.append(__check_td).append(__disAssignObjectName_td).append(__operUserTrueName_td).append(__operTime_td).append(__reqFdbkTime_td).append(__workOrderStatus_td).append(__signStatus_td).append(__signDesc_td);
                    __tbody.append(__tr);
                }
                __table.append(__tbody);
                $('#__feedback_list_view_table').empty().append(__table);
                __hide_metar_loading();
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            __hide_metar_loading();
        }
    })
});

function __checkAllFdbks(_this){
    $('input[name="__feedback_processsInstID_checkbox"]').attr('checked' , _this.checked);

}

function __getCheckedFeedbacks(){
var     __selectedFeedbacks = new Array();
    var feedbacks = $('input[name="__feedback_processsInstID_checkbox"]');
    for(var i = 0 ; i < feedbacks.length ; i++){
        if(feedbacks[i].checked){
            __selectedFeedbacks.push({processInstID : feedbacks[i].value});
        }
    }

    return __selectedFeedbacks;
}

function closeModalWindow(modalWindow){
    $('#' + modalWindow).modal('hide');
}
function __show_person_info(action_id , person_ids){
    var user_properties = ['trueName' , 'orgEntity' , 'mobilePhone' , 'email'];
    __open_metar_window(action_id , '用户信息' , 960 , 200 , function(window_body){
        __show_metar_loading();
        $.ajax({
            url : _PATH + '/commWorkFlowMonitorController.do?method=getUserEntityByIds&ids=' + person_ids,
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
                            if(user_properties[j] == 'orgEntity'){
                                debugger;
                                __td = $('<td><span>' + response[i][user_properties[j]]['orgName'] + '</span></td>');
                            }
                            __tr.append(__td);
                        }
                        __tbody.append(__tr);
                    }
                    __table.append(__tbody);
                    $(window_body).append(__table);
                    var __feedback_list_btns = $('<div class="__dialog_panel_btns"></div>');
                    var ____feedback_list_btn_close = $('<span class="btn btn-danger">关闭</span>');
                    ____feedback_list_btn_close.click(function(){
                        $(window_body).parent().modal('hide');
                    });
                    __feedback_list_btns.append(____feedback_list_btn_close);

                    $(window_body).parent().append(__feedback_list_btns);
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