<%@ page import="com.metarnet.eomeem.model.EemApply" %>
<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>

<html>
<head>
    <title>待办列表</title>

  <%--  <div1 id="div1" ><marquee id=go1 onMouseOver=go1.stop()
                   onMouseOut=go1.start() scrollamount=2 scrolldelay=10
                   direction=right><FONT size="4" color=#FF0000>你好！集团已退回数据！！！</FONT></MARQUEE>
    </div1>
<script>

    function showdiv1(){
        var entry= <%=request.getAttribute("entry")%>;
        if (entry==1){//if判断条件成立的情况
            document.getElementById("div1").style.visibility="visible";
        }else{
            document.getElementById("div1").style.visibility="hidden";
        }
    }


</script>--%>

    <script type="text/javascript">

          //映射内容

        function customSearch_2_2_3(){
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['tempId'] = $('#tempName').val();
            grid_2_1_2.parameters['year'] = $('#year').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
            //
            grid_2_1_2.parameters['deptIds'] = $("#deptIds").val();
            grid_2_1_2.parameters['type'] = 'pro';
            //jw   省份查询框

            grid_2_1_2.refresh(true);
//            console.log();

        }

          //  jw 3.7省份查询框
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
          //
        <!---------------------------------------------------->




          function allAudit(){

            $('#all_audit')[0].reset();
            var records = grid_2_1_2.getCheckedRecords();
            var ids = "";
            for(var record in records){
                ids+=records[record].objectId+",";
            }
            if (ids.charAt(ids.length-1)==",") {
                ids = ids.substr(0,ids.length-1);
            }

            $('#ids').val(ids);
            showWin("批量审核");
        }
        function showWin(name){
            __open_metar_window("allAudit", name, 600, 300, function(__window_body){
                __window_body.append($("#all_audit"));
                $("#all_audit").show();
                var __applyEitorLink_btns = $('<div class="__dialog_panel_btns"></div>');
                var __applyEitorLink_btn_submit = $('<span class="btn btn-danger" style="margin-top: 5px">提交</span>');
                var __applyEitorLink_btn_cancel = $('<span class="btn btn-danger" style="margin-top: 5px">取消</span>');
                __applyEitorLink_btn_submit.click(function () {
                    if(saveAudit()){
                        __window_body.parent().modal('hide');

                    }else{
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
        function saveAudit(){

            if($('#ids').val()==null||$('#ids').val()==''){
               alert("请选择数据！");
                return false;
            }
            var sta= "";
           var staId= $("[name='audit']:checked").val();
            var operdesc = "";
            var operDesc=$("#operDesc").val();

            $.ajax({
                type: "POST",
                url: _PATH+"/eemCommonController.do?method=saveAudit",
                data:{
                    eid:$('#ids').val(),
                    staus:staId,
                    operdesc:operDesc
                } ,

            async: false,
                dataType: "json",
                success: function (response) {
                    if (response.success) {
                        grid_2_1_2.load(function(){
                            __hide_metar_loading();
                        });
                        $('#all_audit')[0].reset();
                        $('#ids').val('');
//                        $("#operDesc").val(data.operDesc);
//                        $("#all_audit").hide();
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

        <!---------------------------------------------------->
        function showReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId+"&type=todo"+"&yearStr="+$("#year").val()+
            "&tempId="+$('#tempName').val()+"&reportDate="+$('#reportDate').val();
        }
        function downReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=downReportData&objectId=" + objectId;
        }


        function allDown(){
            var ids = "";
            var records = grid_2_1_2.getCheckedRecords();
            for(var record in records){
                ids+=records[record].objectId+",";
            }
            if (ids.charAt(ids.length-1)==",") {
                ids = ids.substr(0,ids.length-1);
            }
            if (ids=="") {
                alert('请选择下载的数据！');
                return;
            }

            window.location.href = "<%=path%>/reportController.do?method=allDownReportData&eid=" + ids ;
        }
    </script>
    <style type="text/css">
        #all_audit {
            display: none;
        }
    </style>
</head>
<%--
<body onload="showdiv1();">
--%>
<body onload="customSearch_2_2_3()">


<input type="hidden" id="tempIds" value="${tempIds}">
<div class="big-title-metar">待办查询</div>
<input type="hidden" id="ids">
<div class="container-fluid">
    <div class="row">
        <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">模板名称：</div>
        <div class="col-xs-4 col-sm-4" style="padding: 0px">
            <select id="tempName" style="width: 90%">
                <option value="" selected>-------------------------------</option>
                <c:forEach items="${tempList}" var="temp">
                    <option value="${temp.objectId}">${temp.tempName}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">粒度：</div>
        <div class="col-xs-2 col-sm-2" style="padding: 0px">
            <select name="year" id="year" style="width: 45%">
                <option value="" selected>-------</option>
                <c:forEach begin="2015" end="${yearStr}" var="y" step="1">
                    <c:if test="${y eq yearStr}" var="ss">
                        <option value="${y }" selected="selected">${y }</option>
                    </c:if>
                    <c:if test="${!ss}" var="ss">
                        <option value="${y }">${y }</option>
                    </c:if>
                </c:forEach>
            </select>
            <select name="reportDate" id="reportDate" style="width: 45%">
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
        <%--////////////////////////////////////省份查询框/////--%>
        <%
            Object object = request.getSession().getAttribute("globalUniqueUser");
            if (object != null) {
                UserEntity userEntity = (UserEntity) object;
        %>
        <div class="col-xs-1 col-sm-1" style="padding: 0px">省分：</div>
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
                    }
                }
            %>
            <input type="hidden" name="deptIds" id="deptIds"/>
        </div>

        <%-- /////////////////////////////////////////////--%>
        <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>

        <div class="btn btn-danger" id="allAudit">批量审核</div>
        <div class="btn btn-danger" id="collect_2_2_9">打包下载</div>
    </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<form action="#" method="post" id="all_audit">
    <table class="table table-form-4">
        <tr>
            <th>审核意见<font color="red"></font></th>
            <td><textarea class="form-control" rows="3" id="operDesc" name="operDesc" ></textarea></td>
        </tr>
        <tr>
            <th>审核</th>
            <td>
                <label><input  type="radio"  name="audit" checked staId="passId" value="Y" />通过</label>
                <label><input  type="radio" name="audit"  staId="rejectId" value="N" />驳回</label>

            </td>
        </tr>
    </table>
</form>
<script>
    var dtGridColumns_2_1_2 = [
        {
            id: 'tpInputName',
            title: '文件名称',
            type: 'string',
//                headerStyle: 'width:160px',
            headerStyle: '15%',
            columnStyle:'15%',
            columnClass: 'text-center',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return "<a href='javascript:;' onclick='showReportData("+record.objectId+")'>《"+value+"-"+record.operOrgName+"》待审核</a>";
            }
        },
        {
            id: 'operUserTrueName',
            title: '上报人',
            type: 'string',
//                headerStyle: 'width:100px',
            headerStyle: '10%',
            columnStyle:'10%',
            columnClass: 'text-center'
        },
        {
            id: 'operOrgName',
            title: '上报人部门',
            headerStyle: '15%',
            columnStyle:'15%',
//                headerStyle: 'width:120px',
            type: 'string',
            columnClass: 'text-center'
        },
        {
            id: 'operUserPhone',
            title: '电话',
            type: 'string',
            headerStyle: '10%',
            columnStyle:'10%',
//                headerStyle: 'width:120px',
            columnClass: 'text-center'
        },
        {
            id: 'dateGrading',
            title: '统计粒度',
            headerStyle: '10%',
            columnStyle:'10%',
//                headerStyle: 'width:100px',
            type: 'string',
            columnClass: 'text-center'
        },
        {
            id: 'creationTime',
            title: '上报时间',
            type: 'date',
            format:'yyyy-MM-dd hh:mm:ss',
            columnClass: 'text-center' ,
//                headerStyle: 'width:160px'
            headerStyle: '15%',
            columnStyle:'15%'
        },
        {
            id: 'objectId',
            title: '操作',
            headerStyle: '15%',
//                columnStyle:'15%',
            type: 'string',
//                headerStyle: 'width:100px',
            columnClass: 'text-center',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return "<a href='javascript:;' onclick='downReportData("+value+")'>下载</a>";
            }}
    ];
    var dtGridOption_2_1_2 = {
        lang : 'zh-cn',
        ajaxLoad : true,
        check : true,
        loadURL: '<%=path%>/eemCommonController.do?method=queryDataList&type=todo&tempIds='+$('#tempIds').val(),
        exportFileName : '数据查询列表',
        columns : dtGridColumns_2_1_2,
        gridContainer : 'dtGridContainer_2_1_2',
        toolbarContainer : 'dtGridToolBarContainer_2_1_2',
        tools:'refresh',
        pageSize : 50,
        pageSizeLimit : [10, 20, 50,100]
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
        grid_2_1_2.load(function(){
            __hide_metar_loading();
        });
        $('#custom_search_2_2_3').click(customSearch_2_2_3);
        $("#allAudit").click(allAudit);
        $('#collect_2_2_9').click(allDown);

    });
</script>
</body>
</html>
