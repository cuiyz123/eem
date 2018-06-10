function signFormSuccessBatchLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 500 , 200 , 'none');
    $('#' + __link_dialog_body).load(_PATH + '/base/page/signForm.jsp?__signFormAction=Y&up=N&__link_dialog_body=' + __link_dialog_body + '&__batchFlag=Y');

}