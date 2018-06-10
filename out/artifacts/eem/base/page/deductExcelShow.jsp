<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>上报数据展示</title>
    <%--<script type="text/javascript" src="<%=path%>/base/js/eemSignFormSuccessLink.js"></script>--%>
    <%--<script type="text/javascript" src="<%=path%>/base/js/eemSignFormFailedLink.js"></script>--%>
    <style type="text/css">
        table{
            margin-top: 50px;
        }
    </style>
</head>
<body>
<div id="__link_bar">
    <span class="btn btn-danger" onclick="javascript:window.history.go(-1)">返回</span>
</div>

${tempHTML}

<div id="__reject_modal_window" class="__link_dialog_container modal">
    <div class="modal-header">驳回
        <div class="close" onclick="closeModalWindow('__reject_modal_window')">×</div>
    </div>
    <div class="modal-body" id="__reject_modal_body"></div>
</div>
<div id="__pass_modal_window" class="__link_dialog_container modal">
    <div class="modal-header">通过
        <div class="close" onclick="closeModalWindow('__pass_modal_window')">×</div>
    </div>
    <div class="modal-body" id="__pass_modal_body"></div>
</div>
</body>
<script>
    __$__processingObjectId='${objectId}';
    __$__processingObjectTable='t_eem_excel_page';
    $(function () {
        $("table").attr("class", "table");
        $('input:text').attr("readonly", "readonly");

    })
    var __back_action = $('<a>返回</a>');
    var __reject_action = $('<a></a>');
    var __pass_action = $('<a></a>');
    __back_action.click(function () {
        <%
            String type = request.getAttribute("type").toString();
        %>
        location.href = "<%=path%>/base/page/<%=type%>.jsp";
    });
    __reject_action.click(__reject_feedback);
    __pass_action.click(__pass_feedback);
    function __reject_feedback() {
        $('#__pass_modal_window').modal('show');
        signFormSuccessLink('__pass_modal_body', 'N');
    }
    function __pass_feedback() {
        $('#__reject_modal_window').modal('show');
        signFormFailedLink('__reject_modal_body', 'N');
    }
    function closeModalWindow(modalWindow) {
        $('#' + modalWindow).modal('hide');
    }
   /* <%
    if(type.equals("todo")){
    %>
    $('#__link_bar').append(__reject_action).append(__pass_action).append(__back_action);
    <%}else{%>
    $('#__link_bar').append(__back_action);
    <%}%>*/
    function __resizeLinkDialog(__link_dialog_body_div, __width, __height, __btns) {
        if (__width) {
            $('#' + __link_dialog_body_div).parent().css('width', __width);
            $('#' + __link_dialog_body_div).parent().css('margin-left', -__width / 2);
        }
        if (__height) {
            __height = __height > document.documentElement.clientHeight ? document.documentElement.clientHeight : __height;
            if (__height < 350) {
                __height = 350;
            }
            $('#' + __link_dialog_body_div).parent().css('height', __height);
            if (__btns == 'none') {
                $('#' + __link_dialog_body_div).css('height', __height - 35);
            } else {
                $('#' + __link_dialog_body_div).css('height', __height - 75);
            }

            $('#' + __link_dialog_body_div).parent().css('margin-top', -__height / 2);
        }
    }
</script>
</html>
