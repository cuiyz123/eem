<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>设备类型管理</title>
    <script type="text/javascript" src="<%=path%>/base/js/jquery.form.js"></script>
    <script type="text/javascript">
        __show_metar_loading();
        var grid_2_1_2;
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'devicename',
                title: '设备名称',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
//                headerStyle: 'width:160px'
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='showVendorData("+record.tpinputid+")'>"+value+"</a>";
                }
            },

            {
                id: 'sheetname',
                title: '报表名称',
                type: 'string',
//                headerStyle: 'width:160px',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'

                }

        ];
        var dtGridOption_2_1_2 = {
            lang: 'zh-cn',
            ajaxLoad: true,
            loadURL: '<%=path%>/eemQueryController.do?method=querySheetList',
            exportFileName: '设备类型与填报模板关系',
            columns: dtGridColumns_2_1_2,
            gridContainer: 'dtGridContainer_2_1_2',
            toolbarContainer: 'dtGridToolBarContainer_2_1_2',
            tools: 'refresh',
            pageSize: 10,
            pageSizeLimit: [10, 20, 50,100]
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function () {
                __hide_metar_loading();
            });
                   
            $('#collect_2_2_2').click(queryDeviceList);
            $("#showVendorData").click(showVendorData);


        });



        //查询省分上报数据
        function queryDeviceList() {
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['id'] = $('#deviceNameid').val();
            grid_2_1_2.refresh(true);


        }






        function modifyReportDate() {
            var frequencyOfReporting = $("[id=id] option:selected").attr("lang");
            if (frequencyOfReporting == 2) {
                $("#reportDate").empty();
                $("#reportDate").append('<option value="" selected>-------</option>');
                $("#reportDate").append('<option value="上半年">上半年</option>');
                $("#reportDate").append('<option value="下半年">下半年</option>');
                $("#reportDate").append('<option value="全年">全年</option>');
            } else {
                $("#reportDate").empty();
                $("#reportDate").append('<option value="" selected>-------</option>');
                $("#reportDate").append('<option value="上半年">上半年</option>');
                $("#reportDate").append('<option value="下半年">下半年</option>');
                $("#reportDate").append('<option value="全年">全年</option>');
            }
        }

        function showVendorData(tpInputID){
            //window.location.href = "<%=path%>/eemQueryController.do?method=showVendorData&tpInputID=" + tpInputID;

//            $('#tpInputID').val(tpInputID);
            showWin("厂家列表",tpInputID);
        }
        function showWin(name,tpInputID){
            $("#deviceId_temp").empty();
            $.ajax({
                type: "get",
                url: _PATH+"/eemQueryController.do?method=showVendorData&tpInputID="+ tpInputID,
                async: false,
                dataType: "json",
                success: function (response) {
                    var arrs = response.names.split(",");
                    var st="";
                    for(var i=0;i<arrs.length;i++){
                        st+="<input type='text' value='"+arrs[i]+"'/>";

                    }
                    $("#deviceId_temp").append(st);
                },
                error: function (request) {
                    __hide_metar_loading();
                    alert("加载失败");
                }
            });
            __open_metar_window("showVendorData", name, 480, 300, function(__window_body){

                __window_body.append($("#deviceId"));




                var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">关闭</span>');

                __applyEitorLink_btn_cancel.click(function () {
                    __window_body.parent().modal('hide');
                });
                __applyEitorLink_btns.append(__applyEitorLink_btn_cancel);
                __window_body.parent().append(__applyEitorLink_btns);
                $("#deviceId").show();
            });
        }
    </script>
    <style>
        #deviceId{
            style:"display:none";
        }

    </style>
</head>
<body>
<div class="big-title-metar">设备类型与填报模板关系</div>
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px">设备名称：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <select id="deviceNameid" style="width: 100%" onchange="modifyReportDate()">
                <option value="" selected>-------------------------------</option>
                <c:forEach items="${tempList}" var="temp">
                    <option value="${temp.id}"  deviceName="${temp.id}">${temp.deviceName}</option>
                </c:forEach>
            </select>
        </div>

        <div class="col-xs-1 col-sm-1 btn btn-danger" style="width: 120px" id="collect_2_2_2">查询</div>
        <%
            Object object = request.getSession().getAttribute("globalUniqueUser");
            if (object != null) {
                UserEntity userEntity = (UserEntity) object;
        %>

        <%--<div class="btn btn-danger" id="custom_search_2_2_3">查询</div>--%>

        <%
            if ("UNI".equals(userEntity.getCategory())) {
        %>


        <%
                }
            }
        %>

    </div>
</div>
<div id="deviceId" >

    <div id="deviceId_temp">
    </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
