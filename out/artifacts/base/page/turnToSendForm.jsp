<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>

<body>
<form id="__turn2send_form_form">
<table class="__dialog_panel_table">
    <tbody>
        <tr>
            <th>转派说明</th>
            <td><textarea id="processingResultOpinion" name="processingResultOpinion" class="form-control"></textarea></td>
        </tr>
        <tr>
            <th><span class="__require">*</span>转派对象</th>
            <td>
                <div class="input-group">
                    <input id="ParticipantID" name="ParticipantID" type="hidden">
                    <input id="ParticipantLabel" name="ParticipantLabel" readonly="readonly" type="text" class="form-control">
                    <span id="__person_tree_to_turn" class="input-group-addon glyphicon glyphicon-user"></span>
                </div>
            </td>
        </tr>
    </tbody>
</table>
</form>
<div class="__dialog_panel_btns">
    <span class="btn btn-danger" onclick="__submit_turn2send_form_form()">提交</span>
    <span class="btn btn-default" onclick="__reset_turn2send_form_form()">重置</span>
</div>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
<%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
$('#__person_tree_to_turn').click(function(){
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
    });
});

function __submit_turn2send_form_form(){
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前转派对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前转派对象Table：__$__processingObjectTable');
        return;
    }
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    data.processingResultOpinion = $('#processingResultOpinion').val();

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
    data.processingType = "TURN_DIS";
    for(var pro in _winParams){
        data[pro] = _winParams[pro];
    }
    $.ajax({
        url : "workBaseController.do?method=generalProcessing",
        type : 'POST',
        async : true,
        dataType : "json",
        data : data,
        success:function(response){
            if(response.success){
                __exitFromFrame();
            } else {
                alert('转派失败，请重试。');
            }
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('转派失败，请重试。');
        }
    })

}
function __reset_turn2send_form_form(){
    document.getElementById('__turn2send_form_form').reset();
}
</script>
</body>
</html>
