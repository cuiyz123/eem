<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
  <title>模板管理</title>
  <script type="text/javascript" src="<%=path%>/base/js/jquery.form.js"></script>
  <script type="text/javascript">
    __show_metar_loading();
    var grid_2_1_2;
    //映射内容
    var dtGridColumns_2_1_2 = [
      {
        id: 'tempName',
        title: '模板名称',
        type: 'string',
          headerStyle: '25%',
          columnStyle:'25%',
        columnClass: 'text-center'
      },
      {
        id: 'tempType',
        title: '模板类型',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(value==1){
            return "填报模板";
          }else if(value==2){
            return "汇总模板";
          }else if(value==3){
              return "扣分模板";
          }else{
            return "";
          }
        }
      },
      {
        id: 'creationTime',
        title: '创建时间',
        type: 'date',
        format:'yyyy-MM-dd hh:mm:ss',
        columnClass: 'text-center' ,
          headerStyle: '15%',
          columnStyle:'15%'
      },
      {
        id: 'editHtml',
        title: '操作',
        type: 'string',
          headerStyle: '15%',
          columnStyle:'15%',
        columnClass: 'text-center'
      }
    ];
    var dtGridOption_2_1_2 = {
      lang : 'zh-cn',
      ajaxLoad : true,
      loadURL: '<%=path%>/eemTemplateController.do?method=queryTemplateList',
      exportFileName : '模板管理列表',
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
      $("#addTemp").click(addTemp);
      $("#downLoadAllTemps").click(downLoadAllTemps);
      $("#downLoadDescription").click(downLoadDescription);
    });
    function customSearch_2_2_3(){
      grid_2_1_2.parameters = new Object();
      grid_2_1_2.parameters['tempName'] = $('#tempName').val();
      grid_2_1_2.parameters['tempType'] = $('#tempType').val();
      grid_2_1_2.parameters['tempCreateTime'] = $('#tempCreateTime').val();
      grid_2_1_2.refresh(true);
    }
    function downLoadDescription(){
        window.location='<%=path%>/eemTemplateController.do?method=downLoadDescription';
    }
    function addTemp(){
      $("#temp_add")[0].reset();
      $("#objectId").val("");
      __open_metar_window("addTemp", "添加模板", 500, 340, function(__window_body){
        __window_body.append($("#temp_add"));
        $("#temp_add").show();
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
    function updateTemp(objectID,tempName,shortName,tempType,reportedFrequency,tempPattern,relID){
      $("#temp_update")[0].reset();
      $("#objectId_update").val(objectID);
      $("#temp_Name_update").val(tempName);
      $("#shortName_update").val(shortName);
      $("#tempType_update").attr("value",tempType);
      $("#reportedFrequency_update").attr("value",reportedFrequency);
      $("#tempPattern_update").attr("value",tempPattern);
      if(tempType==2){
        $("#rel_temp_update").hide();
      }else{
        $("#rel_temp_update").show();
      }
      __open_metar_window("updateTemp", "修改模板", 500, 330, function(__window_body){
        __window_body.append($("#temp_update"));
        $("#temp_update").show();
        var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
        var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">保存</span>');
        var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
        __applyEitorLink_btn_submit.click(function () {
          if(updateForm()){
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
    function downLoadAllTemps(){
        debugger;
      var type=$("#tempType").val();
        if(type==""){
            alert("请选择模板类型！");
        }
      window.location='<%=path%>/eemTemplateController.do?method=downloadZip&type='+type;
    }
    function downloadTemp(objectID){
      window.location='<%=path%>/eemTemplateController.do?method=downFile&objectID='+objectID;
    }
    function deleteTemp(value,tempName){
      var r=confirm("你确定要删除模板【"+tempName+"】?");
      if (r==true){
        $.ajax({
          type: "POST",
          url: "<%=path%>/eemTemplateController.do?method=deleteTemp",
          data: {objectID:value},
          async: false,
          success: function (data) {
            grid_2_1_2.load(function(){
              __hide_metar_loading();
            });
          },
          error: function (request) {
            alert("删除失败");
          }
        });
      }
    }
    function submitForm(){
      var tempName = $("#temp_Name").val();
      if(tempName==''){
        alert("模板名称不能为空，请填写");
        return false;
      }
      var arrs = new Array(); //定义一数组
       var temps= $("#uploadFiles").val();
       if(temps==''){
       alert("模板文件不能为空，请选择");
       return false;
       }
       arrs = temps.split('.');
       var suffix = arrs [arrs .length - 1];
       if (suffix != 'xls'&&suffix!='xlsx') {
           alert("你选择的模板文件不是EXCEL，请选择EXCEL");
           var obj = document.getElementById('uploadFiles');
           obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
           return false;
       }
      var arrs2 = new Array(); //定义一数组
       var temps2= $("#uploadFiles2").val();
       if(temps2==''){
          alert("页面模板不能为空，请选择");
          return false;
       }
       arrs2 = temps2.split('.');
       var suffix2 = arrs2 [arrs2 .length - 1];
       if (suffix2 != 'xls'&&suffix2!='xlsx') {
           alert("你选择的页面模板不是EXCEL，请选择EXCEL");
           var obj2 = document.getElementById('uploadFiles2');
           obj2.outerHTML = obj2.outerHTML; //这样清空，在IE8下也能执行成功
           return false;
       }
      $("#temp_add").ajaxSubmit({
        type:'post',
        url:"<%=path%>/eemTemplateController.do?method=uploadFile",
        dataType: 'Json',
        success:function(data){
          if(data.success){
            var obj = document.getElementById('uploadFiles');
            obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
            var obj2 = document.getElementById('uploadFiles2');
            obj2.outerHTML = obj2.outerHTML; //这样清空，在IE8下也能执行成功
            $("#temp_Name").val('');
            grid_2_1_2.load(function(){
              __hide_metar_loading();
            });
          }else{
            alert(data.msg);
            $("#temp_Name").val('');
          }
        },
        error:function(XmlHttpRequest,textStatus,errorThrown){
          alert("保存失败");
        }
      });
      return true;
    }
    function updateForm(){
      var tempName = $("#temp_Name_update").val();
      if(tempName==''){
        alert("模板名称不能为空，请填写");
        return false;
      }
      var arrs = new Array(); //定义一数组
      var temps= $("#uploadFiles_update").val();
      if(temps==''){
       alert("模板文件不能为空，请选择");
       return false;
      }
       arrs = temps.split('.');
       var suffix = arrs [arrs .length - 1];
      if (suffix != 'xls'&&suffix!='xlsx') {
           alert("你选择的模板文件不是EXCEL，请选择EXCEL");
           var obj = document.getElementById('uploadFiles_update');
           obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
           return false;
      }
      var arrs2 = new Array(); //定义一数组
       var temps2= $("#uploadFiles2_update").val();
       if(temps2==''){
          alert("页面模板不能为空，请选择");
          return false;
       }
       arrs2 = temps2.split('.');
       var suffix2 = arrs2 [arrs2 .length - 1];
       if (suffix2 != 'xls'&&suffix2!='xlsx') {
           alert("你选择的页面模板不是EXCEL，请选择EXCEL");
           var obj2 = document.getElementById('uploadFiles2_update');
           obj2.outerHTML = obj2.outerHTML; //这样清空，在IE8下也能执行成功
           return false;
       }
      $("#temp_update").ajaxSubmit({
        type:'post',
        url:"<%=path%>/eemTemplateController.do?method=uploadFile",
        dataType: 'Json',
        success:function(data){
          if(data.success){
            var obj = document.getElementById('uploadFiles_update');
            obj.outerHTML = obj.outerHTML; //这样清空，在IE8下也能执行成功
            var obj2 = document.getElementById('uploadFiles2_update');
            obj2.outerHTML = obj2.outerHTML; //这样清空，在IE8下也能执行成功
            $("#temp_Name_update").val('');
            grid_2_1_2.load(function(){
              __hide_metar_loading();
            });
          }else{
            alert(data.msg);
            $("#temp_Name_update").val('');
          }
        },
        error:function(XmlHttpRequest,textStatus,errorThrown){
          alert("保存失败");
        }
      });
      return true;
    }
    /*function showRelTemp(val){
      if(val==2){
        $("#rel_temp").hide();
      }else{
        $("#rel_temp").show();
      }
    }*/
    function setRel(objectID,tempName,relObjectID){
      $("#objectId_rel").val(objectID);
      $("#temp_Name_rel").val(tempName);
      $("#relID_update").val(relObjectID);
      __open_metar_window("updateTemp_rel", "修改模板关联关系", 500, 180, function(__window_body){
        __window_body.append($("#temp_rel"));
        $("#temp_rel").show();
        var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
        var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">保存</span>');
        var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
        __applyEitorLink_btn_submit.click(function () {
          $("#temp_rel").ajaxSubmit({
            type:'post',
            url:"<%=path%>/eemTemplateController.do?method=updateRel",
            dataType: 'Json',
            success:function(data){
              if(data.success){
                __window_body.parent().modal('hide');
                grid_2_1_2.load(function(){
                  __hide_metar_loading();
                });
              }else{
                alert(data.msg);
              }
            },
            error:function(XmlHttpRequest,textStatus,errorThrown){
              alert("保存失败");
            }
          });
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
  </script>
  <style>
    #temp_add {
      display: none;
    }
    #temp_update{
      display: none;
    }
    #temp_rel{
      display: none;
    }
  </style>
</head>
<body>
<div class="big-title-metar">模板管理</div>
<div class="container-fluid">
  <div class="row">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">模板名称：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="tempName"></div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px">模板类型：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
      <select id="tempType" style="width: 90%">
        <option value="">--------</option>
          <c:if test="${type==1}">
              <option value="1">填报模板</option>
              <option value="2">汇总模板</option>
              <option value="3">扣分模板</option>
          </c:if>
          <c:if test="${type==2}">
              <option value="1">填报模板</option>
              <%--<option value="2">汇总模板</option>--%>
          </c:if>
          <c:if test="${type==3}">
              <option value="1">填报模板</option>
          </c:if>

      </select>
    </div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px">创建时间：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px"><input id="tempCreateTime" type="text" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd'})" readonly></div>
    <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>
    <%
      Object object = request.getSession().getAttribute("globalUniqueUser");
    if(object!=null){
        UserEntity userEntity = (UserEntity)object;
        if("root".equals(userEntity.getUserName())){
          %>
    <div class="btn btn-danger" id="addTemp">添加模板</div>
    <%
        }
    }
    %>
    <div class="btn btn-danger" id="downLoadAllTemps">批量下载模板</div>
      <div class="btn btn-danger" id="downLoadDescription">后评价系统说明书</div>
  </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<form action="#" method="post" id="temp_add" enctype="multipart/form-data">
  <table class="table table-form-4">
    <tr>
      <th>模板名称<font color="red">*</font></th>
      <td><input type="text" class="form-control __metar_check_form" id="temp_Name" name="tempName"><input type="hidden" id="objectId" name="objectId"></td>
    </tr>
    <tr>
      <th>模板简称</th>
      <td><input type="text" class="form-control" id="shortName" name="shortName"></td>
    </tr>
    <tr>
      <th>模板类型<font color="red">*</font></th>
      <td>
        <select id="tempTypeAdd" name="tempType" style="width: 100%">
          <option value="1">填报模板</option>
          <option value="2">汇总模板</option>
            <option value="3">扣分模板</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>上报频率</th>
      <td>
        <select id="reportedFrequency" name="reportedFrequency" style="width: 100%">
          <option value="0">--空--</option>
          <option value="1">季报</option>
          <option value="2">半年</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>模板格式</th>
      <td>
        <select id="tempPattern" name="tempPattern" style="width: 100%">
          <option value="0">--空--</option>
          <option value="1">模板中sheet数量固定</option>
          <option value="2">模板中sheet数量不定</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>模板文件<font color="red">*</font></th>
      <td>
        <input type="file" multiple="true" style="width: 100%" id="uploadFiles" name="dataTemplate">
      </td>
    </tr>
    <tr>
      <th>页面模板<font color="red">*</font></th>
      <td><input type="file" multiple="true" style="width: 100%" id="uploadFiles2" name="pageTemplate"></td>
    </tr>
    <%--<tr id="rel_temp">
      <th>关联的汇总模板</th>
      <td>
        <select id="relID" name="relID" style="width: 100%">
          <c:forEach items="${tempList}" var="temp">
            <option value="${temp.objectId}">${temp.tempName}</option>
          </c:forEach>
      </select>
      </td>
    </tr>--%>
  </table>
</form>
<form action="#" method="post" id="temp_update" enctype="multipart/form-data">
  <table class="table table-form-4">
    <tr>
      <th>模板名称<font color="red">*</font></th>
      <td><input type="text" class="form-control __metar_check_form" id="temp_Name_update" name="tempName"><input type="hidden" id="objectId_update" name="objectId"></td>
    </tr>
    <tr>
      <th>模板简称</th>
      <td><input type="text" class="form-control" id="shortName_update" name="shortName"></td>
    </tr>
    <tr>
      <th>模板类型<font color="red">*</font></th>
      <td>
        <select id="tempType_update" name="tempType" style="width: 100%">
          <option value="1">填报模板</option>
          <option value="2">汇总模板</option>
            <option value="3">扣分模板</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>上报频率</th>
      <td>
        <select id="reportedFrequency_update" name="reportedFrequency" style="width: 100%">
          <option value="0">--空--</option>
          <option value="1">季报</option>
          <option value="2">半年</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>模板格式</th>
      <td>
        <select id="tempPattern_update" name="tempPattern" style="width: 100%">
          <option value="0">--空--</option>
          <option value="1">模板中sheet数量固定</option>
          <option value="2">模板中sheet数量不定</option>
        </select>
      </td>
    </tr>
    <tr>
      <th>模板文件<font color="red">*</font></th>
      <td>
        <input type="file" multiple="true" style="width: 100%" id="uploadFiles_update" name="dataTemplate">
      </td>
    </tr>
    <tr>
      <th>页面模板<font color="red">*</font></th>
      <td><input type="file" multiple="true" style="width: 100%" id="uploadFiles2_update" name="pageTemplate"></td>
    </tr>
    <%--<tr id="rel_temp_update">
      <th>关联的汇总模板</th>
      <td>
        <select id="relID_update" name="relID" style="width: 100%">
          <c:forEach items="${tempList}" var="temp">
            <option value="${temp.objectId}">${temp.tempName}</option>
          </c:forEach>
      </select>
      </td>
    </tr>--%>
  </table>
</form>
<form action="#" method="post" id="temp_rel">
  <table class="table table-form-4">
    <tr>
      <th>模板名称<font color="red">*</font></th>
      <td><input type="text" class="form-control" id="temp_Name_rel" readonly><input type="hidden" id="objectId_rel" name="tempID"></td>
    </tr>
    <tr>
      <th>关联的汇总模板</th>
      <td>
        <select id="relID_update" name="relTempID" style="width: 100%">
          <c:forEach items="${tempList}" var="temp">
            <option value="${temp.objectId}">${temp.tempName}</option>
          </c:forEach>
        </select>
      </td>
    </tr>
  </table>
</form>
</body>
</html>
