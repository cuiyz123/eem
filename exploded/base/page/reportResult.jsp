<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>上报结果查询</title>
    <style>
        .table_name {
            font-size: 12px;
            line-height: 20px;
            color: #000000;
            text-decoration: none;
            text-align: center;
            background-color: #FFE8E8;
        }
    </style>
   <%-- //--------4.2---jw=============导出js插件//////////////////--%>
    <script type="text/javascript" src="<%=path%>/base/js/jquery.min.js"></script>
    <script type="text/javascript" src="<%=path%>/base/js/jquery-table2excel-master/src/jquery.table2excel.js"></script>
    <%-- //--------6.6---cuiyiz=============导出js插件//////////////////--%>
    <link rel="stylesheet" href="<%=path%>/base/js/bootstrap.min.css" type="text/css">
    <script type="text/javascript" src="<%=path%>/base/js/bootstrap.min.js"></script>

 <%--   //---------==============================================--%>
    <script type="text/javascript">
        function queryData() {

            //	var year=$("[name=yearStr]").val();
            //	var time=$("[name=time]").val();

            var year = document.getElementById("yearStr").value;
            var time = document.getElementById("time").value;
            if (time == "上半年") {
                document.location.href = "<%=request.getContextPath()%>/eemCommonController.do?method=queryReportResult" + "&year=" + year + "&dateStr=" + encodeURIComponent(encodeURIComponent("上半年"));
            } else if (time == "下半年") {
                document.location.href = "<%=request.getContextPath()%>/eemCommonController.do?method=queryReportResult" + "&year=" + year + "&dateStr=" + encodeURIComponent(encodeURIComponent("下半年"));
            } else {
                document.location.href = "<%=request.getContextPath()%>/eemCommonController.do?method=queryReportResult" + "&year=" + year + "&dateStr=" + time;
            }
        }
        function gotoDataQueryPage(type) {
            if (type == 'dataQueryAll') {
                //window.location = "<%=path%>/eemCommonController.do?method=initQueryDataAll";
                window.history.back();
            } else {
               // window.location = "<%=path%>/eemCommonController.do?method=initQueryData";
                window.history.back();
            }
        }
        function showDisplay(tempID, tempName, deptCode, deptName) {
            var year = document.getElementById("yearStr").value;
            var time = document.getElementById("time").value;
            $.ajax({
                type: "POST",
                url: "<%=path%>/eemCommonController.do?method=findExcelPages",
                data: {tempID: tempID, deptCode: deptCode, reportYear: year, reportDate: time},
                async: false,
                dataType: "json",
                success: function (data) {
                    var table = "<table class='table'><tr>" +
                            "<th>上报人</th><th>上报时间</th><th>上报人电话</th><th>上报类型</th><th>状态</th></tr>";
                    for (var i = 0; i < data.length; i++) {
                        table += '<tr><td><span>' + data[i].operUserTrueName + '</span></td><td><span>' + data[i].creationTime + '</span></td><td><span>' + data[i].operUserPhone + '</span></td>';
                        if (data[i].reportType == 'report') {
                            table += '<td><span>直接上报</span></td>';
                        } else {
                            table += '<td><span>汇总上报</span></td>';
                        }
                        table += '<td><span>' + data[i].workOrderStatus + '</span></td></tr>';
                    }
                    table = table + "</table>";
                    $('#__showDisplay_container').remove();
                    var length = 150;
                    if (data.length >= 7) {
                        length = 360
                    } else {
                        length = length + 30 * data.length;
                    }
                    ;
                    __open_window("showDisplay", deptName + "-" + tempName, 600, length, function (__window_body) {
                        __window_body.append(table);
                        var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                        var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
                        __applyEitorLink_btn_cancel.click(function () {
                            __window_body.parent().modal('hide');
                        });
                        __applyEitorLink_btns.append(__applyEitorLink_btn_cancel);
                        __window_body.parent().append(__applyEitorLink_btns);
                    });
                },
                error: function (request) {
                    alert("删除失败");
                }
            });
        }
        //--------4.2---jw=============///////////////////////////////////////////////////////
       function outtable(){
            console.log(1)
             alert("开始导出");
           $("#datatable").table2excel({
                exclude: ".noExl",
                name: "各地区厂商数据上报情况表",
                filename: "各地区厂商数据上报情况表",
                exclude_img: true,
                exclude_links: true,
                exclude_inputs: true
            });
           alert("导出完毕，请查看文件");
        }
        //---------==============================================

        //--------6.8---cuiyz=============///////////////////////////////////////////////////////
        function __open_window(__window, __title, __width, __height, __callback) {
            var __window_window = $('#__' + __window + '_container');
            if (!document.getElementById('__' + __window + '_container')) {
                __window_window = $('<div id="__' + __window + '_container" class="__link_dialog_container modal"></div>');
                var __window_header = $('<div class="modal-header">' + __title + '</div>');
                var __window_header_close_btn = $('<div class="close">×</div>');
                __window_header_close_btn.click(function () {
                    $(__window_window).modal('hide');
                });
                __window_header.append(__window_header_close_btn);
                var __window_body = $('<div class="modal-body"></div>');
                __window_window.append(__window_header);
                __window_window.append(__window_body);
                if (__width) {
                    __window_window.css('width', __width);
                    __window_window.css('margin-left', __width);
                }
                if (__height) {
                    __window_window.css('height', __height);
                    __window_body.css('height', __height - 85);
                    __window_window.css('margin-top', __height);
                }
                __window_window.modal({backdrop: 'static',show:true});
                $('body').append(__window_window);
                __callback(__window_body);
                drag(__window_window.get(0), __window_header.get(0));
            }else{
                var __window_header_close_btn = $('<div class="close">×</div>');
                __window_header_close_btn.click(function () {
                    $(__window_window).modal('hide');
                });
                $(".modal-header").html(__title).append(__window_header_close_btn);
            }
            __window_window.modal({backdrop: 'static',show:true});
            return __window_window;
        }
        //---------==============================================

    </script>
