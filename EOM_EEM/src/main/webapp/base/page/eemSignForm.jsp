<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String __signFormAction = request.getParameter("__signFormAction");
    __signFormAction = (__signFormAction == null || "".equals(__signFormAction)) ? "Y" : __signFormAction;
    String __batchFlag = request.getParameter("__batchFlag");
    String up = request.getParameter("up");
%>
<html>
<body>
<form id="__sign_form_<%=__signFormAction%>">
    <table class="__dialog_panel_table">
        <tbody>
        <tr>
            <th><span class="__require">*</span>审核意见</th>
            <td colspan="2"><textarea id="approvalOpinion_<%=__signFormAction%>" class="form-control"></textarea></td>
        </tr>
        <%
            if (up.equals("Y")) {
        %>
        <tr>
            <th>是否上报</th>
            <td style="text-align: center;border: none">
                <span class="radio">
                    <label>
                        <input name="report" type="radio" value="Y">是
                    </label>
                </span>
            </td>
            <td style="text-align: center;border: none">
                <span class="radio">
                    <label>
                        <input name="report" type="radio" value="N" checked>否
                    </label>
                </span>
            </td>
        </tr>
        <% }
        %>

        </tbody>
    </table>
    <div class="__dialog_panel_btns">
        <span id="__submit_sign_form_<%=__signFormAction%>" class="btn btn-danger">提交</span>
        <span id="__reset_sign_form_<%=__signFormAction%>" class="btn btn-default">重置</span>
    </div>
</form>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
    <%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
    $('#__submit_sign_form_<%=__signFormAction%>').click(function () {
        var data = {};
        data.operTypeEnumId = '40050227';
        var req_url = 'eemCommonController.do?method=saveAudit';
        if ('<%=__batchFlag%>' == 'Y') {
//            req_url = 'commFeedbackController.do?method=approval';
            var processInstID = '';
            var __selectedFeedbacks = __getCheckedFeedbacks();
            if(__selectedFeedbacks.length == 0){
                alert('请选择需要操作的数据');

                return;
            }
            for (var i = 0; i < __selectedFeedbacks.length; i++) {
                processInstID += __selectedFeedbacks[i]['processInstID'];
                if (i < __selectedFeedbacks.length - 1) {
                    processInstID += ',';
                }
            }
            data.processInstID = processInstID;
        } else {
            if (__$__processingObjectId == 0) {
                alert('未设置当前审核对象ID：__$__processingObjectId');
                return;
            } else if (__$__processingObjectTable == 0) {
                alert('未设置当前审核对象Table：__$__processingObjectTable');
                return;
            }
            data.processingObjectID = __$__processingObjectId;
            data.processingObjectTable = __$__processingObjectTable;
        }
        data.operDesc = $('#approvalOpinion_<%=__signFormAction%>').val();
        if (data.operDesc == '') {
            $('#approvalOpinion_<%=__signFormAction%>').addClass('__notnull');
            $('#approvalOpinion_<%=__signFormAction%>').focus();
            return;
        }
        data.processingStatus = '<%=__signFormAction%>';

        if($("input[name='report']").length>0){
            if($("input[name='report']")[0].checked){
                data.report = $("input[name='report']")[0].value;
            }else{
                data.report = $("input[name='report']")[1].value;
            }
        }
        __show_metar_loading();
        $.ajax({
            url: req_url,
            type: 'POST',
            async: true,
            dataType: "json",
            data: data,
            success: function (response) {
                if (response.success) {
                    location.href = _PATH + "/eemCommonController.do?method=todo&type=1&nodeID=todoAudit";
                } else {
                    __hide_metar_loading();
                    if(response.msg){
                        alert(response.msg);
                    } else {
                        alert('审核失败，请重试。');
                    }
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
//            alert('error' + errorThrown);
                __hide_metar_loading
                alert('审核失败，请重试。');
            }
        })
    });

    $('#__reset_sign_form_<%=__signFormAction%>').click(function () {
        document.getElementById('__sign_form_<%=__signFormAction%>').reset();
        $('#approvalOpinion_<%=__signFormAction%>').focus();
    });

    if ('<%=__signFormAction%>' == 'Y') {
        $('#approvalOpinion_Y').val('通过');
    }
</script>
</body>
</html>
