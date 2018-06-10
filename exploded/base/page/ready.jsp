<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="../basePage.jsp"/>
<html>
<head>

</head>
<body>
<h2>已办查询</h2>
<div class="container-fluid" style="">
    <div class="row">
        <div class="col-md-12">
            <a href="index.jsp"><button class="btn btn-info">登录</button></a>
            <a href="base/page/draft.jsp"><button class="btn btn-info">拟稿</button></a>
            <a href="<%=request.getContextPath()%>/test.do?method=todo"><button class="btn btn-info">待办查询</button></a>
            <br/>
              <table class="table table-striped table-hover">
                  <tbody>
                      <tr>
                          <th></th>
                          <th>任务ID</th>
                          <th>任务环节名称</th>
                          <th>流程模板ID</th>
                          <th>流程实例ID</th>
                          <th>流程启动者</th>
                          <th>完成时间</th>
                          <th>任务状态</th>
                          <th>处理人</th>
                          <th>当前处理人</th>
                          <th></th>
                      </tr>
                  <c:forEach items="${todoList}"  var="todo" varStatus="status">
                       <tr>
                           <td>${status.index + 1}</td>
                           <td>${todo.id}</td>
                           <td>${todo.name}</td>
                           <td>${todo.processId}</td>
                           <td>${todo.processInstanceId}</td>
                           <td>${todo.createdById}</td>
                           <td>${todo.completeTime}</td>
                           <td>${todo.status}</td>
                           <td>${todo.actualOwnerId}</td>
                           <td>${todo.currentActors}</td>
                           <td style="padding:0px;"><a href="<%=request.getContextPath()%>/test.do?method=getWrokflowdiagram"><span class="btn btn-info">监控</span></a></td>
                       </tr>

                  </c:forEach>
                  </tbody>
              </table>

        </div>
    </div>
</div>
</body>
</html>
