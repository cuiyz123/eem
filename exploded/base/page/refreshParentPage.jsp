<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/base/basePage.jsp" %>


<html>
<head>
</head>
<body onload="submit2()">

<script>
    function submit2() {
        var url= "<%=request.getContextPath()%>/base/page/todoApply.jsp";
        parent.location.href= url;
    }

</script>

</body>


</html>


