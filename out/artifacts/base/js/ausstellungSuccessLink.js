function ausstellungSuccessLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 500 , 200 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/base/page/ausstellung.jsp?__ausstellungAction=Y&__link_dialog_body=' + __link_dialog_body);

}