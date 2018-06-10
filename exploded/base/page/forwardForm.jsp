<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>

<body>
<form id="__forward_form_form">
    <table class="__dialog_panel_table">
        <tbody>
        <tr>
            <th>转办说明</th>
            <td><textarea id="processingResultOpinion" name="processingResultOpinion" class="form-control"></textarea></td>
        </tr>
        <tr>
            <th><span class="__require">*</span>转办对象</th>
            <td>
                <div class="input-group">
                    <input id="ParticipantID" name="ParticipantID" type="hidden">
                    <input id="ParticipantLabel" name="ParticipantLabel" readonly="readonly" type="text" class="form-control">
                    <span id="__person_tree_to_forward" class="input-group-addon glyphicon glyphicon-user"></span>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</form>
<div class="__dialog_panel_btns">
    <span class="btn btn-danger" onclick="__submit_forward_form_form()">提交</span>
    <span class="btn btn-default" onclick="__reset_forward_form_form()">重置</span>
</div>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
<%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
$('#__person_tree_to_forward').click(function(){
    __open_tree(this.id , 2 , '人员选择' ,function(selectedNodes){
        if(selectedNodes == ''){
            $('#ParticipantID').val('');
            $('#ParticipantLabel').val('');
        } else {
            var userName = '';
            var userTrueName = '';
            for(var i = 0 ; i < selectedNodes.length ; i ++){
                userName += selectedNodes[i].userName;
                userTrueName += selectedNodes[i].label;
                if(i < selectedNodes.length - 1){
                    userName += ',';
                    userTrueName += ',';
                }
            }
            $('#ParticipantID').val(userName);
            $('#ParticipantLabel').val(userTrueName);
            $('#ParticipantLabel').removeClass('__notnull');
        }
//        console.info(selectedNodes);
    },'','','radio');
});
function __submit_forward_form_form(){
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前转办对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前转办对象Table：__$__processingObjectTable');
        return;
    }
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    data.operDesc = $('#processingResultOpinion').val();

    var ParticipantID = $('#ParticipantID').val();
    if(ParticipantID != ''){
        var participant=[];
        var pid= ParticipantID.split(',');
        for(var i=0;i<pid.length;i++){
            participant.push({
                'ParticipantID' : pid[i],
                'ParticipantType' : '1'
            });
        }
        data.participant='{participantList:'+JsonObjectToString(participant)+'}' ;
    } else {
        $('#ParticipantLabel').addClass('__notnull');
        $('#ParticipantLabel').focus();
        return;
    }
    data.operTypeEnumId = '40050464';
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
                alert('转办失败，请重试。');
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('转办失败，请重试。');
        }
    })

}
function __reset_forward_form_form(){
    document.getElementById('__forward_form_form').reset();
}
</script>
</body>
</html>
