function signFormSuccessLink(__link_dialog_body , __batchFlag){
    __resizeLinkDialog(__link_dialog_body , 500 , 200 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/eemSignForm.jsp?__signFormAction=Y&up=N&__link_dialog_body=' + __link_dialog_body + '&__batchFlag=' + __batchFlag);

}