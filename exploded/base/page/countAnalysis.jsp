<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
  <title>统计分析</title>
     <%--jw4.1 --%>

    <%--   //---------==============================================--%>
    <script type="text/javascript" src="<%=path%>/base/js/boostrap_table_export/table_export/tableExport.js"></script>


    <script type="text/javascript">
    __show_metar_loading();
    //映射内容
    var dtGridColumns_2_1_2 = [
      {
        id: 'provinceName',
        title: '省分',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
      },
      {
        id: 'reportPerson',
        title: '上报人(联系电话)',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%'
      },
      {
        id: 'timelyRate',
        title: '及时率(%)',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(record.reportPerson==undefined||record.reportPerson==''){
            return "0";
          }else if(value==100){
            return "100%";
          }else{
            if(value==''){
              return '<a href="javascript:;" onclick="showTimelyRate(\''+record.provinceName+'\',\''+record.provinceCode+'\')">'+0+'</a>';
            }else{
              return '<a href="javascript:;" onclick="showTimelyRate(\''+record.provinceName+'\',\''+record.provinceCode+'\')">'+value+'%</a>';
            }
          }
        }
      },
      {
        id: 'momTimelyRate',
        title: '环比(%)',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(value==''){
            return "0";
          }else{
            return value+"%";
          }
        }
      },
      {
        id: 'accuracyRate',
        title: '准确率(%)',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(record.reportPerson==undefined||record.reportPerson==''){
            return "0";
          }else if(value==100){
            return "100%";
          }else{
            if(value==''){
              return '<a href="javascript:;" onclick="showAccuracyRate(\''+record.provinceName+'\',\''+record.provinceCode+'\')">0</a>';
            }else{
              return '<a href="javascript:;" onclick="showAccuracyRate(\''+record.provinceName+'\',\''+record.provinceCode+'\')">'+value+'%</a>';
            }
          }
        }
      },
      {
        id: 'momAccuracyRate',
        title: '环比(%)',
        type: 'string',
        columnClass: 'text-center',
          headerStyle: '15%',
          columnStyle:'15%',
        resolution: function (value, record, column, grid, dataNo, columnNo) {
          if(value==''){
            return "0";
          }else{
            return value+"%";
          }
        }
      }
    ];
   var dtGridOption_2_1_2 = {
      lang : 'zh-cn',
      ajaxLoad : true,
      loadURL: '<%=path%>/eemCommonController.do?method=countAnalysis&code=1',
      exportFileName : '统计分析列表',
      columns : dtGridColumns_2_1_2,
      gridContainer : 'dtGridContainer_2_1_2',
      toolbarContainer : 'dtGridToolBarContainer_2_1_2',
      tools :'print',
      pageSize: 50,
      pageSizeLimit : [10, 20, 50]
    };
    /* tools:'refresh',*/
    /* tools:'refresh',
     tools :'export[excel,csv,pdf,txt]',*/
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
      grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
      grid_2_1_2.refresh(true);
    }
    function showTimelyRate(name,code){
      window.location.href = "<%=path%>/eemCommonController.do?method=initTimelyRate&provinceName="+encodeURIComponent(encodeURIComponent(name))+"&provinceCode=" + code+"&reportDate="+encodeURIComponent(encodeURIComponent($("#reportDate").val()));
    }
    function showAccuracyRate(name,code){
      window.location.href = "<%=path%>/eemCommonController.do?method=initAccuracyRate&provinceName="+encodeURIComponent(encodeURIComponent(name))+"&provinceCode=" + code+"&reportDate="+encodeURIComponent(encodeURIComponent($("#reportDate").val()));
    }

  </script>
</head>
<body>

<table id="datatable">

<div class="big-title-metar">统计分析</div>
<div class="container-fluid">
  <div class="row">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">统计粒度：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
      <select id="reportDate" style="width: 100%">
      <%--  <option value="2016-上半年">2016-上半年</option>--%>
        <option value="2017-下半年">2017-下半年</option>
        <option value="2017-上半年">2017-上半年</option>
        <option value="2016-下半年">2016-下半年</option>

      <%--<option value="2016-全年">2016年全年</option>--%>
      </select>
    </div>
    <div class="btn btn-danger" style="margin-left: 15px;" id="custom_search_2_2_3">查询</div>

      <%--<div class="btn btn-danger" style="margin-left: 15px;" onclick="outtable()">导出</div>--%>


    <%--jw 3.26--%>
    <%--<div class="btn btn-danger" style="margin-left:50px" id="btn_d  ownload" onClick ="$('#dtGridContainer_2_1_2').tableExport({ type: 'excel', escape: 'false', fileName: '统计分析列表' })">数据导出</div>
--%>  </div>
</div>

<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<%--<jw4.1-%>--%>
<%--<table id="table">
</table>--%>

</table>

</body>
</html>
