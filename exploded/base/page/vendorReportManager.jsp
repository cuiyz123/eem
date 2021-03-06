<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage2.jsp" %>


<html>
<head>
    <title>报表与厂商关系维护</title>
    <%
        String venders = (String) request.getAttribute("venders");
        String type = (String) request.getAttribute("type");
    %>
    <style type="text/css">
        .div0{
            height: 100px;

        }
        /*.div1{*/
            /*background-color: #f0f0ee;*/
        /*}*/
        /*.div2{*/
            /*background-color: #ffc6c7;*/
        /*}*/

    </style>
    <script type="text/javascript">
        __show_metar_loading();
        var dtGridColumns_2_1_2 = [
            {
                id: 'vendor_name',
                title: '厂家名字',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: 'width:80px',
                columnStyle:'width:80px'
            }
        ]

        var sb='<%=venders%>';
        var venders=sb.split(","); //字符分割

        for (i=0;i<venders.length;i++ )
        {
            var obj ={
                id: '',
                title: '',
                type: 'string',
                columnClass: 'text-center',
                headerStyle: 'width:80px',
                columnStyle:'width:80px'
            }
            obj.id='a'+i;
            obj.title=venders[i];
            dtGridColumns_2_1_2.push(obj);
        }

        var dtGridOption_2_1_2 = {
            lang : 'zh-cn',
            ajaxLoad : true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryVengerList&type=<%=type%>',
            exportFileName : '数据查询列表',
            columns : dtGridColumns_2_1_2,
            gridContainer : 'dtGridContainer_2_1_2',
            pageSize : 1000
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function(){
                __hide_metar_loading();
            });
            __hide_metar_loading();

            //绑定方法
            $('#custom_search_2_2_3').click(customSearch_2_2_3);
            $("#addReport").click(addReport);

        });
        function customSearch_2_2_3(){
            location.href= "eemTemplateController.do?method=initAdd&type="+$('#tempType').val();
//            grid_2_1_2.parameters = new Object();
//            var reportDate = $('#reportDate').val();
//            var year = $('#year').val()
//            if(!reportDate||!year){
//                alert("请选择统计粒度");
//                return ;
//            }
//            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
//            grid_2_1_2.parameters['year'] = $('#year').val();
//            grid_2_1_2.reload()
//            grid_2_1_2.refresh(true);
        }
        function addReport(){
            $("#report_add")[0].reset();
            $("#objectId").val("sheetName");
            __open_metar_window("addReport", "添加报表", 500, 340, function(__window_body){
                __window_body.append($("#report_add"));
                $("#report_add").show();
                var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">保存</span>');
                var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
                __applyEitorLink_btn_submit.click(function () {
                    if(submitForm()){
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
        function submitForm(){
            var venderName = $("#venderName").val();
            var shortName = $("#shortName").val();
            if(venderName==''|| shortName==''){
                alert("关联厂家或报表简称不能为空，请填写");
                return false;
            }
            var sheetName=$("[name=tpInputID] option:selected").attr("sheetName");
            $("#query_sheetName").val(sheetName);
            $.ajax({
                type:'post',
                url:"<%=path%>/eemTemplateController.do?method=saveReport",
                data: $('#report_add').serialize(),
                success:function(data){
                    var object = $.parseJSON(data);
                    if(object.success){
                        alert("保存成功");
                        grid_2_1_2.load(function(){
                            __hide_metar_loading();
                        });
                    }else{
                        alert(object.msg);
                        $("#sheetName").val('');
                    }
                },
                error:function(XmlHttpRequest,textStatus,errorThrown){
                    alert("保存失败");
                }
            });
            return true;
        }
        function showTimelyRate(name,code){
            window.location.href = "<%=path%>/eemCommonController.do?method=initTimelyRate&provinceName="+name+"&provinceCode=" + code+"&reportDate="+$("#reportDate").val();
        }
        function showAccuracyRate(name,code){
            window.location.href = "<%=path%>/eemCommonController.do?method=initAccuracyRate&provinceName="+name+"&provinceCode=" + code+"&reportDate="+$("#reportDate").val();
        }
    </script>
    <style>
        #report_add {
            display: none;
        }
    </style>
</head>
<body>
<div class="big-title-metar">报表与厂商关系维护</div>
<div class="container-fluid">
    <div class="row">
        <%--<div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">统计粒度：</div>
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
                &lt;%&ndash;<option value="第一季度">第一季度</option>&ndash;%&gt;
                &lt;%&ndash;<option value="第二季度">第二季度</option>&ndash;%&gt;
                &lt;%&ndash;<option value="第三季度">第三季度</option>&ndash;%&gt;
                &lt;%&ndash;<option value="第四季度">第四季度</option>&ndash;%&gt;
                <option value="上半年">上半年</option>
                <option value="下半年">下半年</option>
                &lt;%&ndash;<option value="全年">全年</option>&ndash;%&gt;
            </select>
        </div>--%>

        <div class="col-xs-1 col-sm-1" style="padding: 0px">设备类型：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <select id="tempType" style="width: 90%">
                <option value="1">甲类</option>
                <option value="2">乙类</option>
                <option value="3">丙类</option>
            </select>
        </div>
        <div class="btn btn-danger" style="margin-left: 15px;" id="custom_search_2_2_3">查询</div>
        <%
            Object object = request.getSession().getAttribute("globalUniqueUser");
            if(object!=null){
                UserEntity userEntity = (UserEntity)object;
                if("root".equals(userEntity.getUserName())){
        %>
        <div class="btn btn-danger" id="addReport">添加报表</div>
        <%
                }
            }
        %>

        <div class="col-xs-1 col-sm-3" style="float: right;">√表示此模板涵盖该厂商设备</div>
    </div>
</div>
<div0 id="dtGridContainer_2_1_2" class="dt-grid-container" div0></div0>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<form action="#" method="post" id="report_add" enctype="multipart/form-data">
    <table class="table table-form-4">
        <tr>
            <th>报表名称<font color="red">*</font></th>
            <td>
                <input type="hidden" id="query_sheetName" name="sheetName">
                <select name="tpInputID" style="width: 100%" onchange="modifyReportDate()">
                    <c:forEach items="${tempList}" var="temp">
                        <option value="${temp.objectId}"  sheetName="${temp.tempName}">${temp.tempName}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr>
            <th>报表简称<font color="red">*</font></th>
            <td><input type="text" class="form-control" id="shortName" name="shortName"></td>
        </tr>
        <tr>
            <th>报表类型<font color="red">*</font></th>
            <td>
                <select id="type" name="type" style="width: 100%">
                    <option value="甲">甲类</option>
                    <option value="乙">乙类</option>
                    <option value="丙">丙类</option>
                </select>
            </td>
        </tr>
        <tr>
            <th>关联厂家<font color="red">*</font></th>
            <td><input type="text" class="form-control" id="venderName" name="venderName"></td>
        </tr>
    </table>
</form>
<script>
    $("#tempType").empty();
    $("#tempType").append(' <option value="1">甲类</option>');
    if('<%=type%>'=='2'){
        $("#tempType").append(' <option value="2" selected>乙类</option>');
    }else{
        $("#tempType").append(' <option value="2">乙类</option>');
    }
    if('<%=type%>'=='3'){
        $("#tempType").append(' <option value="3" selected>丙类</option>');
    }else{
        $("#tempType").append(' <option value="3">丙类</option>');
    }



</script>
</body>
</html>
