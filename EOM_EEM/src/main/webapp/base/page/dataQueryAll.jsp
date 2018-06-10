<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <%
        String yearStr = (String)request.getAttribute("yearStr");
        if(yearStr == null){
            yearStr = request.getParameter("yearStr");
        }
    %>
    <c:set scope="request" value="<%=yearStr%>" var="yearStr"/>
    <title>公布版数据下载</title>
    <style type="text/css">
        .div0{
            height: 200px;
        }
    </style>
    <script type="text/javascript">

       // __show_metar_loading();
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'tpInputName',
                title: '文件名称',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='downReportData(" + record.objectId + ")'>"+record.operOrgName+"_"+record.objectId+"_"+value+"_"+record.reportDate+"</a>";
                    return ;
                }
            },
            {
                id: 'operUserTrueName',
                title: '上报人',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
            },
            {
                id: 'operOrgName',
                title: '上报人部门',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'operUserPhone',
                title: '电话',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
            },
            {
                id: 'dateGrading',
                title: '统计粒度',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'creationTime',
                title: '上报时间',
                type: 'date',
                format: 'yyyy-MM-dd hh:mm:ss',
                columnClass: 'text-center',
                headerStyle: '15%',
                columnStyle:'15%'
//                headerStyle: 'width:160px'
            },
            {
                id: 'objectId',
                title: '操作',
                type: 'string',
//                headerStyle: 'width:100px',
                headerStyle: '5%',
                columnStyle:'5%',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='showReportData(" + value + ")'>查看</a>";
                }
            }
        ];
        var dtGridOption_2_1_2 = {
            lang: 'zh-cn',
            ajaxLoad: true,
            loadURL: '<%=path%>/eemQueryController.do?method=queryDataListAll&tempIds='+$('#tempIds').val(),
            exportFileName: '数据查询列表',
            columns: dtGridColumns_2_1_2,
            gridContainer: 'dtGridContainer_2_1_2',
            toolbarContainer: 'dtGridToolBarContainer_2_1_2',
            tools: 'refresh',
            pageSize: 10,
            pageSizeLimit : [10,20,50,100]
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            searchProData();
            grid_2_1_2.load(function () {
                __hide_metar_loading();
            });
            $('#collect_2_2_1').click(searchCityData);
            $('#collect_2_2_2').click(searchProData);
        });
        //    查询地市上报数据
        function searchCityData() {
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['tempId'] = $('#tempName').val();
            grid_2_1_2.parameters['year'] = $('#year').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
            grid_2_1_2.parameters['deptIds'] = $("#deptIds").val();
            grid_2_1_2.parameters['type'] = 'city';
            grid_2_1_2.refresh(true);
        }
        //查询省分上报数据
        function searchProData() {
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['tempId'] = $('#tempName').val();
            grid_2_1_2.parameters['year'] = $('#year').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
            grid_2_1_2.parameters['deptIds'] = $("#deptIds").val();
            grid_2_1_2.parameters['type'] = 'pro';
            grid_2_1_2.refresh(true);
        }
        function showReportData(objectId) {
            window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId + "&type=dataQueryAll";
        }

        function downReportData(objectId) {
            window.location.href = "<%=path%>/reportController.do?method=downReportData&objectId=" + objectId;
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

        //数据打包下载的方法
        function dataZipAll(){
//            var r = confirm("是否下载查询出的所有模板数据");
//            if(r){
                var tempName = $("#tempName").find("option:selected").text();
                var reportData = $("#reportDate").val();
                var reportYear= $("#year").val();
                var proNames = $("#deptIds").val();

                 if(tempName.startsWith("--")){
                 alert("请选择要打包的模板");
                 return;
                 }
                if(reportYear==''){
                    alert("请选择要打包的时间段");
                    return;
                }
                if(reportData==''){
                    alert("请选择要打包的粒度");
                    return;
                }
                /*
                 if(proNames == ""){
                 alert("请选择上报省份");
                 return;
                 }
                 */
                // window.location.href = "<%=path%>/eemQueryController.do?method=downAllProReportExcel&reportYear="+reportYear+"&reportData="+encodeURIComponent(encodeURIComponent(reportData))+"&tempId="+$('#tempName').val()+"&reportOrgCode="+proNames;
                window.location.href = "<%=path%>/eemQueryController.do?method=downAllProReportExcel&reportYear="+reportYear+"&tempId="+$('#tempName').val()+"&reportData="+encodeURIComponent(encodeURIComponent(reportData));

//            }
        }

        //整体打包下载方法
        function overAllZip(){
            var r = confirm("确定要整体打包下载数据？");
            if(r){
                var reportData = $("#reportDate").val();
                var reportYear= $("#year").val();
                if(reportYear==''){
                    alert("请选择要打包的时间段");
                    return;
                }
                if(reportData==''){
                    alert("请选择要打包的粒度");
                    return;
                }
                window.location.href = "<%=path%>/eemQueryController.do?method=overAllZipReportExcel&reportData=" + encodeURIComponent(encodeURIComponent(reportData)) + "&reportYear=" + reportYear;
            }
        }


    </script>
</head>
<style type="text/css">
    .div0{
        height: 400px!important;
    }
</style>
<body>
<div class="big-title-metar">公布版数据下载</div>
<input type="hidden" id="tempIds" value="${tempIds}">
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px">模板名称：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <select id="tempName" style="width: 100%" onchange="modifyReportDate()">
                <option value="" selected>-------------------------------</option>
                <c:forEach items="${tempList}" var="temp">
                    <option value="${temp.objectId}" lang="${temp.reportedFrequency}">${temp.tempName}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 10px">粒度：</div>
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
        <%
            Object object = request.getSession().getAttribute("globalUniqueUser");
            if (object != null) {
                UserEntity userEntity = (UserEntity) object;
        %>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">上报省分：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <%
                if ("UNI".equals(userEntity.getCategory())) {
            %>
            <input type="text" style="width: 90%" name="deptNames" id="deptNames" onclick="getDeptName()"/>
            <%
            } else {
            %>
            <span>${proType}</span>
            <%
                }%>

            <input type="hidden" name="deptIds" id="deptIds"/>
        </div>
        <%--<div class="btn btn-danger" id="custom_search_2_2_3">查询</div>--%>

        <%
            if ("UNI".equals(userEntity.getCategory())) {
        %>
        <div class="col-xs-1 col-sm-1 btn btn-danger" style="width: 120px" id="collect_2_2_2">查询省分上报数据</div>
        <div class="col-xs-1 col-sm-1 " style="width: 120px"><a href="javascript:;" onclick="showReportResult()">上报结果查看</a></div>

        <%
        } else if ("PRO".equals(userEntity.getCategory())) {
        %>
        <div class="col-xs-1 col-sm-1 "><a href="javascript:;" onclick="showReportResult()">上报结果查看</a></div>
        <div style="text-align: right;margin-right:100px">
            <div class="btn btn-danger" id="collect_2_2_1">查询地市上报数据</div>
            <div class="btn btn-danger" id="collect_2_2_2">查询本省上报数据</div>
        </div>
        <%
        } else if ("CITY".equals(userEntity.getCategory())) {
        %>
        <div class="col-xs-1 col-sm-1 btn btn-danger" id="collect_2_2_1">查询</div>
        <div class="col-xs-1 "><a href="javascript:;" onclick="showReportResult()">上报结果查看</a></div>
        <%
                }
            }
        %>
    </div>
    <%
        if (object != null) {
            UserEntity userEntity = (UserEntity) object;
            if ("UNI".equals(userEntity.getCategory())) {
    %>
    <div class="btn btn-danger" style="float: right" onclick="overAllZip()">整体打包下载</div>
    <div class="btn btn-danger" style="float: right" onclick="dataZipAll()">数据打包下载</div>
    <%}}%>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
