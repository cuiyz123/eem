<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="../basePage.jsp"/>
<html>
<head>

</head>
<body>
<h2>拟稿</h2>
<div class="container-fluid"  style="margin-top:100px;">
    <div class="row">
        <div  class="col-md-4"></div>
        <div  class="col-md-4">
            <form id="form4draft" action="<%=request.getContextPath()%>/test.do?method=start" method="post">
                <div class="form-group" style="border:10px dashed #bbbbbb;padding: 50px;border-radius: 10px">
                    <label class="login-title">流程模板</label>
                    <select  name="processId" class="form-control">
                        <option value="com.test.bpmn">com.test.bpmn</option>
                        <option value="com.test20150708.bpmn">com.test20150708.bpmn</option>
                    </select>
                    <label class="login-title">下一步处理人</label>
                    <input type="text" class="form-control" id="nextUserID" name="nextUserID" placeholder="下一步处理人">
                    <select  name="need" class="form-control">
                        <option value="true">需要审核</option>
                        <option value="false">不需审核</option>
                    </select>
                    <br/>
                    <span onclick="checkForm()" class="btn btn-info">提交</span>
                    <a href="javascript:history.go(-1)"><button class="btn btn-danger">返回</button></a>
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
    document.getElementById('form4draft').submit();
}

</script>
</body>
</html>
