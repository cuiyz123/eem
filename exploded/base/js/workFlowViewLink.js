function workFlowViewLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 1000 , 500 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
//    $('#' + __link_dialog_body).load(_PATH + '/base/frame/workFlowView.jsp?processInstID=' + _winParams.processInstID);
    var iframe = $('<iframe frameborder="0" style="width:1200px;height:500px;" src="'+_PATH + '/base/frame/workFlowView.jsp?mode=INFINITE&processInstID=' + _winParams.processInstID+' &jobID='+_winParams.jobID+'"></iframe>');
    $('#' + __link_dialog_body).append(iframe);
}