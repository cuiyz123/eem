function workFlowMonitor(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 1000 , 500 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/workFlowMonitor.jsp?__link_dialog_body=' + __link_dialog_body + '&rootProcessInstId=' + _winParams.rootProcessInstId + '&jobID=' + _winParams.jobID);

}