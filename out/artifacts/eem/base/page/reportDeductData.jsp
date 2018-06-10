<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
<head>
    <title>上报数据</title>
    <script type="text/javascript" src="<%=path%>/base/js/jquery.form.js"></script>
    <script type="text/javascript" src="<%=path%>/base/js/ajaxfileupload.js"></script>
    <style type="text/css">
        .clear{
            clear: both;
        }
        .upload{
            filter:alpha(opacity=0);
            -moz-opacity:0;
            opacity:0;
            width: 100%;
            margin-top: -28px;
            height: 100%;
        }
        .tableDiv{
            overflow:auto;
        }
        #notice_show {
            display: none;
        }
    </style>
    <script type="text/javascript">
        __show_metar_loading();
        var grid_2_1_2;
        var grid_2_1_2_cancel;
        function showNotice(objectID){
            $.ajax({
                type: "POST",
                url: _PATH+"/eemNoticeController.do?method=showNotice",
                data: {objectID:objectID},
                async: false,
                dataType: "json",
                success: function (response) {
                    if (response.success) {
                        var data = response.eemNoticeEntity;
                        $("#notice_show tr:eq(0) td:nth-child(2) span").html(data.theme);
                        $("#deptNamesShow").val(data.deptNames);
                        $("#notice_show tr:eq(2) td:nth-child(2) span").html(data.startDate);
                        $("#notice_show tr:eq(3) td:nth-child(2) span").html(data.endDate);
                        if(data.top){
                            $("#notice_show tr:eq(5) td:nth-child(2) span").html("是");
                        }else{
                            $("#notice_show tr:eq(5) td:nth-child(2) span").html("否");
                        }
                        $("#operDescShow").val(data.operDesc);
                        __open_metar_window("showNotice", "通知详情", 800, 410, function(__window_body){
                            __window_body.append($("#notice_show"));
                            $("#notice_show").show();
                            var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                            var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
                            __applyEitorLink_btn_cancel.click(function () {
                                __window_body.parent().modal('hide');
                            });
                            __applyEitorLink_btns.append(__applyEitorLink_btn_cancel);
                            __window_body.parent().append(__applyEitorLink_btns);
                        });
                    } else {
                        __hide_metar_loading();
                        alert(response.msg);
                    }
                },
                error: function (request) {
                    __hide_metar_loading();
                    alert("提交失败");
                }
            });
        }
        var dtGridColumns_2_1_2_cancel = [
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
                    return "<a href='javascript:;' onclick='showReportData("+record.pageid+","+record.attachmentid+")'>查看</a>&nbsp;<a href='javascript:;' onclick='downReportData("+record.attachmentid+")'>下载</a>&nbsp;<a href='javascript:;' onclick='deleteReportData("+record.pageid+")'>删除</a>";
//                    return "<a href='javascript:;' onclick='downReportData(" + value + ")'>下载</a>";
                }
            }
        ];
        var dtGridOption_2_1_2_cancel = {
            lang : 'zh-cn',
            ajaxLoad : true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryDeductList',
            exportFileName : '退回信息列表',
            columns : dtGridColumns_2_1_2_cancel,
            gridContainer : 'dtGridContainer_2_1_2_cancel',
            toolbarContainer : 'dtGridToolBarContainer_2_1_2_cancel',
            tools:'refresh',
            pageSize : 5,
            pageSizeLimit : [5, 10, 20]
        };
        grid_2_1_2_cancel = $.fn.DtGrid.init(dtGridOption_2_1_2_cancel);
        $(function () {
            grid_2_1_2_cancel.load(function(){
                __hide_metar_loading();
            });
            $('#custom_search_2_2_3').click(customSearch_2_2_3);
        });
        function customSearch_2_2_3(){
            grid_2_1_2_cancel.parameters = new Object();
            grid_2_1_2_cancel.parameters['tempId'] = $('#tempName').val();
            grid_2_1_2_cancel.parameters['year'] = $('#year').val();
            grid_2_1_2_cancel.parameters['reportDate'] = $('#reportDate').val();
            grid_2_1_2_cancel.refresh(true);
        }
    </script>
</head>
<body>
<div class="tableDiv">
    <div class="big-title-metar">数据上报</div>
