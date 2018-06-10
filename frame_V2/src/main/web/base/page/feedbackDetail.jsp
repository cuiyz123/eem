<%@ page import="com.metarnet.core.common.utils.Constants" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/base/basePage.jsp" %>
<%
    //String path = request.getContextPath();
    // String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    String processInstID = request.getParameter("processInstID");
    String rootId = request.getParameter("rootId");
    String appId = request.getParameter("appId");
    //String fromPage = request.getParameter("fromPage");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="<%=path%>/component/jquery-treetable/css/jquery.treetable.css" type="text/css">
    <link rel="stylesheet" href="<%=path%>/component/jquery-treetable/css/jquery.treetable.theme.default.css"
          type="text/css">
</head>
<style type="text/css">
    .dib {
        margin-left: 15px;
        height: 30px;
    }
</style>
<body>
<div id="__link_bar"></div>
<div id="__page_big_title" class="big-title-metar">工单全景图</div>

<input type="hidden" name="objectIds" value="${tEomTaskDisForm.objectId}">
<c:if test="${taskAppForm!=null}">
    <c:if test="${isRoot=='root'}">
        <div class="dib"><span class="">申请单：<a
                onclick="openWin('${appMethod}${taskAppForm.objectId}')">${taskAppForm.appOrderNumber}</a></span>
        </div>
    </c:if>
</c:if>
<c:if test="${tEomTaskDisForm!=null}">
    <c:if test="${isRoot=='root'}">
        <div class="dib"><span class="">调度单：<a
                onclick="openWin('${disMethod}${tEomTaskDisForm.objectId}')">${tEomTaskDisForm.disOrderNumber}</a></span>
        </div>
    </c:if>
    <c:if test="${isRoot!='root'}">
        <div class="dib"><span class="">转派单：<a
                onclick="openWin('/showTurnToDispatch.do?processInstID=${tEomTaskDisForm.processInstId}')">${tEomTaskDisForm.disOrderNumber}</a></span>
        </div>
    </c:if>
