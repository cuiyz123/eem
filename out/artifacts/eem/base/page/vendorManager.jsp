<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>厂商管理</title>
    <script type="text/javascript" src="<%=path%>/base/js/jquery.form.js"></script>
    <script type="text/javascript">
        __show_metar_loading();
        var grid_2_1_2;
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'vendorCode',
                title: '厂商编号',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: '10%',
                columnStyle:'10%'
            },
            {
                id: 'vendorName',
                title: '厂商名称',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: '20%',
                columnStyle:'20%'
            },
            {
                id: 'shortName',
                title: '英文简称',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: '15%',
                columnStyle:'15%'
            },
            {
                id: 'remark',
                title: '备注',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: '53%',
                columnStyle:'53%'
            }
        ];
        var dtGridOption_2_1_2 = {
            lang : 'zh-cn',
            ajaxLoad : true,
            check : true,
            loadURL: '<%=path%>/eemVendorController.do?method=queryVendorList',
            exportFileName : '厂商管理列表',
            columns : dtGridColumns_2_1_2,
            gridContainer : 'dtGridContainer_2_1_2',
            toolbarContainer : 'dtGridToolBarContainer_2_1_2',
            tools:'refresh',
            pageSize : 10,
            pageSizeLimit : [10, 20, 50,100]
        };
        grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function(){
                __hide_metar_loading();
            });
            //绑定方法
            $('#custom_search_2_2_3').click(customSearch_2_2_3);
            $("#addVendor").click(addVendor);
            $("#updateVendor").click(updateVendor);
            $("#deleteVendor").click(deleteVendor);
            $("#downVendorTemp").click(downVendorTemp);
            $("#importVendor").click(importVendor);
        });
        function customSearch_2_2_3(){
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['vendorCode'] = $('#query_vendorCode').val();
            grid_2_1_2.parameters['vendorName'] = $('#query_vendorName').val();
            grid_2_1_2.parameters['shortName'] = $('#query_shortName').val();
            grid_2_1_2.refresh(true);
        }
        function addVendor(){
            $('#vendor_add')[0].reset();
            $('#objectId').val('');
            showVendorWin("添加厂商");
        }
        function showVendorWin(name){
            if(name=="添加厂商"){
                $.ajax({
                    type: "POST",
                    url: _PATH+"/eemVendorController.do?method=getVendorCode",
                    data: {},
                    async: false,
                    dataType: "json",
                    success: function (response) {
                        if (response.success) {
                            $('#vendorCode').val(response.vendorCode);
                        }
                    },
                    error: function (request) {
                        __hide_metar_loading();
                        alert("获取失败失败");
                    }
                });
            }
            __open_metar_window("addVendor", name, 800, 300, function(__window_body){
                __window_body.append($("#vendor_add"));
                $("#vendor_add").show();
                var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">保存</span>');
                var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
                __applyEitorLink_btn_submit.click(function () {
                    if(saveVendor()){
                        __window_body.parent().modal('hide');
                    }
                });
                __applyEitorLink_btn_cancel.click(function () {
                    __window_body.parent().modal('hide');
                });
                __applyEitorLink_btns.append(__applyEitorLink_btn_submit);
                __applyEitorLink_btns.append("&nbsp;&nbsp;");
                __applyEitorLink_btns.append(__applyEitorLink_btn_cancel);
                __window_body.parent().append(__applyEitorLink_btns);
            });
        }
        function saveVendor(){
            if(!__metar_check_form(document.getElementById("vendor_add"))){
                __hide_metar_loading();
                return false;
            }
            $.ajax({
                type: "POST",
                url: _PATH+"/eemVendorController.do?method=saveVendor",
                data: $('#vendor_add').serialize(),
                async: false,
                dataType: "json",
                success: function (response) {
                    if (response.success) {
                        grid_2_1_2.load(function(){
                            __hide_metar_loading();
                        });
                        $('#vendor_add')[0].reset();
                    } else {
                        __hide_metar_loading();
                        alert(response.msg);
                        return false;
                    }
                },
                error: function (request) {
                    __hide_metar_loading();
                    alert("提交失败");
                }
            });
            return true;
        }
        function updateVendor(){
            var records = grid_2_1_2.getCheckedRecords();
            if(records.length==0){
                alert("请选择要编辑的厂商");
                return;
            }else if(records.length>1){
                alert("同时只能编辑一条厂商数据");
                return;
            }else{
                var data = records[0];
                $("#objectId").val(data.objectId)
                $("#vendorCode").val(data.vendorCode)
                $("#vendorName").val(data.vendorName)
                $("#shortName").val(data.shortName)
                $("#shortName1").val(data.shortName1)
                $("#category").val(data.category)
                $("#remark").val(data.remark);
                showVendorWin("编辑厂商");
            }
        }
        function deleteVendor(){
            var records = grid_2_1_2.getCheckedRecords();
            var ids = "";
            if(records.length==0){
                alert("请选择要删除的厂商");
                return;
            }else{
                var r = confirm('您一共选择了 '+records.length+' 条数据，确定要删除吗？');
                if(r){
                    for(var record in records){
                        ids+=records[record].objectId+",";
                    }
                    if(ids!=""){
                        ids = ids.substr(0,ids.length-1);
                    }
                    $.ajax({
                        type: "POST",
                        url: _PATH+"/eemVendorController.do?method=deleteVendor",
                        data: {objectIDs:ids},
                        async: false,
                        dataType: "json",
                        success: function (response) {
                            if (response.success) {
                                grid_2_1_2.load(function(){
                                    __hide_metar_loading();
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
            }
        }
        function downVendorTemp(){
            window.location = _PATH+"/eemVendorController.do?method=downVendorTemp";
        }
        function importVendor(){
            __open_metar_window("importVendor", "导入厂商", 600, 150, function(__window_body){
                __window_body.append($("#vendor_import"));
                $("#vendor_import").show();
                var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                var __applyEitorLink_btn_import = $('<span class="btn btn-danger" style="margin-top: 5px">导入</span>');
                __applyEitorLink_btn_import.click(function () {
                    var arrs = new Array(); //定义一数组
                    var temps= $("#uploadFiles").val();
                    if(temps==''){
                        alert("模板文件不能为空，请选择");
                        return false;
                    }
                    arrs = temps.split('.');
                    var suffix = arrs [arrs .length - 1];
                    if (suffix != 'xls'&&suffix!='xlsx') {
                        alert("你选择的文件不是EXCEL，请选择EXCEL");
                        var obj = document.getElementById('uploadFiles');
                        obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
                        return false;
                    }
                    $("#vendor_import").ajaxSubmit({
                        type:'post',
                        url:"<%=path%>/eemVendorController.do?method=uploadFile",
                        dataType: 'Json',
                        success:function(data){
                            if(data.success){
                                var obj = document.getElementById('uploadFiles');
                                obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
                                grid_2_1_2.load(function(){
                                    __hide_metar_loading();
                                });
                                __window_body.parent().modal('hide');
                            }else{
                                alert(data.msg);
                            }
                        },
                        error:function(XmlHttpRequest,textStatus,errorThrown){
                            alert("保存失败");
                        }
                    });
                });
                __applyEitorLink_btns.append(__applyEitorLink_btn_import);
                __window_body.parent().append(__applyEitorLink_btns);
            });
        }
        function limitLength(name,limit){
            var value = $("#"+name).val();
            if(value.length>limit){
                $("#"+name).val(value.substring(0, limit));
            }
        }
    </script>
    <style>
        #vendor_add{
            display: none;
        }
        #vendor_import{
            display: none;
        }
    </style>
</head>
<body>
<div class="big-title-metar">厂商管理</div>
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">厂商编号：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="query_vendorCode"></div>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">厂商名称：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="query_vendorName"></div>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">英文简称：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="query_shortName"></div>
        <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>
    </div>
    <%
        Object object = request.getSession().getAttribute("globalUniqueUser");
        if(object!=null){
            UserEntity userEntity = (UserEntity)object;
            if(("UNI".equals(userEntity.getCategory())&&userEntity.getAdmin())||"root".equals(userEntity.getUserName())){
    %>
    <div class="row">
        <div class="btn btn-danger" id="addVendor">新增</div>
        <div class="btn btn-danger" id="updateVendor">编辑</div>
        <div class="btn btn-danger" id="deleteVendor">删除</div>
        <div class="btn btn-danger" id="downVendorTemp">导入模板下载</div>
        <div class="btn btn-danger" id="importVendor">批量导入</div>
    </div>
    <%
            }
        }
    %>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<form action="#" method="post" id="vendor_add">
    <table class="table table-form-4">
        <tr>
            <th>厂商编号<font color="red">*</font></th>
            <td><input type="text" class="form-control __metar_check_form" id="vendorCode" name="vendorCode" readonly><input type="hidden" id="objectId" name="objectId"></td>
        </tr>
        <tr>
            <th>厂商名称<font color="red">*</font></th>
            <td><input type="text" class="form-control __metar_check_form" id="vendorName" name="vendorName"></td>
        </tr>
        <tr>
            <th>英文简称<font color="red">*</font></th>
            <td><input type="text" class="form-control __metar_check_form" id="shortName" name="shortName"></td>
        </tr>
        <tr>
            <th>中文简称<font color="red">*</font></th>
            <td><input type="text" class="form-control __metar_check_form" id="shortName1" name="shortName1"></td>
        </tr>
        <tr>
            <th>设备类型<font color="red">*</font></th>
            <td><input type="text" class="form-control __metar_check_form" id="category" name="category"></td>
        </tr>
        <tr>
            <th>备注</th>
            <td><textarea class="form-control" rows="3" id="remark" name="remark" onkeyup="limitLength(this.name,100)"></textarea></td>
        </tr>
    </table>
</form>
<form action="#" method="post" id="vendor_import" enctype="multipart/form-data">
    <table class="table">
        <tr>
            <th style="width:20%;">文件<font color="red">*</font></th>
            <td><input type="file" multiple="true" style="width: 100%" id="uploadFiles" name="uploadFiles"></td>
        </tr>
    </table>
</form>
</body>
</html>
