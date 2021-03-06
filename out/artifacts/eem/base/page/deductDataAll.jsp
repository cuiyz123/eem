<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>集团扣分公示版下载</title>
    <script type="text/javascript">
        __show_metar_loading();
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'pagename',
                title: '设备类型',
                type: 'string',
                columnClass: 'text-center',
//                headerStyle: 'width:100px'
            },
            {
                id: 'tpinputname',
                title: '文件名称',
                type: 'string',
                columnClass: 'text-center'
//                headerStyle: 'width:80px'
            },
            {
                id: 'reportdate',
                title: '上报周期',
                type: 'string',
                columnClass: 'text-center',
//                headerStyle: 'width:80px',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return record.reportyear+record.reportdate;
                }
            },
            {
                id: 'creation_time',
                title: '填报时间',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'cancelperson',
                title: '操作',
                type: 'string',
                columnClass: 'text-center',
//                headerStyle: 'width:160px',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='showReportData("+record.pageid+","+record.attachmentid+")'>查看</a>&nbsp;<a href='javascript:;' onclick='downReportData("+record.attachmentid+")'>下载</a>";
//                    return "<a href='javascript:;' onclick='downReportData(" + value + ")'>下载</a>";
                }
            }
        ];
        var dtGridOption_2_1_2 = {
            lang: 'zh-cn',
            ajaxLoad: true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryDeductList',
            exportFileName: '数据查询列表',
            columns: dtGridColumns_2_1_2,
            gridContainer: 'dtGridContainer_2_1_2',
            toolbarContainer: 'dtGridToolBarContainer_2_1_2',
            tools: 'refresh',
            pageSize: 1000,
            pageSizeLimit : []
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function () {
                __hide_metar_loading();
            });
            $('#custom_search_2_2_3').click(searchProData);
        });


        function searchProData() {
            grid_2_1_2.parameters = new Object();

            grid_2_1_2.parameters['year'] = $('#year').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();

            grid_2_1_2.refresh(true);
        }
        function showReportData(pageid,objectId) {
            window.location.href = "<%=path%>/deductController.do?method=showReportData&attachmentid=" + objectId+"&pageid="+pageid ;
        }
        function downReportData(objectId) {
            window.location.href = "<%=path%>/deductController.do?method=download&attachmentid=" + objectId;
        }
        function getDeptName() {
            __open_tree(this.id, 1, '派发树', function (selectedNodes) {
                var deptInf = getDeptInfo(selectedNodes);
                $('#deptIds').val(deptInf[0]);
                $('#deptNames').val(deptInf[1]);
            });
        }
        function getDeptInfo(selectedNodes) {
            var strDepName = "";
            var strDepId = "";
            for (var x = 0; x < selectedNodes.length; x++) {
                strDepName += selectedNodes[x].fullName + ","
                if (selectedNodes[x].type == 1)
                    strDepId += selectedNodes[x].code + ",";
                else
                    strDepId += selectedNodes[x].id + ":MEMBER,";
            }
            strDepName = strDepName.substring(0, strDepName.length - 1);
            strDepId = strDepId.substring(0, strDepId.length - 1);
            var deptInf = new Array();
            deptInf[0] = strDepId;
            deptInf[1] = strDepName;
            return deptInf;
        }
        function showReportResult() {
            window.location.href = "<%=path%>/eemCommonController.do?method=queryReportResult&backType=dataQueryAll";
        }

        function modifyReportDate() {
            var frequencyOfReporting = $("[id=tempName] option:selected").attr("lang");
            if (frequencyOfReporting == 2) {
                $("#reportDate").empty();
                $("#reportDate").append('<option value="" selected>-------</option>');
                $("#reportDate").append('<option value="上半年">上半年</option>');
                $("#reportDate").append('<option value="下半年">下半年</option>');
                $("#reportDate").append('<option value="全年">全年</option>');
            } else {
                $("#reportDate").empty();
                $("#reportDate").append('<option value="" selected>-------</option>');
                $("#reportDate").append('<option value="第一季度">第一季度</option>');
                $("#reportDate").append('<option value="第二季度">第二季度</option>');
                $("#reportDate").append('<option value="第三季度">第三季度</option>');
                $("#reportDate").append('<option value="第四季度">第四季度</option>');
                $("#reportDate").append('<option value="全年">全年</option>');
            }
        }
        function   downZipAll(){
            var r = confirm("确定要整体打包下载？");
            if(r){
                var reportData = $("#reportDate").val();
                var reportYear= $("#year").val();
                if(reportData==''||reportYear==''){
                    alert("请选择要打包的粒度");
                    return;
                }
                $.ajax({
                    type: "post",
                    dataType: 'Json',
//                    data: {
//                        reportDate: reportDate
//                    },
                    url: "<%=path%>/deductController.do?method=allDownload&reportData=" + reportData + "&reportYear=" + reportYear,
                    async: false,
                    success: function (obj) {
                        if (obj.success) {
                            window.location.href = "<%=path%>/deductController.do?method=download&attachmentid="+obj.attIds+"&name="+decodeURIComponent(decodeURIComponent(reportYear+reportData+'集团扣分公示数据'));
                        }else{
                            alert("没有数据！");
                        }
                    }
                });
                <%--window.location.href = "<%=path%>/deductController.do?method=allDownload&reportYear="+reportYear+"&reportData="+decodeURIComponent(decodeURIComponent(reportData));--%>
            }
        }
        <%--function showReportData(objectId) {--%>
            <%--window.location.href = "<%=path%>/deductController.do?method=showReportData&attachmentid=" + objectId ;--%>
        <%--}--%>

        <%--function downReportData(objectId) {--%>
            <%--window.location.href = "<%=path%>/deductController.do?method=allDownload&attachmentid=" + objectId;--%>
        <%--}--%>
    </script>
</head>
<body>
<div class="big-title-metar">集团扣分公示版下载</div>
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px">上报周期：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <select id="year" style="width: 45%">
                <option value="" selected>-------</option>
                <c:forEach begin="2015" end="${yearStr}" var="y" step="1">
                    <c:if test="${y eq yearStr}" var="ss">
                        <option value="${y}" selected="selected">${y}</option>
                    </c:if>
                    <c:if test="${!ss}">
                        <option value="${y}">${y}</option>
                    </c:if>
                </c:forEach>
            </select>
            <select id="reportDate" style="width: 45%">
                <option value="" selected>-------</option>
                <%--<option value="第一季度">第一季度</option>--%>
                <%--<option value="第二季度">第二季度</option>--%>
                <%--<option value="第三季度">第三季度</option>--%>
                <%--<option value="第四季度">第四季度</option>--%>
                <option value="上半年">上半年</option>
                <option value="下半年">下半年</option>
                <%--<option value="全年">全年</option>--%>
            </select>
        </div>
        <div class="btn btn-danger" style="margin-left: 15px;" id="custom_search_2_2_3">查询</div>
    <div class="btn btn-danger" style="float: right" onclick="downZipAll()">打包下载</div>

</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
