<meta http-equiv="X-UA-Compatible"content="IE=8">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="base/basePage.jsp"/>
<html>
<head>
  <title>登录</title>
  <style>
    #index-title{
      background-color:#F5F5F5;
      font-size:30px;
      line-height:60px;
      font-family:'微软雅黑';
      padding-left:20px;
    }
    .col-md-4{
      display: inline;
    }
    #loginPanel{
      margin: auto;
      width:500px;
    }
    #alertDiv{
      height:20px;
      color:#ff0000;
    }
    @media screen and (max-width: 500px){
      #loginPanel{
        width: 100%;
      }
    }
  </style>
</head>
<body>
<div id="index-title">登录</div>
<div class="container-fluid" style="margin-top:10px;">
  <div class="row">
    <div class="col-md-4"></div>
    <div class="col-md-4">
      <div id="loginPanel">
        <form id="loginForm" action="<%=request.getContextPath()%>/loginController.do?method=userlogin" method="post">
          <div class="form-group" style="border:10px dashed #bbbbbb;padding: 50px;border-radius: 10px">
            <label class="login-title">用户名</label>
            <input type="text" class="form-control" name="username" id="username" placeholder="请输入用户名">
            <label class="login-title">密码</label>
            <input type="password" class="form-control" name="password" id="password"  placeholder="请输入密码">
            <%--<input type="password" class="form-control" name="password" id="password" value="metarnet@110" placeholder="请输入密码">--%>
            <div id="alertDiv"></div>
            <span onclick="login()" class="btn btn-danger">登录</span>
            <br/>
            <br/>
            <div><input id="__file_upload" type="file" multiple="true"/></div>
            <div>此文本框可输入长度为10：<input maxlength="10"/></div>
            <div>日期选择（年月日时分秒）<input class="form-control" type="text" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss', minDate: '%y-%M-%d'})"/></div>
            <div>日期选择（年月日）<input class="form-control" type="text" onfocus="WdatePicker({dateFmt: 'yyyy-MM-dd', minDate: '%y-%M-%d'})"/></div>
            <div>日期选择（时分秒）<input class="form-control" type="text" onfocus="WdatePicker({dateFmt: 'HH:mm:ss'})"/></div>
            人员树-单选
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__person_tree_test_radio" class="input-group-addon glyphicon glyphicon-user"></span>
            </div>
            人员树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__person_tree_test" class="input-group-addon glyphicon glyphicon-user"></span>
            </div>
            组织树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__org_tree_test" class="input-group-addon glyphicon glyphicon-home"></span>
            </div>
            全国树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__whole_org_tree_test" class="input-group-addon glyphicon glyphicon-home"></span>
            </div>
            会签处室树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__sing_org_tree_test" class="input-group-addon glyphicon glyphicon-home"></span>
            </div>
            专业网管树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__major_org_tree_test" class="input-group-addon glyphicon glyphicon-home"></span>
            </div>
            派发树
            <div class="input-group">
              <input type="text" class="form-control">
              <span id="__dispatch_tree_test" class="input-group-addon glyphicon glyphicon-th"></span>
            </div>
            <br/>
                    <span class="checkbox">
                        <label>
                          <input name="test1" type="checkbox" value="">这是一个复选框1
                        </label>
                        <label>
                          <input name="test1" type="checkbox" value="">这是一个复选框2
                        </label>
                    </span>
            <br/>
                    <span class="radio">
                        <label>
                          <input name="test2" type="radio" value="">这是一个单选框1
                        </label>
                        <label>
                          <input name="test2" type="radio" value="">这是一个单选框2
                        </label>
                    </span>
            <br/>
            <div>
              枚举值
              <select id="__enum_1" class="form-control __metar_enum" enumCode="RATE">
              </select>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
  function login(){
    $('#alertDiv').html('');
    var username = $('#username').val();
    var password = $('#password').val();
    if(username == ''){
      $('#username').focus();
      $('#alertDiv').html('请输入用户名');
      return;
    } else if(password == ''){
      $('#password').focus();
      $('#alertDiv').html('请输入密码');
      return;
    }
    $('#loginForm').submit();
  }

  $('#__person_tree_test_radio').click(function(){
    __open_tree(this.id , 2 , '人员树-单选' ,function(selectedNodes){
//        console.info(selectedNodes);
      alert(selectedNodes);
    } , '' , '' , 'radio');
  });
  $('#__person_tree_test').click(function(){
    __open_tree(this.id , 2 , '人员树' ,function(selectedNodes){
      console.info(selectedNodes);
      alert(selectedNodes);
    });
  });
  $('#__org_tree_test').click(function(){
    __open_tree(this.id , 1 , '组织树' ,function(selectedNodes){
      alert(selectedNodes);
    });
  });
  $('#__whole_org_tree_test').click(function(){
    __open_tree(this.id , 1 , '全国树' ,function(selectedNodes){
      alert(selectedNodes);
    } , 120);
  });
  $('#__sing_org_tree_test').click(function(){
    __open_tree(this.id , 4 , '会签处室树' ,function(selectedNodes){
      alert(selectedNodes);
    } , '' , 1);
  });
  $('#__major_org_tree_test').click(function(){
    __open_tree(this.id , 5 , '专业网管树' ,function(selectedNodes){
      alert(selectedNodes);
    } , '' , 1);
  });
  $('#__dispatch_tree_test').click(function(){
    __open_tree(this.id , 3 , '派发树' ,function(selectedNodes){
      console.info(selectedNodes);
      alert(selectedNodes);
    });
  });

  //$(".form_datetime").datetimepicker({
  //    format: "dd MM yyyy - hh:ii",
  //    autoclose: true,
  //    todayBtn: true,
  //    pickerPosition: "bottom-left"
  //});

  __init_attachment_function('__file_upload' , '1' , 'test' , 'edit');
  __loadValues4Select();
</script>
</body>
</html>
