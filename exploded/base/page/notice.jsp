<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>后评价系统通知</title>
<script type="text/javascript">
    __show_metar_loading();
    var grid_2_1_2;
    //映射内容
    var dtGridColumns_2_1_2 = [
      {
        id: 'theme',
        title: '主题',
        type: 'string',
          headerStyle: '15%',
          columnStyle:'15%',
        columnClass: 'text-center'
      },
      {
        id: 'oper_user_true_name',
        title: '发布人',
        type: 'string',
        columnClass: 'text-center'
      },
      {
        id: 'oper_full_org_name',
        title: '发布人部门',
        type: 'string',
          headerStyle: '15%',
          columnStyle:'15%',
        columnClass: 'text-center'
      },
      {
        id: 'start_date',
        title: '通知开始时间',
        type: 'date',
        format:'yyyy-MM-dd',
        columnClass: 'text-center' ,
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(value!=''){
            return value.substr(0,10);
          }else{
            return "";
          }
        }
      },
      {
        id: 'end_date',
        title: '通知结束时间',
        type: 'date',
        format:'yyyy-MM-dd',
        columnClass: 'text-center' ,
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(value!=''){
            return value.substr(0,10);
          }else{
            return "";
          }
        }
      },
      {
        id: 'creation_time',
        title: '通知发布时间',
        type: 'string',
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
      loadURL: '<%=path%>/eemNoticeController.do?method=queryNoticeList',
      exportFileName : '公告管理列表',
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
      $("#addNotice").click(addNotice);
    });
    function customSearch_2_2_3(){
      grid_2_1_2.parameters = new Object();
      grid_2_1_2.parameters['noticeTheme'] = $('#noticeTheme').val();
      grid_2_1_2.parameters['noticeCreatePerson'] = $('#noticeCreatePerson').val();
      grid_2_1_2.parameters['noticeCreateDate'] = $('#noticeCreateDate').val();
      grid_2_1_2.refresh(true);
    }
    function addNotice(){
      $('#notice_add')[0].reset();
      $('#objectId').val('');
      showWin("添加通知");
    }
    function showWin(name){
      __open_metar_window("addNotice", name, 900, 550, function(__window_body){
        __window_body.append($("#notice_add"));
        $("#notice_add").show();
        var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
        var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">保存</span>');
        var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
        __applyEitorLink_btn_submit.click(function () {
          if(saveNotice()){
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
    function saveNotice(){
      if(!__metar_check_form(document.getElementById("notice_add"))){
        __hide_metar_loading();
        return false;
      }
      if($("#startDate").val()>=$("#endDate").val()){
        alert("结束时间需要大于开始时间");
        __hide_metar_loading();
        return false;
      }
      $.ajax({
        type: "POST",
        url: _PATH+"/eemNoticeController.do?method=saveNotice",
        data: $('#notice_add').serialize(),
        async: false,
        dataType: "json",
        success: function (response) {
          if (response.success) {
            grid_2_1_2.load(function(){
              __hide_metar_loading();
            });
            $('#notice_add')[0].reset();
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
      return true;
    }
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
            __open_metar_window("showNotice", "通知详情", 900, 550, function(__window_body){
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
          alert("加载失败");
        }
      });
    }
    function updateNotice(objectID){
      $.ajax({
        type: "POST",
        url: _PATH+"/eemNoticeController.do?method=showNotice",
        data: {objectID:objectID},
        async: false,
        dataType: "json",
        success: function (response) {
          if (response.success) {
            var data = response.eemNoticeEntity;
            $("#objectId").val(data.objectId)
            $("#theme").val(data.theme)
            $("#deptCodes").val(data.deptCodes);
            $("#deptNames").val(data.deptNames);
            $("#startDate").val(data.startDate)
            $("#endDate").val(data.endDate)
            $("#operDesc").val(data.operDesc);
            $("#top ").attr("value",data.top);
            showWin("修改通知");
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
    function delNotice(objectID){
      var r = confirm("确定要删除此通知？");
      if(r){
        $.ajax({
          type: "POST",
          url: _PATH+"/eemNoticeController.do?method=deleteNotice",
          data: {objectID:objectID},
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
    function topNotice(objectID){
      $.ajax({
        type: "POST",
        url: _PATH+"/eemNoticeController.do?method=updateTopNotice",
        data: {objectID:objectID},
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
    function limitLength(name,limit){
      var value = $("#"+name).val();
      if(value.length>limit){
        $("#"+name).val(value.substring(0, limit));
      }
    }
    function getDeptName(){
      __open_tree(this.id , 1 , '派发树' ,function(selectedNodes){
        var deptInf = getDeptInfo(selectedNodes);
        $('#deptCodes').val(deptInf[0]) ;
        $('#deptNames').val(deptInf[1]);
      });
    }
    function getDeptInfo(selectedNodes){
      var strDepName = "" ;
      var strDepId = "";
      for(var x=0;x<selectedNodes.length;x++){
        strDepName += selectedNodes[x].fullName+","
        if(selectedNodes[x].type == 1)
          strDepId += selectedNodes[x].code +",";
        else
          strDepId += selectedNodes[x].id +":MEMBER,";
      }
      strDepName = strDepName.substring(0,strDepName.length - 1) ;
      strDepId = strDepId.substring(0,strDepId.length - 1);
      var deptInf = new Array();
      deptInf[0]= strDepId;
      deptInf[1]= strDepName;
      return deptInf;
    }
</script>
  <style>
    #notice_add {
      display: none;
    }
    #notice_show {
      display: none;
    }
  </style>
</head>
<body>
<div class="big-title-metar">后评价系统通知</div>
<div class="container-fluid">
  <div class="row">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">主题：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="noticeTheme"></div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px">发布人：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px"><input type="text" id="noticeCreatePerson"></div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px">发布时间：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px"><input id="noticeCreateDate" type="text" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd'})" readonly></div>
    <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>
    <%
      Object object = request.getSession().getAttribute("globalUniqueUser");
      if(object!=null){
        UserEntity userEntity = (UserEntity)object;
//        if(("UNI".equals(userEntity.getCategory())&&userEntity.getAdmin())||"root".equals(userEntity.getUserName())){
        if(("UNI".equals(userEntity.getCategory()))||"root".equals(userEntity.getUserName())){
    %>
    <div class="btn btn-danger" id="addNotice">添加通知</div>
    <%
        }
      }
    %>
  </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<form action="#" method="post" id="notice_add" >
  <table class="table table-form-4" style="height: 440px">
    <tr style="height: 10px">
      <th>主题<font color="red">*</font></th>
      <td><input type="text" class="form-control __metar_check_form" id="theme" name="theme"><input type="hidden" id="objectId" name="objectId"></td>
    </tr>
    <tr>
      <th style="height: 130px">通知单位<font color="red">*</font></th>
      <td>
        <div class="col-xs-2 col-sm-2" style="padding: 0px;width: 100%">
          <%--<input type="text" style="width: 100%" name="deptNames" id="deptNames" onclick="getDeptName()"/>--%>
          <textarea class="form-control __metar_check_form" rows="3" id="deptNames" name="deptNames" onclick="getDeptName()" readonly style="height: 130px"></textarea>
          <input type="hidden" name="deptCodes"  id="deptCodes"/>
        </div>
      </td>
    </tr>
    <tr style="height: 7px">
      <th>通知开始时间<font color="red">*</font></th>
      <td><input type="text" class="form-control __metar_check_form _task_start" id="startDate" name="startDate" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd'})" readonly></td>
    </tr>
    <tr style="height: 7px">
      <th>通知结束时间<font color="red">*</font></th>
      <td><input type="text" class="form-control __metar_check_form _task_end" id="endDate" name="endDate" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd'})" readonly></td>
    </tr>
    <tr >
      <th style="height: 150px">内容<font color="red">*</font></th>
      <td ><textarea class="form-control __metar_check_form" rows="3" id="operDesc" name="operDesc" onkeyup="limitLength(this.name,600)" style="height:150px"></textarea></td>
    </tr>
    <tr >
      <th style="height: 7px">是否置顶</th>
      <td>
        <select id="top" name="top" style="width: 100%">
          <option value="false" selected>否</option>
          <option value="true">是</option>
        </select>
      </td>
    </tr>
  </table>
</form>
<table class="table table-form-4" id="notice_show" style="height: 440px">
  <tr style="height: 10px">
    <th>主题<font color="red">*</font></th>
    <td><span></span></td>
  </tr>
  <tr style="height: 120px">
    <th>通知单位<font color="red">*</font></th>
    <td><textarea class="form-control" rows="3" id="deptNamesShow" readonly style="height: 130px"></textarea></td>
  </tr>
  <tr style="height: 7px">
    <th>通知开始时间<font color="red">*</font></th>
    <td><span></span></td>
  </tr>
  <tr style="height: 7px">
    <th>通知结束时间<font color="red">*</font></th>
    <td><span></span></td>
  </tr>
  <tr style="height: 100px">
    <th>内容<font color="red">*</font></th>
    <td><textarea class="form-control" rows="3" id="operDescShow" readonly style="height:150px"></textarea></td>
  </tr>
  <tr style="height: 7px">
    <th>是否置顶</th>
    <td><span></span></td>
  </tr>
</table>
</body>
</html>
