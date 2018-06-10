function TurnToSendFormLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 500 , 230 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/turnToSendForm.jsp?__link_dialog_body=' + __link_dialog_body);
}