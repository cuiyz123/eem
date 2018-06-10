<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>

<body>
<form id="__recall_form_form">
<table class="__dialog_panel_table">
    <tbody>
        <tr>
            <th><span class="__require">*</span>撤单原因</th>
            <td><textarea id="processingReason" name="processingReason" class="form-control"></textarea></td>
        </tr>
    </tbody>
</table>
</form>
<div class="__dialog_panel_btns">
    <span class="btn btn-danger" onclick="__submit_recall_form_form()">提交</span>
    <span class="btn btn-default" onclick="__reset_recall_form_form()">重置</span>
</div>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
<%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
function __submit_recall_form_form(){
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前撤单对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前撤单对象Table：__$__processingObjectTable');
        return;
    }
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    data.operDesc = $('#processingReason').val();
    if (data.operDesc == '') {
        $('#processingReason').addClass('__notnull');
        $('#processingReason').focus();
        return;
    }
//    data.processingType = "CANCEL";
    data.operTypeEnumId = '40050465';
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
                alert('撤单失败，请重试。');
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('撤单失败，请重试。');
        }
    })

}
function __reset_recall_form_form(){
    document.getElementById('__recall_form_form').reset();
}
</script>
</body>
</html>
