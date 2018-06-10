<%@ page import="com.metarnet.core.common.utils.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>

<body>
<form id="__turn2${dispatchType}_form_form">
<table class="__dialog_panel_table">
    <tbody>
        <tr>
            <th><span class="__require">*</span>转派对象</th>
            <td>
                <div class="input-group">
                    <input id="mainTransfer${dispatchType}" name="mainTransfer" type="hidden">
                    <input id="mainTransferLabel${dispatchType}" name="mainTransferLabel" readonly="readonly" type="text" class="form-control">
                    <span id="__person_tree_to_send${dispatchType}" class="input-group-addon glyphicon glyphicon-th"></span>
                </div>
            </td>
        </tr>
        <tr>
            <th><span class="__require">*</span>要求完成时间</th>
            <td>
                <input class="form-control __metar_check_form _task_start" id="reqFdbkTime${dispatchType}" type="text" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss', minDate: '%y-%M-%d 18:00:00'})">
            </td>
        </tr>
        <%--<tr>
            <th>抄送</th>
            <td>
                <div class="input-group">
                    <input id="copyID" name="copyID" type="hidden">
                    <input id="copyLabel" name="copyLabel" readonly="readonly" type="text" class="form-control">
                    <span id="__person_tree_to_copy" class="input-group-addon glyphicon glyphicon-th"></span>
                </div>
            </td>
        </tr>--%>
        <tr>
            <th>转派说明<span style="font-size: 10px;">(字数限制200个)</span></th>
            <td><textarea id="processingResultOpinion${dispatchType}" name="processingResultOpinion" class="form-control">无</textarea></td>
        </tr>
        <tr>
            <th>附件</th>
            <td><input id="__file_upload_turn_dispatch${dispatchType}" type="file" multiple="true"/></td>
        </tr>
    </tbody>
</table>
</form>
<div class="__dialog_panel_btns">
    <span class="btn btn-danger" onclick="__submit_turn2send_form_form()">提交</span>
    <span class="btn btn-default" onclick="__reset_turn2send_form_form()">重置</span>
</div>
<script>
__init_attachment_function('__file_upload_turn_dispatch${dispatchType}' , '${disCommonModel.objectId}' , '<%=Constants.DIS_TABLE%>' , 'edit');
$('#__person_tree_to_send'+'${dispatchType}').click(function(){
    __open_tree(this.id , 3 , '转派对象选择' ,function(selectedNodes){
        debugger;
        if(selectedNodes == ''){
            $('#mainTransfer'+'${dispatchType}').val('');
            $('#mainTransferLabel'+'${dispatchType}').val('');
        } else {
            var userName = '';
            var userTrueName = '';
            for(var i = 0 ; i < selectedNodes.length ; i ++){
                debugger;
                if(selectedNodes[i].type == 1){
                    userName += (selectedNodes[i].code + ':ORG');
                } else {
                    userName += (selectedNodes[i].id + ':MEMBER');
                }
                userTrueName += selectedNodes[i].label;
                if(i < selectedNodes.length - 1){
                    userName += ',';
                    userTrueName += ',';
                }
            }
            $('#mainTransfer'+'${dispatchType}').val(userName);
            $('#mainTransferLabel'+'${dispatchType}').val(userTrueName);
            $('#mainTransferLabel'+'${dispatchType}').removeClass('__notnull');
        }
    });
});
$('#__person_tree_to_copy').click(function(){
    __open_tree(this.id , 3 , '抄送对象选择' ,function(selectedNodes){
        if(selectedNodes == ''){
            $('#copyID').val('');
            $('#copyLabel').val('');
        } else {
            var userName = '';
            var userTrueName = '';
            for(var i = 0 ; i < selectedNodes.length ; i ++){
                userName += (selectedNodes[i].id + ':' + selectedNodes[i].userName + ':' + selectedNodes[i].label);
                if(selectedNodes[i].type == 1){
                    userName += ':ORG';
                } else {
                    userName += ':MEMBER';
                }
                userTrueName += selectedNodes[i].label;
                if(i < selectedNodes.length - 1){
                    userName += ',';
                    userTrueName += ',';
                }
            }
            $('#copyID').val(userName);
            $('#copyLabel').val(userTrueName);
        }
    });
});

function __submit_turn2send_form_form(){
    debugger;
    var data = {};
    if(__$__processingObjectId == 0){
        alert('未设置当前转派对象ID：__$__processingObjectId');
        return;
    } else if(__$__processingObjectTable == 0){
        alert('未设置当前转派对象Table：__$__processingObjectTable');
        return;
    }
    data.objectId = '${disCommonModel.objectId}';
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
    data.dispatchType = '${dispatchType}';
    data.operDesc = $('#processingResultOpinion'+'${dispatchType}').val();
    if(data.operDesc.length>200){
        alert("转派说明字数超过200");
        return;
    }
    var mainTransfer = $('#mainTransfer'+'${dispatchType}').val();
    var mainTransferLabel = $('#mainTransferLabel'+'${dispatchType}').val();
    if(mainTransfer != ''){
        data.mainTransfer = mainTransfer;
        data.mainTransferLabel = mainTransferLabel;
    } else {
        $('#mainTransferLabel'+'${dispatchType}').addClass('__notnull');
        $('#mainTransferLabel'+'${dispatchType}').focus();
        return;
    }
    var reqFdbkTime = $('#reqFdbkTime'+'${dispatchType}').val();
    if(reqFdbkTime!=''){
        data.reqFdbkTime = reqFdbkTime;
    }else{
        $('#reqFdbkTime'+'${dispatchType}').addClass('__notnull');
        return;
    }
    /*var copyID = $('#copyID').val();
    if(copyID != ''){
        data.copy = copyID;
    }*/
    for(var pro in _winParams){
        data[pro] = _winParams[pro];
    }
    __show_metar_loading();
    $.ajax({
        url : "commDispatchController.do?method=turnToDispatch",
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
            __hide_metar_loading();
            alert('转派失败，请重试。');
        }
    })

}
function __reset_turn2send_form_form(){
    document.getElementById('__turn2${dispatchType}_form_form').reset();
}
</script>
</body>
</html>
