<%@ page import="java.util.Calendar" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>上报数据展示</title>
    <script type="text/javascript" src="<%=path%>/base/js/eemSignFormSuccessLink.js"></script>
    <script type="text/javascript" src="<%=path%>/base/js/eemSignFormFailedLink.js"></script>
    <%-- //--------4.9---jw=============导出js插件//////////////////--%>
    <script type="text/javascript" src="<%=path%>/base/js/jquery.min.js"></script>
    <script type="text/javascript" src="<%=path%>/base/js/jquery-table2excel-master/src/jquery.table2excel.js"></script>
    <%-- //--------6.6---cuiyiz=============导出js插件//////////////////--%>
    <link rel="stylesheet" href="<%=path%>/base/js/bootstrap.min.css" type="text/css">
    <script type="text/javascript" src="<%=path%>/base/js/bootstrap.min.js"></script>
    <%--<link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>--%>
    <%--   //---------==============================================--%>
    <script type="text/javascript">

    //--------4.10---jw=============///////////////////////////////////////////////////////
    function outtable(){
    console.log(1)
    alert("开始导出");
    $("#datatable").table2excel({
    exclude: ".noExl",
    name: "数据详情表",
    filename: "数据详情表",
    exclude_img: true,
    exclude_links: true,
    exclude_inputs: true
    });
    alert("导出完毕，请查看文件");
    }
    </script>

</head>
<body>
<input type="hidden" id="tempIds" value="${tempIds}">
<div id="__link_bar">
   <span class="btn btn-danger btn-xs" onclick="outtable()">导出</span>
    <%--<div style="color: #ea0000" onclick="outtable()">导出</div>--%>
</div>



<%--<span class="btn btn-danger btn-xs" onclick="javascript:window.history.go(-1)">返回</span>--%>
  <%-- 添加table   jw4.9--%>
<%--$('table').attr('id','id_name');--%>
${tempHTML}

<%--</table>--%>

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
    //
    $('table').attr('id','datatable');     // jw 4.12
    //

    __$__processingObjectId='${objectId}';
    __$__processingObjectTable='t_eem_excel_page';
    $(function () {
        $("table").attr("class", "table");
        $('input:text').attr("readonly", "readonly");

    })
    var __back_action = $('<a>返回</a>');
    var __reject_action = $('<a>通过</a>');
    var __pass_action = $('<a>驳回</a>');
    __back_action.click(function () {
        <%
            String type = request.getAttribute("type").toString();
//            String provinceName = request.getAttribute("provinceName").toString();
//            String provinceCode = request.getAttribute("provinceCode").toString();
//            String yearStr = request.getAttribute("yearStr").toString();
//            request.setAttribute("yearStr", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            String objectId = request.getAttribute("objectId").toString();
//            request.setAttribute("objectId", objectId);

        %>
        debugger;
        if("todo"=="<%=type%>"){
           // location.href = "base/page/todo.jsp";
            window.history.back();
//            window.location.replace(document.referrer);//刷新整个jsp界面，不可用

        }else if("already"=="<%=type%>"){
            <%--location.href = "<%=path%>/eemCommonController.do?method=already";--%>
            window.history.back();
        }else if("dataQuery"=="<%=type%>"){
/*
            location.href = "<%=path%>/eemQueryController.do?method=queryDataList&tempIds="+$('#tempIds').val();
*/


                    window.history.back();
        }else if("dataQueryAll"=="<%=type%>"){
            <%--location.href = "<%=path%>/eemCommonController.do?method=initQueryDataAll";--%>
            window.history.back();
        }else{
            <%--location.href = "<%=path%>/base/page/<%=type%>.jsp?provinceCode=<%=provinceCode%>&provinceName=<%=provinceName%>&yearStr=<%=yearStr%>";--%>
            window.history.back();
        }

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
    <%
    if(type.equals("todo")){
    %>
    $('#__link_bar').append(__reject_action).append(__pass_action).append(__back_action);
    <%}else{%>
    $('#__link_bar').append(__back_action);
    <%}%>
    function __resizeLinkDialog(__link_dialog_body_div, __width, __height, __btns) {
        if (__width) {
            $('#' + __link_dialog_body_div).parent().css('width', __width);
            $('#' + __link_dialog_body_div).parent().css('margin-left', +__width );
        }
        if (__height) {
            __height = __height > document.documentElement.clientHeight ? document.documentElement.clientHeight : __height;
            if (__height < 350) {
                __height = 220;
            }
            $('#' + __link_dialog_body_div).parent().css('height', __height);
            if (__btns == 'none') {
                $('#' + __link_dialog_body_div).css('height', __height - 35);
            } else {
                $('#' + __link_dialog_body_div).css('height', __height - 75);
            }

            $('#' + __link_dialog_body_div).parent().css('margin-top', +__height);
        }
    }
</script>
</html>
