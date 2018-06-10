//@ sourceURL=ArchiveLink.js
function ArchiveLink(__link_dialog_body){
    var data = {};
//    if(__$__processingObjectId == 0){
//        alert('未设置当前审核对象ID：__$__processingObjectId');
//        return false;
//    } else if(__$__processingObjectTable == 0){
//        alert('未设置当前审核对象Table：__$__processingObjectTable');
//        return false;
//    }
    data.processingType = "ARCHIVE";
    data.operTypeEnumId = '40050439';
    data.processingReason ="";
    data.processingResultOpinion ="";
    data.processingObjectId = __$__processingObjectId;
    data.processingObjectTable = __$__processingObjectTable;
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
                alert('归档成功');
                __exitFromFrame();
            } else {
                alert(response.success);
            }

        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert('error' + errorThrown);
        }
    });
    return false;
}
