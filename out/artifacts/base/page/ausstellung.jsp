<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String __ausstellungAction = request.getParameter("__ausstellungAction");
    __ausstellungAction = (__ausstellungAction == null || "".equals(__ausstellungAction)) ? "Y" : __ausstellungAction;
%>
<html>
<body>
<form id="__ausstellung_form_<%=__ausstellungAction%>">
<table class="__dialog_panel_table">
    <tbody>
        <tr>
            <th><span class="__require">*</span>签发意见</th>
            <td><textarea id="processingReason_<%=__ausstellungAction%>" class="form-control"></textarea></td>
        </tr>
    </tbody>
</table>
<div class="__dialog_panel_btns">
    <span id="__submit_ausstellung_<%=__ausstellungAction%>" class="btn btn-danger">提交</span>
    <span id="__reset_ausstellung_<%=__ausstellungAction%>" class="btn btn-default">重置</span>
</div>
</form>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
<%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
$('#__submit_ausstellung_<%=__ausstellungAction%>').click(function(){
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前签发对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前签发对象Table：__$__processingObjectTable');
        return;
    }
    data.operTypeEnumId = '40050228';
    data.operDesc = $('#processingReason_<%=__ausstellungAction%>').val();
    if (data.operDesc == '') {
        $('#processingReason_<%=__ausstellungAction%>').addClass('__notnull');
        $('#processingReason_<%=__ausstellungAction%>').focus();
        return;
    }
    data.processingStatus = '<%=__ausstellungAction%>';
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    for(var pro in _winParams){
        data[pro] = _winParams[pro];
    }
    data.TASKLIST = TASKLIST;
    __show_metar_loading();
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
                __hide_metar_loading();
                alert(response.msg);
            }

        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            __hide_metar_loading
            alert('error' + errorThrown);
        }
    })
});

$('#__reset_ausstellung_<%=__ausstellungAction%>').click(function(){
    document.getElementById('__ausstellung_form_<%=__ausstellungAction%>').reset();
    $('#processingReason_<%=__ausstellungAction%>').focus();
});
if('<%=__ausstellungAction%>' == 'Y') {
    $('#processingReason_<%=__ausstellungAction%>').val('通过');
}
</script>
</body>
</html>
