<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>
<div class="__component_title">审核记录</div>
<table id="__signFormListTable" class="table table-fixed">
    <%--<tbody>--%>
    <%--<tr>--%>
        <%--<th style="width:2%"></th>--%>
        <%--<th style="width:10%">审核人</th>--%>
        <%--<th style="width:10%">审核结果</th>--%>
        <%--<th style="width:20%">审核部门</th>--%>
        <%--<th>审核意见</th>--%>
    <%--</tr>--%>
    <%--<c:forEach items="${jsonArray}" var="approval_record" varStatus="approval">--%>
        <%--<tr>--%>
            <%--<td>${approval.index + 1}</td>--%>
            <%--<td>${approval_record.approverName}</td>--%>
            <%--<td>--%>
                <%--<c:choose>--%>
                    <%--<c:when test="${approval_record.approvalStatus == 'Y'}">通过</c:when>--%>
                    <%--<c:otherwise>驳回</c:otherwise>--%>
                <%--</c:choose>--%>
            <%--</td>--%>
            <%--<td>${approval_record.approvalOrgName}</td>--%>
            <%--<td>${approval_record.approvalOpinion}</td>--%>
        <%--</tr>--%>
    <%--</c:forEach>--%>

    <%--</tbody>--%>
</table>
<script>
var __$__request_sign_form_list_counter = 0;
__approvalInfoRecordList();
function __approvalInfoRecordList(){
    if(__$__processingObjectId == 0 && __$__request_sign_form_list_counter++ < 20){
        setTimeout(function(){
            __approvalInfoRecordList();
        } , 500);
    } else {
        $.ajax({
            url : _PATH + '/workBaseController.do?method=approvalInfoRecordList',
            method : 'POST',
            async : true,
            dataType : 'json',
            data : {approvalObjectTable : __$__processingObjectTable , approvalObjectId : __$__processingObjectId , shardingId : 1},
            success:function(response){
                var __tbody = $('<tbody></tbody>');
                __tbody.append('<tr><th style="width:50px;"></th><th>审核人</th><th style="width:70px;">审核结果</th><th>审核部门</th><th>审核意见</th><th>审核时间</th></tr>');
                for(var i = 0 ; i < response.length ; i++){
                    var __record = response[i];
                    __tbody.append('<tr><td><span>'+(i+1)+'</span></td><td><span>'+__record.approverName+'</span></td><td><span>'+(__record.approvalStatus=='Y'?'通过':'驳回')+'</span></td><td><span>'+__record.approvalOrgName+'</span></td><td><span>'+__record.approvalOpinion+'</span></td><td><span>'+ __record.approvedTime + '</span></td></tr>');
                }
                $('#__signFormListTable').empty().append(__tbody);
            },
            error:function(XMLHttpRequest, textStatus, errorThrown){
                alert('获取审核列表失败，请刷新页面重试');
            }
        })
    }
}
</script>
</body>
</html>
