function signFormUpSuccessLink(__link_dialog_body , __batchFlag){
    __resizeLinkDialog(__link_dialog_body , 500 , 250 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/signForm.jsp?__signFormAction=Y&up=Y&__link_dialog_body=' + __link_dialog_body + '&__batchFlag=' + __batchFlag);

}