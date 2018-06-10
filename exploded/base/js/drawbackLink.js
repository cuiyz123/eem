function drawbackLink(__link_dialog_body){
    var data = {};
    for(var pro in _winParams){
        data[pro] = _winParams[pro];
    }
    $.ajax({
        url : "workBaseController.do?method=drawbackWorkItem" ,
        type : 'POST',
        async : true,
        dataType : "json",
        data : data,
        success:function(response){
            if(response.success){
                alert('撤回成功');
                __exitFromFrame();
            } else {
                alert('撤回失败');
            }

        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('撤回失败:' + errorThrown);
        }
    });
    return false;
}
