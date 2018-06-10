<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
<title>综合运维门户</title>
<style type="text/css">
a , a:active {
    color: #ff0000;
    cursor: pointer;
    display: block;
    text-decoration:none;
}
a:hover {
    color: #ff0000;
    text-decoration: underline;
}
#indexTodoTable{
    width:100%;
}
    #indexTodoTable td{
        border-bottom: 1px solid #bbb;
        font-size: 12px;
        line-height: 30px;
        border-collapse:collapse;
        font-family: 宋体;
        width:25%;
        padding-left: 20px;
    }
</style>
</head>
<body>
    <table id="indexTodoTable">
        <tr>
            <td>电路调度</td>
            <td><a href="javascript:window.parent.openNewUrl('absolutePath','http://10.249.6.35/IOMPROJ/workorder/dispatchWorkOrderToDo.jsp','','运维管理->电路调度')">5</a></td>
            <td>重保管理</td>
            <td><a href="javascript:window.parent.openNewUrl('absolutePath','http://10.249.6.35/EOM_KGM/frame/frame.jsp','','生产管理->重保管理')">1</a></td>
        </tr>
        <tr>
            <td>任务管理</td>
            <td><a href="javascript:window.parent.openNewUrl('absolutePath','http://10.249.6.35/EOM_TM/frame/frame.jsp','','生产管理->任务管理')">1</a></td>
            <td>故障管理</td>
            <td><a href="javascript:window.parent.openNewUrl('absolutePath','http://10.249.6.35/comws/frontFrame.jsp','','生产管理->故障管理')">2</a></td>
        </tr>
        <tr>
            <td>局数据管理</td>
            <td><a>0</a></td>
            <td>版本管理</td>
            <td><a>0</a></td>
        </tr>
        <tr>
            <td>割接管理</td>
            <td><a>1</a></td>
            <td>在线操作</td>
            <td><a>0</a></td>
        </tr>
    </table>
</body>
<script>
</script>
</html>