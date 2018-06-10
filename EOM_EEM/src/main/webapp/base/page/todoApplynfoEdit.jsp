<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>申请展示</title>
</head>
<body>
<table class="table table-form-4">
    <tr>
        <th>主题<font color=red>*</font></th>
        <td>
            <span>${problem.tpInputName}</span>
        </td>
        <th>提出时间<font color=red>*</font></th>
        <td>
            <span>${problem.creationTime}</span>
        </td>
    </tr>
    <tr>
        <th>上报人</th>
        <td>
            <span>${problem.reportUserTrueName}</span>
        </td>
        <th>上报人部门</th>
        <td>
            <span>${problem.reportOrgName}</span>
        </td>
    </tr>
    <tr>
        <th>申请原因<font color=red>*</font></th>
        <td>
            <span>${problem.reason}</span>
        </td>
        <th>上报粒度</th>
        <td>
            <span>${problem.theme}</span>
        </td>

    </tr>
    <form id="problemReport" method="post">
    <tr>
        <th>问题答复人</th>
        <td>
            <input class="form-control __metar_check_form" type="text" name="operUserTrueName" value="${user.trueName}" readonly="true">
        </td>
        <th><font color=red>*</font>回复意见</th>
        <td>
            <%--<input class="form-control __metar_check_form" type="text" name="operDesc">--%>
            <select name="result">
                <option value="同意">同意</option>
                <option value="不同意">不同意</option>
            </select>
            <input  type="hidden" name="objectId" value="${problem.objectId}">
        </td>
    </tr>

    <tr>
        <th>描述</th>
        <td colspan="3">
            <input type="text" name="auditReason" class="form-control">
        </td>
    </tr>
    </form>
</table>

</body>
<script>
    function submitBtn() {

        if(!__metar_check_form(document.getElementById("feedbackForm"))){
            return;
        }
        var url= "<%=request.getContextPath()%>/eemCommonController.do?method=saveApply2";
        document.getElementById("problemReport").action= url;
        document.getElementById("problemReport").submit();

    }

</script>
</html>
