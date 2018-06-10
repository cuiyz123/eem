<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
  <title>集团扣分统计</title>
  <script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
      {
        id: 'equipmentName',
        title: '设备类型',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
      },
      {
        id: 'huawei',
        title: '华为',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '5%',
          columnStyle:'5%'
//        headerStyle: 'width:15%',
//        resolution: function (value, record, column, grid, dataNo, columnNo) {
//          if(value==''){
//            return "0";
//          }else{
//            return value+"%";
//          }
//        }
      },
        {
            id: 'zhongxing',
            title: '中兴',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        }, {
            id: 'ailixin',
            title: '爱立信',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'nuoxi',
            title: '诺西',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'beier',
            title: '贝尔',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'cisco',
            title: 'CISCO',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'fenghuo',
            title: '烽火',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
            //headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'alang',
            title: '阿郎',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'juniper',
            title: 'JUNIPER',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%'
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'tefa',
            title: '特发',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'putian',
            title: '普天',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'datang',
            title: '大唐',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'xinyoutong',
            title: '新邮通',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
        {
            id: 'huasan',
            title: '华三',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '5%',
            columnStyle:'5%'
//            headerStyle: 'width:15%',
//            resolution: function (value, record, column, grid, dataNo, columnNo) {
//                if(value==''){
//                    return "0";
//                }else{
//                    return value+"%";
//                }
//            }
        },
    ];
    var dtGridOption_2_1_2 = {
      lang : 'zh-cn',
      ajaxLoad : true,
      loadURL: '<%=path%>/deductController.do?method=countAnalysis',
      exportFileName : '统计分析列表',
      columns : dtGridColumns_2_1_2,
      gridContainer : 'dtGridContainer_2_1_2',
      toolbarContainer : 'dtGridToolBarContainer_2_1_2',
      tools:'refresh',
      pageSize: 1000,
      pageSizeLimit : []
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
      grid_2_1_2.load(function(){
        __hide_metar_loading();
      });
      //绑定方法
      $('#custom_search_2_2_3').click(customSearch_2_2_3);
    });
    function customSearch_2_2_3(){
      grid_2_1_2.parameters = new Object();
       var reportDate = $('#reportDate').val();
        var year = $('#year').val()
        if(!reportDate||!year){
            alert("请选择统计粒度");
            return ;
        }
      grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
        grid_2_1_2.parameters['year'] = $('#year').val();
      grid_2_1_2.refresh(true);
    }
    function showTimelyRate(name,code){
      window.location.href = "<%=path%>/eemCommonController.do?method=initTimelyRate&provinceName="+name+"&provinceCode=" + code+"&reportDate="+$("#reportDate").val();
    }
    function showAccuracyRate(name,code){
      window.location.href = "<%=path%>/eemCommonController.do?method=initAccuracyRate&provinceName="+name+"&provinceCode=" + code+"&reportDate="+$("#reportDate").val();
    }
  </script>
</head>
<body>
<div class="big-title-metar">集团扣分统计</div>
<div class="container-fluid">
  <div class="row">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">统计粒度：</div>
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
    <div class="btn btn-danger" style="margin-left: 15px;" id="custom_search_2_2_3">查询</div>
      <div class="col-xs-1 col-sm-3" style="float: right;">— 表示此厂家未提供该类设备</div>
  </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
