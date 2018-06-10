<%@ page import="com.metarnet.eomeem.model.EemTempEntity" %>
<%@ page import="java.util.List" %>
<%@ page import="com.metarnet.eomeem.service.impl.EemCommonServiceImpl" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>已申请列表</title>
    <script type="text/javascript">
        __show_metar_loading();
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'tpInputName',
                title: '文件名称',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
            },
            {
                id: 'reportUserTrueName',
                title: '上报人',
                type: 'string',
                headerStyle: '10%',
                columnStyle:'10%',
                columnClass: 'text-center'
            },
            {
                id: 'reportOrgName',
                title: '上报人部门',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'theme',
                title: '上报粒度',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return record.reportYear+'-'+record.reportDate;
               }
            },
            {
                id: 'result',
                title: '审核结果',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },{
                id: 'auditReason',
                title: '操作',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return  "<a href='javascript:;' onclick='allAudit(" + record.objectId + ")'>查看</a>";
                }
            }
        ];
        var dtGridOption_2_1_2 = {
            lang : 'zh-cn',
            ajaxLoad : true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryApplyList&type=3',
            exportFileName : '数据查询列表',
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
            $("#allAudit").click(allAudit);
        });
        function customSearch_2_2_3(){
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['tempId'] = $('#tempName').val();
            grid_2_1_2.parameters['year'] = $('#year').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
            grid_2_1_2.refresh(true);
        }
        <!---------------------------------------------------->


        <!---------------------------------------------------->
        function showReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId+"&type=todo"+"&yearStr="+${yearStr};
        }
        function downReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=downReportData&objectId=" + objectId;
        }

        function allAudit(objectId){
            $('#__pass_modal_window').modal('show');
            pcSignFormLink('__pass_modal_body',objectId,'show');

        }


        function closeModalWindow(){
            $("#__pass_modal_window").modal('hide')
        }

        function __resizeLinkDialog(__link_dialog_body_div , __width , __height , __btns){
            if(__width){
                $('#' + __link_dialog_body_div).parent().css('width' , __width);
                $('#' + __link_dialog_body_div).parent().css('margin-left' , -__width/2);
            }
            if(__height){
                __height = __height > document.documentElement.clientHeight ? document.documentElement.clientHeight : __height;
                $('#'  +__link_dialog_body_div).parent().css('height' , __height);
                if(__btns == 'none'){
                    $('#'  +__link_dialog_body_div).css('height' , __height - 35);
                } else {
                    $('#'  +__link_dialog_body_div).css('height' , __height - 75);
                }

                $('#' + __link_dialog_body_div).parent().css('margin-top' , -__height/2);
            }
        }


        function pcSignFormLink(__link_dialog_body,object,type){
            __resizeLinkDialog(__link_dialog_body , 1000 , 370);
            var iframe = $('<iframe id="' + __link_dialog_body + '_iframe" frameborder="0" style="width:100%;height:230px;" src="'+_PATH + '/eemCommonController.do?method=getApplyData&objectId='+object+'&type='+type+'"></iframe>');
            $('#' + __link_dialog_body).empty()
            $('#' + __link_dialog_body).append(iframe);

            var __feedBackEditorLink_btn = $('<div class="__dialog_panel_btns"></div>');
            var __feedBackEditorLink_btn_submit = $('<span class="btn btn-danger">保存</span>');
            __feedBackEditorLink_btn_submit.click(function(){
                document.getElementById(__link_dialog_body+"_iframe").contentWindow.submitBtn();
            })
            __feedBackEditorLink_btn.append(__feedBackEditorLink_btn_submit);


            var __closeEditorLink_btn = $('<div class="__dialog_panel_btns"></div>');
            var __closeEditorLink_btn_submit = $('<span class="btn btn-danger">关闭</span>');
            __closeEditorLink_btn_submit.click(function(){
                $("#__pass_modal_window").modal('hide')
            })
            __closeEditorLink_btn.append(__closeEditorLink_btn_submit);

            if(type=='edit'){
                $('#' + __link_dialog_body).append(__feedBackEditorLink_btn);
            }else{
                $('#' + __link_dialog_body).append(__closeEditorLink_btn);
            }
        }
    </script>
    <style type="text/css">
        #all_audit {
            display: none;
        }
    </style>
</head>
<body>
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
                <option value="上半年">上半年</option>
                <option value="下半年">下半年</option>

            </select>
        </div>
        <div class="btn btn-danger" id="custom_search_2_2_3">查询</div>


    </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<div id="__pass_modal_window" class="__link_dialog_container modal">
    <div class="modal-header">申请重报数据查看
        <div class="close" onclick="closeModalWindow('__pass_modal_window')">×</div>
    </div>
    <div class="modal-body" id="__pass_modal_body"></div>
</div>
</body>
</html>