</head>
<body>
<h4>
    <c:if test="${userType=='UNI'}">
        各省
    </c:if>
    <c:if test="${userType=='PRO'}">
        本省
    </c:if>
    <c:if test="${userType=='CITY'}">
        本地市
    </c:if>
    <select name="yearStr" id="yearStr">
        <c:forEach begin="2015" end="${endYear}" var="y" step="1">
            <c:if test="${y eq yearStr}" var="ss">
                <option value="${y }" selected="selected">${y }</option>
            </c:if>
            <c:if test="${!ss}">
                <option value="${y }">${y }</option>
            </c:if>

        </c:forEach>
    </select>
    <select name="time" id="time">
        <c:forEach items="${timeList}" var="s">
            <c:if test="${s eq dateStr }" var="t">
                <option selected="selected" value="${dateStr}">${dateStr}</option>
            </c:if>
            <c:if test="${!t}" var="t">
                <option value="${s}">${s}</option>
            </c:if>
        </c:forEach>
    </select>
    上报情况
    &nbsp;&nbsp;&nbsp;&nbsp;
    <span class="btn btn-danger" onclick="queryData()">查询</span>
    <span class="btn btn-danger" onclick="gotoDataQueryPage('${backType}')">返回</span>
    <span class="btn btn-danger" onclick="outtable()">导出</span>
    <%--<span style="font-size:12px;float:right;">注意：针对最近一次上报状态为 已上报已审核：显示“1”；已上报未审核：显示“0”；未上报：显示空白&nbsp;&nbsp;</span>--%>
</h4>
<table cellPadding="3" cellSpacing="1" id="datatable" class="table" width="100%">

     <tr>
         <td class="table_name" style="vertical-align: bottom"  width="100%" >
         注意：针对最近一次上报状态为 已上报已审核：显示“1”；已上报未审核：显示“0”；未上报：显示空白
         </td>
     </tr>
    <%--  4.9jw 更改--%>
    <tr>
        <td class="table_name" style="vertical-align: bottom">
            部门
        </td>

        <c:forEach items="${excelTempletList }" var="excelTemple" varStatus="index">
            <td class="table_name" style="vertical-align: bottom">
                    ${excelTemple.shortName }
            </td>
        </c:forEach>

    </tr>
    <c:forEach items="${groupList }" var="group">
        <tr>
            <td class='table_name' nowrap="nowrap">
                    ${group.orgName}
            </td>
            <c:forEach items="${excelTempletList }" var="excelTemple" varStatus="index">
                <td class="form_content">
                    <div align="center">
                        <c:set var="report" value="none"></c:set>
                        <c:forEach items="${pageList }" var="page">
                            <c:if test="${page.tpinputid==excelTemple.objectId and page.reportorgcode==group.orgCode}">
                                <a href="javascript:"
                                   onclick="showDisplay(${page.tpinputid},'${excelTemple.shortName}',${page.reportorgcode},'${group.orgName}')">${page.num}</a>
                            </c:if>
                        </c:forEach>
                    </div>
                </td>
            </c:forEach>
        </tr>
    </c:forEach>
</table>

</body>
</html>
