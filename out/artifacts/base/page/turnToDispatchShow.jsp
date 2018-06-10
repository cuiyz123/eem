<%@ page import="com.metarnet.core.common.utils.Constants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
<script type="text/javascript">
    __$__processingObjectId='${disCommonModel.objectId}';
    __$__processingObjectTable='<%=Constants.DIS_TABLE%>';
</script>
</head>
<body>
<table class="table">
    <tbody>
        <tr>
            <th>转派人</th>
            <td>
                <span>${disCommonModel.operUserTrueName}</span>
            </td>
            <th>转派时间</th>
            <td colspan="3">
                <span><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${disCommonModel.creationTime}"/></span>
            </td>
        </tr>
        <tr>
            <th>转派人单位</th>
            <td colspan="5">
                <span>${disCommonModel.operOrgName}</span>
            </td>
        </tr>
        <tr>
            <th>转派对象</th>
            <td colspan="5">
                <span>${disCommonModel.mainTransferLabel}</span>
            </td>
        </tr>
        <tr>
            <th>要求完成时间<span style="color:red;">*</span></th>
            <td colspan="5">
                <span><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${disCommonModel.reqFdbkTime}"/></span>
            </td>
        </tr>
        <%--<tr>
            <th>抄送</th>
            <td>
                <div class="input-group">
                    <input id="copyID" name="copyID" type="hidden">
                    <input id="copyLabel" name="copyLabel" readonly="readonly" type="text" class="form-control">
                    <span id="__person_tree_to_copy" class="input-group-addon glyphicon glyphicon-th"></span>
                </div>
            </td>
        </tr>--%>
        <tr>
            <th>转派说明</th>
            <td colspan="5">
                <span>${disCommonModel.operDesc}</span>
            </td>
        </tr>
        <tr>
            <th>附件</th>
            <td colspan="5"><input id="__file_upload_turn_dispatch_show" type="file" multiple="true"/></td>
        </tr>
        <tr>
    </tbody>
</table>
<script>
__init_attachment_function('__file_upload_turn_dispatch_show' , '${disCommonModel.objectId}' , '<%=Constants.DIS_TABLE%>' , 'show');
</script>
</body>
</html>
