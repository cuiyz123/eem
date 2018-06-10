function recallFormLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 500 , 200 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/recallForm.jsp?__link_dialog_body=' + __link_dialog_body);

}