</c:if>
<div id="__feedback_list_view_table" class="modal-body __feedback_list_view_table"></div>


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
<script type="text/javascript" src="<%=path%>/component/jquery-treetable/jquery.treetable.js"></script>
<script type="text/javascript" src="<%=path%>/base/js/signFormSuccessLink.js"></script>
<script type="text/javascript" src="<%=path%>/base/js/signFormFailedLink.js"></script>
<script type="text/javascript">
    var _winParams = {};
    _winParams.rootProcessInstId = '<%=rootId%>';
    _winParams.jobID = '<%=appId%>';
    __show_metar_loading();
    var __selectedFeedbacks = new Array();

    $(document).ready(function () {//初始化ztree对象
        $.ajax({
            url: _PATH + '/commFdbkListController.do?method=getFeedbackListByDispatch',
            data: {processInstID: '<%=processInstID%>', activityDefID: ''},
            method: 'POST',
            async: true,
            dataType: 'json',
            success: function (response) {
                var __table = $('<table class="table"></table>');
                var __tbody = $('<tbody></tbody>');
                var __tr_title = $('<tr><th>反馈单位</th><th>反馈人</th><th>反馈时间</th><th>要求反馈时间</th><th>工单状态</th><th>审核状态</th><th>审核说明</th></tr>');
                __tbody.append(__tr_title);
                if (response) {
                    for (var i = 0; i < response.length; i++) {
                        var fdbk = response[i];
                        var __tr = $('<tr></tr>');
//                    var __check_td = fdbk.workOrderStatus == '已反馈' ? $('<td><span></span></td>') : $('<td></td>');
                        var __disAssignObjectName_td = fdbk.attribute5 == "Y" ? $('<td><span><a href="' + _PATH + '/workBaseController.do?method=LookFormDetail&processInstID=' + fdbk.processInstId + '" target="_blank">' + fdbk.disAssignObjectName + '</a></span></td>') : $('<td><span>' + fdbk.disAssignObjectName + '</span></td>');
                        var __operUserTrueName_td = fdbk.operUserTrueName ? $('<td><span><a id="' + new Date().getTime() + '" title="' + fdbk.operUserTrueName + '" onclick="__show_person_info(this.id , \'' + fdbk.operUserId + '\');">' + fdbk.operUserTrueName + '</a></span></td>') : $('<td></td>');
                        var __operTime_td = fdbk.operTime ? $('<td><span>' + fdbk.operTime + '</span></td>') : $('<td></td>');
                        var __reqFdbkTime_td = $('<td><span>' + fdbk.reqFdbkTime + '</span></td>');
                        var __workOrderStatus_td = "";
                        if (fdbk.workOrderStatus == '未签收' || fdbk.workOrderStatus == '已接单' || fdbk.workOrderStatus == '未反馈') {
                            __workOrderStatus_td = $('<td><span>' + fdbk.workOrderStatus + '</span></td>')
                        } else {
                            __workOrderStatus_td = $('<td><span><a href="' + _PATH + '${feedBackMethod}' + fdbk.processInstId + '" target="_blank">' + fdbk.workOrderStatus + '</a></span></td>')
                        }
                        var __signStatus_td = "";
                        if (fdbk.attribute1 == undefined) {
                            __signStatus_td = $('<td><span>未审核</span></td>')
                        } else {
                            if (fdbk.attribute1 == 'N') {
                                __signStatus_td = $('<td><span>驳回</span></td>')
                            } else {
                                __signStatus_td = $('<td><span>通过</span></td>')
                            }
                        }
                        var __signDesc_td = "";
                        if (fdbk.attribute2 != undefined) {
                            __signDesc_td = $('<td><span>' + fdbk.attribute2 + '</span></td>');
                        } else {
                            __signDesc_td = $('<td><span></span></td>');
                        }
                        __tr.append(__disAssignObjectName_td).append(__operUserTrueName_td).append(__operTime_td).append(__reqFdbkTime_td).append(__workOrderStatus_td).append(__signStatus_td).append(__signDesc_td);
                        __tbody.append(__tr);
                    }
                }
                __table.append(__tbody);
                $('#__feedback_list_view_table').empty().append(__table);
                __hide_metar_loading();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                __hide_metar_loading();
            }
        })
    });

    function __getCheckedFeedbacks() {
        __selectedFeedbacks = new Array();
        var feedbacks = $('input[name="__feedback_processsInstID_checkbox"]');
        for (var i = 0; i < feedbacks.length; i++) {
            if (feedbacks[i].checked) {
                __selectedFeedbacks.push({processInstID: feedbacks[i].value});
            }
        }
    }

    function __reject_feedback() {
        __getCheckedFeedbacks();
        if (__selectedFeedbacks.length == 0) {
            alert('请选择审核驳回的反馈');
            return;
        } else {
            $('#__reject_modal_window').modal('show');
            signFormFailedLink('__reject_modal_body', 'Y');
        }
    }
    function __pass_feedback() {
        __getCheckedFeedbacks();
        if (__selectedFeedbacks.length == 0) {
            alert('请选择审核通过的反馈');
            return;
        } else {
            $('#__pass_modal_window').modal('show');
            signFormSuccessLink('__pass_modal_body', 'Y');
        }
    }
    function closeModalWindow(modalWindow) {
        $('#' + modalWindow).modal('hide');
    }
    function __show_person_info(action_id, person_ids) {
        var user_properties = ['trueName', 'orgEntity', 'mobilePhone', 'email'];
        __open_metar_window(action_id, '用户信息', 960, 200, function (window_body) {
            __show_metar_loading();
            $.ajax({
                url: _PATH + '/commWorkFlowMonitorController.do?method=getUserEntityByIds&ids=' + person_ids,
                method: 'POST',
                async: true,
                dataType: 'json',
                success: function (response) {
                    if (response) {
                        var __table = $('<table class="table"></table>');
                        var __tbody = $('<tbody></tbody>');
                        var __tr_title = $('<tr><th>姓名</th><th>单位</th><th>电话</th><th>邮箱</th></tr>');
                        __tbody.append(__tr_title);
                        for (var i = 0; i < response.length; i++) {
                            var __tr = $('<tr></tr>');
                            for (var j = 0; j < user_properties.length; j++) {
                                var __td = $('<td><span>' + response[i][user_properties[j]] + '</span></td>');
                                if (user_properties[j] == 'orgEntity') {
                                    __td = $('<td><span>' + response[i][user_properties[j]]['orgName'] + '</span></td>');
                                }
                                __tr.append(__td);
                            }
                            __tbody.append(__tr);
                        }
                        __table.append(__tbody);
                        $(window_body).append(__table);
                        var __feedback_list_btns = $('<div class="__dialog_panel_btns"></div>');
                        var ____feedback_list_btn_close = $('<span class="btn btn-danger">关闭</span>');
                        ____feedback_list_btn_close.click(function () {
                            $(window_body).parent().modal('hide');
                        });
                        __feedback_list_btns.append(____feedback_list_btn_close);

                        $(window_body).parent().append(__feedback_list_btns);
                        __hide_metar_loading();
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    __hide_metar_loading();
                }
            })
        });

    }

    function openWin(url) {
        var uu = _PATH + url;
        window.open(uu);
    }
    <%
        String isshow=request.getParameter("show");
        if(isshow!=null&&"show".equals(isshow)){
    %>
    var __back_action = $('<a>返回</a>');
    var _flow_monitor = $('<a>流程监控</a>');
    __back_action.click(function () {
        location.href = "<%=basePath%>/base/frame/workorderList.jsp";
    });
    __bindAction2Link('流程监控', _flow_monitor, 'workFlowMonitor');
    $('#__link_bar').append(_flow_monitor).append(__back_action);
    <%
    }
    %>
    function __bindAction2Link(__link_name, __action, __link_file) {
        require('base/js/' + __link_file + '.js', function () {
            var __link_dialog_show = true;
            __action.bind('click', function () {
                var __link_dialog = $('#__' + __link_file + '_container');
                if (!document.getElementById('__' + __link_file + '_container') || !__link_dialog_show) {
                    __link_dialog = $('<div id="__' + __link_file + '_container" class="__link_dialog_container modal"></div>');
                    var __link_dialog_header = $('<div class="modal-header">' + __link_name + '</div>');
                    var __link_dialog_header_close_btn = $('<div class="close">×</div>');
                    __link_dialog_header_close_btn.bind('click', function () {
//                    debugger;
//                    alert(__link_dialog instanceof jQuery);
                        __link_dialog.modal('hide');
                        if (__link_dialog.css('display') != 'none') {
                            $(__link_dialog).modal('hide');
                        }
                    });
                    __link_dialog_header.append(__link_dialog_header_close_btn);
                    __link_dialog.append(__link_dialog_header);
                    __link_dialog.append($('<div class="modal-body" id="__' + __link_file + '_dialog_body"></div>'));
                    $('body').append(__link_dialog);
                    if (eval(__link_file + '(\'__' + __link_file + '_dialog_body\')') == false) {
                        __link_dialog_show = false;
                    }
                    drag(__link_dialog.get(0), __link_dialog_header.get(0));
                }
//            $('#__' + __link_file + '_container').show();
                if (__link_dialog_show) {
                    __link_dialog.modal('show');
                }
            });
        });
    }
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
</body>
</html>