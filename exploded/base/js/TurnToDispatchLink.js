function TurnToDispatchLink(__link_dialog_body){
    __resizeLinkDialog(__link_dialog_body , 500 , 300 , 'none');
//    $('#' + __link_dialog_body).html(__link_dialog_body);
    $('#' + __link_dialog_body).load(_PATH + '/commDispatchController.do?method=initTurnToDispatch&dispatchType=s_subTurnToSend&__link_dialog_body=' + __link_dialog_body + '&rootProcessInstId=' + _winParams.rootProcessInstId + '&processInstID=' + _winParams.processInstID);
}