<form id="reportForm" method="post" enctype="multipart/form-data">
    <%--<input type="hidden" name="objectId" id="objectId" value="${report.objectId}"/>--%>
    <input type="hidden" name="summaryId" id="summary_Id" value="81">
    <table class="table table-form-6">
        <tr>
            <th>资源表格名称<font color=red>*</font></th>
            <td colspan="2">
                <input type="hidden" id="query_sheetName" name="sheetName">
                <select name="tpInputID" style="width: 100%" onchange="modifyReportDate()">
                    <c:forEach items="${tempList}" var="temp">
                        <option value="${temp.objectId}" lang="${temp.reportedFrequency}" summaryId="${temp.applyId}" sheetName="${temp.attribute1}">${temp.tempName}</option>
                    </c:forEach>
                </select>
            </td>
            <th>上报时间 </th>
            <td>
                <select name="reportYear" id="query_reportYear" style="width: 100%">
                    <c:forEach begin="2015" end="${yearStr}" var="y" step="1">
                        <c:if test="${y eq yearStr}"  var="ss">
                            <option  value="${y}" selected="selected" >${y}</option>
                        </c:if>
                        <c:if test="${!ss}">
                            <option  value="${y}">${y}</option>
                        </c:if>

                    </c:forEach>
                </select>
            </td>
            <td>
                <select id="query_reportDate" name="reportDate" style="width: 100%">
                    <%--<option value="第一季度">第一季度</option>--%>
                    <%--<option value="第二季度">第二季度</option>--%>
                    <%--<option value="第三季度">第三季度</option>--%>
                    <%--<option value="第四季度">第四季度</option>--%>
                    <option value="上半年">上半年</option>
                    <option value="下半年">下半年</option>
                    <%--<option value="全年">全年</option>--%>
                </select>
            </td>
        </tr>
        <tr>
            <%--<td> <button type="button" class="btn btn-glow" onclick="importDate()">导入</button></td>--%>
            <th>上报人</th>
            <td>
                <input class="form-control" name="operUserTrueName" value="${report.operUserTrueName}"  readonly/>
            </td>
            <th>上报人部门</th>
            <td>
                 <input class="form-control" name="operOrgName" value="${report.operOrgName}"  readonly/>
            </td>
            <th>上报人电话<font color=red>*</font></th>
            <td>
                 <input class="form-control __metar_check_form" name="operUserPhone" value="${report.operUserPhone}" />
            </td>
        </tr>
        <tr>
            <th>选择数据文件<font color=red>*</font></th>
            <td colspan="5">
                <input type="text" class="form-control" placeholder="点击选择数据文件" id="excelPageName" name="excelPageName"/>
                <div class="clear"></div>
                <input type="file" name="excelData" multiple="true" id="excelData" class="upload" onchange="excelPageName.value = this.value" />
                <%--<td><input type="file" multiple="true" style="width: 100%" id="uploadFiles2" name="pageTemplate"></td>--%>
            </td>
        </tr>
    </table>
        <div align="center">
            <button type="button" class="btn btn-danger" onclick="reportInfo()">上报</button>
            <button type="reset" class="btn btn-default">重置</button>
        </div>
    <input type="hidden" id="withdraw" name="withdraw">
</form>
    <div id="resultDiv"></div>
</div>
<table class="table table-form-4" id="notice_show">
    <tr>
        <th>主题<font color="red">*</font></th>
        <td><span></span></td>
    </tr>
    <tr>
        <th>通知单位<font color="red">*</font></th>
        <td><textarea class="form-control" rows="3" id="deptNamesShow" readonly></textarea></td>
    </tr>
    <tr>
        <th>通知开始时间<font color="red">*</font></th>
        <td><span></span></td>
    </tr>
    <tr>
        <th>通知结束时间<font color="red">*</font></th>
        <td><span></span></td>
    </tr>
    <tr>
        <th>内容<font color="red">*</font></th>
        <td><textarea class="form-control" rows="3" id="operDescShow" readonly></textarea></td>
    </tr>
    <tr>
        <th>是否置顶</th>
        <td><span></span></td>
    </tr>
</table>
<div class="big-title-metar">记录查询</div>
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">模板名称：</div>
        <div class="col-xs-4 col-sm-4" style="padding: 0px">
            <select id="tempName" style="width: 90%">
                <option value="" selected>-------------------------------</option>
                <c:forEach items="${tempList}" var="temp">
                    <option value="${temp.objectId}">${temp.shortName}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">粒度：</div>
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
                <%--<option value="" selected>-------</option>--%>
                <%--<option value="第一季度">第一季度</option>--%>
                <%--<option value="第二季度">第二季度</option>--%>
                <%--<option value="第三季度">第三季度</option>--%>
                <%--<option value="第四季度">第四季度</option>--%>
                <option value="上半年">上半年</option>
                <option value="下半年">下半年</option>
                <%--<option value="全年">全年</option>--%>
            </select>
        </div>
        <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>
    </div>
<div id="dtGridContainer_2_1_2_cancel" class="dt-grid-container"></div>
<div id="dtGridToolBarContainer_2_1_2_cancel" class="dt-grid-toolbar-container"></div>


