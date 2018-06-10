<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%--
  Created by IntelliJ IDEA.
  User: dong
  Date: 2017/5/16
  Time: 16:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>按专业配置所有模板</title>
    <style>
        .table th,td{
            text-align: center;
        }
        .contaier-botom{
            margin-bottom:15px;
        }
        .table-two tr,td,th{
            border: none;
        }
    </style>
</head>
<%--<script type="text/javascript" src="<%=path%>/base/js/jquery.similar.msgbox.js"></script>--%>
<body>
<div class="big-title-metar">按专业配置所有模板</div>
<form id="setTemplageInfo" method="post">
    <div class="container-fluid contaier-botom">
        <div class="row" style="margin-right: 100px;">
            <div class="col-xs-2 col-sm-2" style="padding-left: 70px">选择模板：</div>
            <div class="col-xs-4 col-sm-4" style="padding: 0px">
                <select  style="width: 80%" name="temp" id="temp" onchange="temptChange()">
                    <option value="" selected>请选择</option>
                    <%
                        try {
                            List<Map> list = (List) request.getAttribute("temps");
                            StringBuilder sb = new StringBuilder();
                            if (list != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    sb.append("<option value=\"" + list.get(i).get("objectid") + "\"> "+list.get(i).get("tempname") + "</option>");
                                }
                                out.print(sb.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    %>
                </select>
            </div>
        </div>
    </div>
    <table class="table">
        <tr>
            <th>模板</th>
            <th>专业</th>
        </tr>
        <c:forEach items="${temps2}" var="obj" varStatus="sta">
            <tr>
                <td>${obj.shortName}</td>
                <td>
                    <table style="width: 100%;border:none;" class="table-two">
                        <tr>
                            <c:if test="${userName=='root'}">
                                <c:forEach items="${obj.nodeVoSet}" var="sobj" varStatus="sta2">
                                <c:if test="${(sta2.index)%4==0}">
                        </tr><tr>
                        </c:if>

                            <%--<td>  <input type="checkbox" name="nodeSet"  readonly><span> ${sobj.sname}</span></td>--%>

                        <td>  <input type="checkbox" name="nodeSet"
                        <c:if test="${sobj.checked}">
                                     checked
                        </c:if> value="${obj.objectId}:${sobj.objectId}:${sobj.scode}:${sobj.sname}:${obj.shortName}"><span> ${sobj.sname}</span></td>
                            <%--<td>${status.current}</td>--%>
                        </c:forEach>
                            </c:if>
                            <c:if test="${userName!='root'}">
                            <c:forEach items="${obj.nodeVoSet}" var="sobj" varStatus="sta2">
                            <c:if test="${(sta2.index)%4==0}">
                    </tr><tr>
                        </c:if>

                            <%--<td>  <input type="checkbox" name="nodeSet"  readonly><span> ${sobj.sname}</span></td>--%>

                        <td>  <input type="checkbox" name="nodeSet"
                        <c:if test="${sobj.checked}">
                                     checked
                        </c:if> disabled><span> ${sobj.sname}</span></td>
                            <%--<td>${status.current}</td>--%>
                        </c:forEach>
                            </c:if>

                        </tr>
                    </table>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${userName=='root'}">
            <tr>
                <td colspan="2"  style="padding-top: 5px;border: none;height:50px;">
                    <input type="button" class="btn btn-danger" value="保存" onclick="saveTempInfo()">
                    <input type="button" class="btn btn-danger" value="清空" onclick="resetForm()">
                    <input type="button" class="btn btn-info" value="全选" onclick="selectAll();">
                </td>
            </tr>
        </c:if>

    </table>
</form>
<script>

    function saveTempInfo(){
//        $.MsgBox.Confirm("温馨提示", "确认提交模板与专业的更改信息？",function(){
        $.ajax({
            type: "POST",
            url: "eemCommonController.do?method=saveTemptSpecInfo",
            data: $('#setTemplageInfo').serialize(),
            async: false,
            success: function (data) {
//                    $.MsgBox.Alert("温馨提示", "保存成功！");
                alert("温馨提示,保存成功！");
            },
            error: function (request) {
//                    $.MsgBox.Alert("温馨提示", "保存失败！");
                alert("温馨提示, 保存失败！");
            }
//            });
        });
    }

 /* function saveTempInfo(){
        $.MsgBox.Confirm("温馨提示", "确认提交模板与专业的更改信息？",function(){
            $.ajax({
                type: "POST",
                url: "eemCommonController.do?method=saveTemptSpecInfo",
                data: $('#setTemplageInfo').serialize(),
                async: false,
                success: function (data) {
                    $.MsgBox.Alert("温馨提示", "保存成功！");
                },
                error: function (request) {
                    $.MsgBox.Alert("温馨提示", "保存失败！");
                }
            });
        });
    }*/
    function temptChange(){
        var temp = $("#temp").val();
        window.location.href = "eemCommonController.do?method=initManagerTemplate&temp=" + temp;
    }
    function resetForm() {
        $("input:not(:button,:hidden)").prop("checked", false);
    }
    function selectAll(){
        $("input:not(:button,:hidden)").prop("checked", true);
    }
</script>
</body>
</html>
