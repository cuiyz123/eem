<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
  <title>数据汇总</title>
    <style type="text/css">
        .div2{
            position: absolute;
            top: 32px;
        }
        .div0{
            height: 250px;
            overflow-x: hidden;
            position: relative;
        }
        .div1{
            position: fixed;
            background: #fff;
            width: 98.5%;
            z-index:10;
        }
    </style>
  <script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
      {
        id: 'excelName',
        title: '文件名称',
        type: 'string',
          headerStyle: '20%',
          columnStyle:'20%',
        columnClass: 'text-center'
//        headerStyle: 'width:20%'
      },
      {
          id: 'reportDeptNames',
          title: '上报省分',
          type: 'string',
          headerStyle: '20%',
          columnStyle:'20%',
          columnClass: 'text-center'
//          headerStyle: 'width:18%'
      },
      {
        id: 'dateGrading',
        title: '汇总粒度',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
//        headerStyle: 'width:10%'
      },
      {
        id: 'creationTime',
        title: '汇总时间',
        type: 'date',
        format:'yyyy-MM-dd hh:mm:ss',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
//          headerStyle: 'width:15%'
      },
      {
        id: 'operUserTrueName',
        title: '汇总人',
        type: 'string',
        columnClass: 'text-center',
          columnStyle:'15%',
          headerStyle: '15%'
      },
      {
        id: 'operUserPhone',
        title: '联系电话',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
      },
      {
        id: 'objectId',
        title: '操作',
        type: 'string',
        columnClass: 'text-center',
//        headerStyle: 'width:10%',
          headerStyle: '10%',
          columnStyle:'10%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
              return "<a href='javascript:;' onclick='downReportData("+value+")'>下载</a>";
        }}
    ];
    var dtGridOption_2_1_2 = {
      lang : 'zh-cn',
      ajaxLoad : true,
      check : true,
      loadURL: '<%=path%>/eemCommonController.do?method=sumDataList',
      exportFileName : '数据汇总列表',
      columns : dtGridColumns_2_1_2,
      gridContainer : 'dtGridContainer_2_1_2',
      toolbarContainer : 'dtGridToolBarContainer_2_1_2',
      tools:'refresh',
      pageSize : 10,
      pageSizeLimit : [10, 20, 50,100]
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
      grid_2_1_2.load(function(){
        __hide_metar_loading();
      });
        $('#custom_search_2_2_3').click(customSearch_2_2_3);
        $('#collect_2_2_6').click(all_collect_down);
    });
    function customSearch_2_2_3(){
        $("#deptIds").val("");
        $("#deptNames").val("");
        grid_2_1_2.parameters = new Object();
        grid_2_1_2.parameters['tempId'] = $('#tempName').val();
        grid_2_1_2.parameters['year'] =$("#reportYear").val();
        grid_2_1_2.parameters['reportDate'] =  $("#reportDate").val();
        grid_2_1_2.parameters['deptIds']  = $("#deptIds").val();
        grid_2_1_2.refresh(true);
    }
      function all_collect_down(){
          var reportYear = $("#reportYear1").val();
          var reportDate = $("#reportDate1").val();
          var temId = $("#tempName1").val();
          var dept = $("#deptIds").val();
          if(""==temId){
              alert("请选择模板");
              return;
          }
          if(""==reportYear){
              alert("请选择年份");
              return;
          }
          if(""==reportDate){
              alert("请选择粒度");
              return;
          }
          if(""==dept){
              alert("请选择上报省分");
              return;
          }else{

              //-------------------------------------------4.8之前注释掉现在放开----
              $.ajax({
                  type: "post",
                  dataType: 'Json',
                  data:{
                      reportDate:reportDate
                  },
                  url: "<%=path%>/eemSummaryController.do?method=checkReportPro&formId="+temId+"&reportYear="+reportYear+"&deptIds="+dept,
                  async: false,
                  success: function (obj) {
                      if (obj.success) {
                          __hide_metar_loading();
                          if(obj.msg==undefined){
              //------------------------4.8之前注释掉现在放开---------------------

                              $.ajax({
                                  type: "post",
                                  dataType: 'Json',
                                  data:{
                                      formId:temId,
                                      reportYear:reportYear,
                                      reportDate:reportDate,
                                      deptIds:dept,
                                      deptNames:$("#deptNames").val()
                                  },
                                  url: "<%=path%>/eemSummaryController.do?method=ZBCollectData",
                                  async: false,
                                  success: function (obj) {
                                      if(obj.success){
                                         alert(obj.msg);
                                          setTimeout(function(){
//                                              alert("哈");
                                             grid_2_1_2.refresh(true);
                                          },45000)
                                      }
                                  },
                                  error: function (request) {
                                      alert("汇总失败，请重试");
                                  }
                              });

              //---------------------------------------------4.8之前注释掉现在放开
                          }else{
                              var r = confirm(obj.msg+"，确定要将未审核数据进行汇总？");
                              if(r){
//                                  window.location.href = "eemSummaryController.do?method=ZBCollectData&formId="+temId+"&reportYear="+reportYear+"&reportDate="+reportDate+"&deptIds="+dept+"&deptNames="+$("#deptNames").val();
                                  $.ajax({
                                      type: "post",
                                      dataType: 'Json',
                                      data:{
                                          formId:temId,
                                          reportYear:reportYear,
                                          reportDate:reportDate,
                                          deptIds:dept,
                                          deptNames:$("#deptNames").val()
                                      },
                                      url: "<%=path%>/eemSummaryController.do?method=ZBCollectData",
                                      async: false,
                                      success: function (obj) {
                                          if(obj.success){
                                              alert(obj.msg);
                                          }
                                      },
                                      error: function (request) {
                                          alert("汇总失败，请重试");
                                      }
                                  });
                              }
                          }
                      }else{
                          alert($("[id=tempName1] option:selected").text()+"，"+$("#reportYear1").val()+"年"+$("[id=reportDate1] option:selected").text()+"，"+obj.msg);
                      }
                  },
                  error: function (request) {
                      alert("汇总失败，请重试");
                  }
              });
              //-----------------------------4.8之前注释掉现在放开----------------


          }
      }
    function getDeptName(){
        __open_tree(this.id , 1 , '派发树' ,function(selectedNodes){
            var deptInf = getDeptInfo(selectedNodes);
            $('#deptIds').val(deptInf[0]) ;
            $('#deptNames').val(deptInf[1]);
        });
    }
    function getDeptInfo(selectedNodes){
        var strDepName = "" ;
        var strDepId = "";
        for(var x=0;x<selectedNodes.length;x++){
            strDepName += selectedNodes[x].label+","
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

    function showReportData(objectId){
        window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId+"&type=query";
    }
    function downReportData(objectId){
        window.location.href = "<%=path%>/eemCommonController.do?method=downCollectExcel&eid=" + objectId;
    }
    function all_down(){
        var records = grid_2_1_2.getCheckedRecords();
        var ids = "";
        if(records.length==0){
            alert("请选择要下载的记录");
            return;
        }else{
            var r = confirm('您一共选择了 '+records.length+' 条记录，确定要下载吗？');
            if(r){
                for(var record in records){
                    ids+=records[record].objectId+",";
                }
                if (ids.charAt(ids.length-1)==",") {
                    ids = ids.substr(0,ids.length-1);
                }
                window.location.href = "<%=path%>/eemCommonController.do?method=downReportExcel&eid=" + ids;
            }
        }
    }
    function modifyReportDate(){
        var frequencyOfReporting=$("[id=tempName] option:selected").attr("lang");
        if(frequencyOfReporting==2){
            $("#reportDate").empty();
            $("#reportDate").append('<option value="" selected>-------</option>');
            $("#reportDate").append('<option value="上半年">上半年</option>');
            $("#reportDate").append('<option value="下半年">下半年</option>');
        }else{
            $("#reportDate").empty();
            $("#reportDate").append('<option value="" selected>-------</option>');
            $("#reportDate").append('<option value="第一季度">第一季度</option>');
            $("#reportDate").append('<option value="第二季度">第二季度</option>');
            $("#reportDate").append('<option value="第三季度">第三季度</option>');
            $("#reportDate").append('<option value="第四季度">第四季度</option>');
        }
    }
    function modifyReportDate1(){
        var frequencyOfReporting=$("[id=tempName1] option:selected").attr("lang");
        if(frequencyOfReporting==2){
            $("#reportDate1").empty();
            $("#reportDate1").append('<option value="" selected>-------</option>');
            $("#reportDate1").append('<option value="上半年">上半年</option>');
            $("#reportDate1").append('<option value="下半年">下半年</option>');
        }else{
            $("#reportDate1").empty();
            $("#reportDate1").append('<option value="" selected>-------</option>');
            $("#reportDate1").append('<option value="第一季度">第一季度</option>');
            $("#reportDate1").append('<option value="第二季度">第二季度</option>');
            $("#reportDate1").append('<option value="第三季度">第三季度</option>');
            $("#reportDate1").append('<option value="第四季度">第四季度</option>');
        }
    }
      function yearChange(value){
          $("#collect_2_2_6").text(value+"年"+$("#reportDate1").val()+"汇总");
//          $("#collect_2_2_7").text(value+"全年汇总");
      }
      function dateChange(value){
          if($("[id=reportYear1] option:selected").text()==undefined||$("[id=reportYear1] option:selected").text()==''){
              $("#collect_2_2_6").text(${yearStr}+"年"+value+"汇总");
          }else{
              $("#collect_2_2_6").text($("[id=reportYear1] option:selected").text()+"年"+value+"汇总");
          }
      }
    window.onload=function(){
        yearChange(${yearStr});
    }
  </script>
</head>
<body>
<div class="big-title-metar">数据汇总</div>
    <table class="table table-form-4">
        <tr>
            <th>模板名称</th>
            <td>
                <select id="tempName1" style="width: 100%" onchange="modifyReportDate1()">
                    <option value="" selected>-------------------------------</option>
                    <c:forEach items="${tempList}" var="temp">
                        <option value="${temp.objectId}" lang="${temp.reportedFrequency}">${temp.tempName}</option>
                    </c:forEach>
                </select>
            </td>
            <th>粒度</th>
            <td>
                <select id="reportYear1" style="width: 40%" onchange="yearChange(this.value)">
                    <%--<option value="" selected>-------</option>--%>
                    <c:forEach begin="2015" end="${yearStr}" var="y" step="1">
                        <c:if test="${y eq yearStr}"  var="ss">
                            <option  value="${y}" selected="selected" >${y}</option>
                        </c:if>
                        <c:if test="${!ss}">
                            <option  value="${y}">${y}</option>
                        </c:if>

                    </c:forEach>
                </select>
                <select id="reportDate1" style="width: 58%" onchange="dateChange(this.value)">
                    <%--<option value="" selected>-------</option>--%>
                    <%--<option value="第一季度">第一季度</option>--%>
                    <%--<option value="第二季度">第二季度</option>--%>
                    <%--<option value="第三季度">第三季度</option>--%>
                    <%--<option value="第四季度">第四季度</option>--%>
                    <option value="上半年">上半年</option>
                    <option value="下半年">下半年</option>
                    <%--<option value="13">全年</option>--%>
                </select>
            </td>
        </tr>
        <tr>
            <th>上报省分</th>
            <td colspan="3">
                <div class="col-xs-2 col-sm-2" style="padding: 0px;width: 100%">
                    <input type="text" style="width: 100%" name="deptNames" id="deptNames" onclick="getDeptName()" readonly/>
                    <input type="hidden" name="deptIds"  id="deptIds"/>
                </div>
            </td>
        </tr>
        <tr>
            <td colspan="4" style="text-align: center;border: none;">
                <%
                    Object object = request.getSession().getAttribute("globalUniqueUser");
                    if(object!=null){
                        UserEntity userEntity = (UserEntity)object;
                        if("UNI".equals(userEntity.getCategory())){
                %>
                <div class="btn btn-danger"  id="collect_2_2_6" style="margin-top: 10px">总部汇总</div>
                <%--<div class="btn btn-danger"  id="collect_2_2_7">全年汇总</div>--%>
                <%
                        }
                    }
                %>
            </td>
        </tr>
    </table>
<hr>
<div class="big-title-metar">已汇总数据列表</div>
<div class="container-fluid">
  <div class="row">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">模板名称：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
        <select id="tempName" style="width: 90%" onchange="modifyReportDate()">
            <option value="" selected>-------------------------------</option>
            <c:forEach items="${tempList}" var="temp">
                <option value="${temp.objectId}" lang="${temp.reportedFrequency}">${temp.tempName}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px">粒度：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
      <select id="reportYear" style="width: 45%">
        <%--<option value="" selected>-------</option>--%>
          <c:forEach begin="2015" end="${yearStr}" var="y" step="1">
              <c:if test="${y eq yearStr}"  var="ss">
                  <option  value="${y}" selected="selected" >${y}</option>
              </c:if>
              <c:if test="${!ss}">
                  <option  value="${y}">${y}</option>
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
          <%--<option value="13">全年</option>--%>
      </select>
    </div>
    <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>
    <div class="btn btn-danger" onclick="all_down()">批量下载</div>
  </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