</body>
<script type="text/javascript">
    $(document).ready(function(){
        modifyReportDate();
    });
    function reportInfo() {
        if(!__metar_check_form(document.getElementById("reportForm"))){
            return;
        }
        var fileData = $("#excelData").val();
        if(""==fileData){
            alert("请选择要上报的EXCEL文件");
            return;
        }
        var arrs = new Array(); //定义一数组
        arrs = fileData.split('.');
        var suffix = arrs [arrs .length - 1];
        if (suffix != 'xls'&&suffix!='xlsx') {
            alert("你选择的模板文件不是EXCEL，请选择EXCEL");
            var obj = document.getElementById('excelData');
            obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
            return false;
        }
 /*   function importDate(){
        var fileData = $("#excelData").val();
        if(""==fileData){
            alert("请选择要导入的EXCEL文件");
            return;
        }*/
        __show_metar_loading();
        $("#reportForm").ajaxSubmit({
            type:'post',
            url:"<%=path%>/deductController.do?method=checkIsReportedSamePage",
            dataType: 'Json',
            success:function(data){
                if(data.msg=='ok'){
                    $("#reportForm").ajaxSubmit({
                        type:'post',
                        url:"<%=path%>/deductController.do?method=importReportData",
                        dataType: 'Json',
                        success:function(data){
                            if(data.success){
                                alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据上报成功！");
                                $("#reportForm")[0].reset();
                            }else{
                                alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据上报失败！"+data.msg);
                            }
                            __hide_metar_loading();
                        },
                        error:function(XmlHttpRequest,textStatus,errorThrown){
                            alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据上报失败！"+data.msg);
                            __hide_metar_loading();
                        }
                    });
                }else if(data.msg=="hasReportedShouldOverride"){
                   if(confirm($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，已经上报过该资源表格的数据是否覆盖？") ){
                       $("#reportForm").ajaxSubmit({
                           type:'post',
                           url:"<%=path%>/deductController.do?method=importReportData",
                           dataType: 'Json',
                           success:function(data){
                               if(data.success){
                                   alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据覆盖上报成功！");
                                   $("#reportForm")[0].reset();
                               }else{
                                   alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据覆盖上报失败！"+data.msg);
                               }
                               __hide_metar_loading();
                           },
                           error:function(XmlHttpRequest,textStatus,errorThrown){
                               alert("重新上报失败");
                               __hide_metar_loading();
                           }
                       });
                   }else{
                       __hide_metar_loading();
                   }
                }else if(data.msg=="withdraw"){
                    alert("退回后重新上报");
                    $("#withdraw").val("yes");
                    $("#reportForm").ajaxSubmit({
                        type:'post',
                        url:"<%=path%>/deductController.do?method=importReportData",
                        dataType: 'Json',
                        success:function(data){
                            if(data.success){
                                alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据重新上报成功！");
                                $("#reportForm")[0].reset();
                            }else{
                                alert($("#query_reportYear").val()+"年"+$("#query_reportDate").val()+"，"+$("[name=tpInputID] option:selected").text()+"，模板数据重新上报失败！"+data.msg);
                            }
                            __hide_metar_loading();
                        },
                        error:function(XmlHttpRequest,textStatus,errorThrown){
                            alert("重新上报失败");
                            __hide_metar_loading();
                        }
                    });
                }else if(data.msg="reportDateError"){
                    alert("不在上报时间范围内，不能上报数据！(一季度：4月1日至4月20日，二季度：7月1日至7月20日，三季度：10月1日至10月20日，四季度：1月1日至1月20日)");
                    __hide_metar_loading();
                }
            },
            error:function(XmlHttpRequest,textStatus,errorThrown){
                alert("保存失败");
                __hide_metar_loading();
            }
        });
    }

    function downTemplate(id){
        window.location.href = "eemTemplateController.do?method=downFile&objectID=" + id;
    }

    function showReportData(pageid,objectId) {
        window.location.href = "<%=path%>/deductController.do?method=showReportData&attachmentid=" + objectId+"&pageid="+pageid ;
    }

    function deleteReportData(pageid,objectId) {
        var r = confirm("是否确实删除？")
        if(r){
            $.ajax({
                type: "get",
                url: _PATH+"/deductController.do?method=deleteReportData&pageid="+pageid ,
//            data: $('#notice_add').serialize(),
                async: false,
                dataType: "json",
                success: function (response) {
                    if (response.flag) {
                        customSearch_2_2_3();
                    } else {
                        alert("删除失败");
                    }
                },
                error: function (request) {
                    __hide_metar_loading();
                    alert("删除失败");
                }
            });
        }

        <%--window.location.href = "<%=path%>/deductController.do?method=deleteReportData&pageid="+pageid ;--%>
    }

    function downReportData(objectId) {
        window.location.href = "<%=path%>/deductController.do?method=download&attachmentid=" + objectId;
    }
    function modifyReportDate(){
        $("#summary_Id").val($("[name=tpInputID] option:selected").attr("summaryId"));
        var frequencyOfReporting=$("[name=tpInputID] option:selected").attr("lang");
        var sheetName=$("[name=tpInputID] option:selected").attr("sheetName");
        $("#query_sheetName").val(sheetName);
        if(frequencyOfReporting==2){
            $("#query_reportDate").empty();
            $("#query_reportDate").append('<option value="上半年">上半年</option>');
            $("#query_reportDate").append('<option value="下半年">下半年</option>');
//            $("#query_reportDate").append('<option value="全年">全年</option>');
        }else{
            $("#query_reportDate").empty();
            $("#query_reportDate").append('<option value="第一季度">第一季度</option>');
            $("#query_reportDate").append('<option value="第二季度">第二季度</option>');
            $("#query_reportDate").append('<option value="第三季度">第三季度</option>');
            $("#query_reportDate").append('<option value="第四季度">第四季度</option>');
            $("#query_reportDate").append('<option value="全年">全年</option>');
        }
    }

</script>
</html>
