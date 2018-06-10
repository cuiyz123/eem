<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="../basePage.jsp"/>
<html>
<head>

</head>
<body>
<h2>主办</h2>
<div class="container-fluid" style="">
    <div class="row">
        <div  class="col-md-4"></div>
        <div  class="col-md-4">
            <form id="form4submit" action="<%=request.getContextPath()%>/test.do?method=submit" method="post">
                <div class="form-group" style="border:10px dashed #bbbbbb;padding: 50px;border-radius: 10px">
                    <table class="table table-striped table-hover">
                        <tbody>
                            <tr>
                                <th>任务ID</th>
                                <td><%=request.getParameter("id")%><input name="id" type="hidden" value="<%=request.getParameter("id")%>"/></td>
                            </tr>
                            <tr>
                                <th>任务环节名称</th>
                                <td><%=request.getParameter("name")%></td>
                            </tr>
                            <tr>
                                <th>流程模板ID</th>
                                <td><%=request.getParameter("processId")%></td>
                            </tr>
                            <tr>
                                <th>流程实例ID</th>
                                <td><%=request.getParameter("processInstanceId")%><input name="processInstanceId" type="hidden" value="<%=request.getParameter("processInstanceId")%>"/></td>
                            </tr>
                            <tr>
                                <th>流程启动者</th>
                                <td><%=request.getParameter("createdById")%></td>
                            </tr>
                            <tr>
                                <th>到单时间</th>
                                <td><%=request.getParameter("createdOn")%></td>
                            </tr>
                            <tr>
                                <th>任务状态</th>
                                <td><%=request.getParameter("status")%></td>
                            </tr>
                        </tbody>
                    </table>
                    <input type="text" class="form-control" id="nextUserID" name="nextUserID" placeholder="下一步处理人">
                    <br/>
                    <span onclick="checkForm()" class="btn btn-info">提交</span>
                    <a href="<%=request.getContextPath()%>/test.do?method=todo"><span class="btn btn-danger">返回</span></a>
                </div>
            </form>
        </div>
    </div>
</div>
<script>
function trim(str){
    return str.replace(/(^\s*)|(\s*$)/g, "");
}
function checkForm(){
    var nextUser = document.getElementById('nextUserID');
    if(trim(nextUser.value) == ''){
        nextUser.focus();
        return false;
    }
    document.getElementById('form4submit').submit();
}

</script>
</body>
</html>
