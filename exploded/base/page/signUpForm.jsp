<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String __signFormAction = request.getParameter("__signFormAction");
    __signFormAction = (__signFormAction == null || "".equals(__signFormAction)) ? "Y" : __signFormAction;
%>
<html>
<body>
<form id="__sign_form_up_<%=__signFormAction%>">
    <table class="__dialog_panel_table">
        <tbody>
        <tr>
            <th>上报说明</th>
            <td colspan="2"><textarea id="approvalOpinion_up_<%=__signFormAction%>" class="form-control"></textarea></td>
        </tr>
        </tbody>
    </table>
    <div class="__dialog_panel_btns">
        <span id="__submit_sign_form_up_<%=__signFormAction%>" class="btn btn-danger">提交</span>
        <span id="__reset_sign_form_up_<%=__signFormAction%>" class="btn btn-default">重置</span>
    </div>
</form>
<script>
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
    <%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
    $('#__submit_sign_form_up_<%=__signFormAction%>').click(function () {
        var data = {};
        data.operTypeEnumId = '40050227';
        var req_url = 'workBaseController.do?method=saveAudit';

        if (__$__processingObjectId == 0) {
            alert('未设置当前审核对象ID：__$__processingObjectId');
            return;
        } else if (__$__processingObjectTable == 0) {
            alert('未设置当前审核对象Table：__$__processingObjectTable');
            return;
        }
        data.processingObjectID = __$__processingObjectId;
        data.processingObjectTable = __$__processingObjectTable;
        for (var pro in _winParams) {
            data[pro] = _winParams[pro];
        }
        data.TASKLIST = TASKLIST;
        data.operDesc = $('#approvalOpinion_up_<%=__signFormAction%>').val();
        /*if (data.operDesc == '') {
            $('#approvalOpinion_up_<%=__signFormAction%>').addClass('__notnull');
            $('#approvalOpinion_up_<%=__signFormAction%>').focus();
            return;
        }*/
        data.processingStatus = '<%=__signFormAction%>';
        data.attribute1 = 'up';

        if ($("input[name='report']").length > 0) {
            if ($("input[name='report']")[0].checked) {
                data.report = $("input[name='report']")[0].value;
            } else {
                data.report = $("input[name='report']")[1].value;
            }
        }
        __show_metar_loading();
        data.levelType = 'up';
        $.ajax({
            url: req_url,
            type: 'POST',
            async: true,
            dataType: "json",
            data: data,
            success: function (response) {
                if (response.success) {
                    __exitFromFrame();
                } else {
                    __hide_metar_loading();
                    if (response.msg) {
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

    $('#__reset_sign_form_up_<%=__signFormAction%>').click(function () {
        document.getElementById('__sign_form_up_<%=__signFormAction%>').reset();
        $('#approvalOpinion_up_<%=__signFormAction%>').focus();
    });
</script>
</body>
</html>
