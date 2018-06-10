<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>

<body>
<form id="__receive_form_form">
<table class="__dialog_panel_table">
    <tbody>
        <tr>
            <th>签收人：</th>
            <td class="__dialog_panel_table__td">${globalUniqueUser.trueName}</td>
        </tr>
        <tr>
            <th>签收意见：</th>
            <td><textarea id="processingResultOpinion" name="processingResultOpinion" class="form-control"></textarea></td>
        </tr>
        <%--<tr>
            <th>备注</th>
            <td><textarea id="remarks" name="remarks" class="form-control"></textarea></td>
        </tr>--%>
    </tbody>
</table>
</form>
<div class="__dialog_panel_btns">
    <span class="btn btn-danger" onclick="__submit_receive_form_form()">提交</span>
    <span class="btn btn-default" onclick="__reset_receive_form_form()">重置</span>
</div>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
<%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
function __submit_receive_form_form(){
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前签收对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前签收对象Table：__$__processingObjectTable');
        return;
    }
    data.operTypeEnumId = '40050229';
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    data.operDesc = $('#processingResultOpinion').val();
//    data.remarks = $('#remarks').val();
    for(var pro in _winParams){
        data[pro] = _winParams[pro];
    }
    $.ajax({
        url : "workBaseController.do?method=generalProcess",
        type : 'POST',
        async : true,
        dataType : "json",
        data : data,
        success:function(response){
            if(response.success){
                __exitFromFrame();
            } else {
                alert('签收失败，请重试。');
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('签收失败，请重试。');
        }
    })

}
function __reset_receive_form_form(){
    document.getElementById('__receive_form_form').reset();
}
</script>
</body>
</html>
