<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>及时率数据详情查询</title>
    <script type="text/javascript">

        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'tpInputName',
                title: '文件名称',
                type: 'string',
                headerStyle: 'width:160px',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='downReportData(" + record.objectId + ")'>"+record.operOrgName+"_"+record.objectId+"_"+value+"_"+record.reportDate+"</a>";
                    return ;
                }
            },
            {
                id:'fileName',
                title:'表单名称',
                type:'string',
                headerStyle: 'width:100px',
                columnClass: 'text-center'
            },
            {
                id: 'operUserTrueName',
                title: '修改人',
                type: 'string',
                headerStyle: 'width:100px',
                columnClass: 'text-center'
            },
            {
                id: 'operOrgName',
                title: '上报人部门',
                headerStyle: 'width:120px',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'operUserPhone',
                title: '电话',
                type: 'string',
                headerStyle: 'width:120px',
                columnClass: 'text-center'
            },
            {
                id: 'dateGrading',
                title: '上报周期',
                headerStyle: 'width:100px',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'creationTime',
                title: '上报时间',
                type: 'date',
                format: 'yyyy-MM-dd hh:mm:ss',
                columnClass: 'text-center',
                headerStyle: 'width:160px'
            },
            {
                id: 'objectId',
                title: '操作',
                type: 'string',
                headerStyle: 'width:100px',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='showReportData(" + value + ")'>查看</a>";
                }
            }
        ];
        <%
            if(request.getAttribute("provinceCode")==null||"".equals(request.getAttribute("provinceCode"))){
                request.setAttribute("provinceCode",request.getParameter("provinceCode"));
                pageContext.setAttribute("provinceCode",request.getParameter("provinceCode"), pageContext.REQUEST_SCOPE);
                request.setAttribute("provinceName",request.getParameter("provinceName"));
            }
            %>
        debugger;
        var dtGridOption_2_1_2 = {
            lang: 'zh-cn',
            ajaxLoad: true,
            loadURL: '<%=path%>/eemCommonController.do?method=showTimelyRate&provinceCode=${provinceCode}&reportDate='+ encodeURIComponent(encodeURIComponent("${reportDate}")),
            exportFileName: '及时率分析列表',
            columns: dtGridColumns_2_1_2,
            gridContainer: 'dtGridContainer_2_1_2',
            toolbarContainer: 'dtGridToolBarContainer_2_1_2',
            tools: 'refresh',
            pageSize: 10,
            pageSizeLimit : [10,20,50]
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function () {
                __hide_metar_loading();
            });
            $('#collect_2_2_1').click(searchData);
//            $('#collect_2_2_2').click(searchProData);
        });
        //查询数据
        function searchData() {
            grid_2_1_2.parameters = new Object();
            grid_2_1_2.parameters['provinceCode'] = $('#deptIds').val();
            grid_2_1_2.parameters['reportDate'] = $('#reportDate').val();
            grid_2_1_2.refresh(true);
        }
        function showReportData(objectId) {
            window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId + "&type=timelyRateInfo&provinceCode="+ $('#deptIds').val()+"&provinceName="+$('#deptNames').val()+"&reportDate="+ $('#reportDate').val();
        }

        function downReportData(objectId) {
            window.location.href = "<%=path%>/reportController.do?method=downReportData&objectId=" + objectId;
        }

        function showReportResult() {
            window.location.href = "<%=path%>/eemCommonController.do?method=queryReportResult";
        }

        function backToAnalysis(){
            location.href = "<%=path%>/base/page/countAnalysis.jsp";
        }
        function getDeptName() {
            __open_tree(this.id, 1, '派发树', function (selectedNodes) {
                var deptInf = getDeptInfo(selectedNodes);
                $('#deptIds').val(deptInf[0]);
                $('#deptNames').val(deptInf[1]);
            },'','','radio');
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
    </script>
</head>
<body>
<div id="__link_bar"><a href="javascript:;" onclick="backToAnalysis()">返回</a></div>
<div class="big-title-metar" style="margin-top: 20px">及时率分析明细</div>
<div class="container-fluid">
    <div class="col-xs-1 col-sm-1" style="padding: 0px;padding-left: 15px;">统计粒度：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
        <select id="reportDate" style="width: 100%">
            <option value="${yearStr}-上半年" <c:if test="${sel==1}">selected="selected"</c:if>>${yearStr}-上半年</option>
            <option value="${yearStr}-上半年" <c:if test="${sel==2}">selected="selected"</c:if>>${yearStr}-下半年</option>
            <option value="${yearStr-1}-上半年" <c:if test="${sel==3}">selected="selected"</c:if>>${yearStr-1}-上半年</option>
            <option value="${yearStr-1}-下半年" <c:if test="${sel==4}">selected="selected"</c:if>>${yearStr-1}-下半年</option>
            <option value="${yearStr-2}-上半年" <c:if test="${sel==5}">selected="selected"</c:if>>${yearStr-2}-上半年</option>
            <option value="${yearStr-2}-下半年" <c:if test="${sel==6}">selected="selected"</c:if>>${yearStr-2}-下半年</option>
            <%--<option value="${yearStr}-全年" <c:if test="${sel==5}">selected="selected"</c:if>>2016年全年</option>--%>
        </select>
    </div>
    <div class="col-xs-1 col-sm-1" style="padding: 0px;margin-left: 20px">上报省分：</div>
    <div class="col-xs-2 col-sm-2" style="padding: 0px">
        <%
            if(request.getAttribute("provinceCode")==null||"".equals(request.getAttribute("provinceCode"))){
                request.setAttribute("provinceCode",request.getParameter("provinceCode"));
                pageContext.setAttribute("provinceCode",request.getParameter("provinceCode"), pageContext.REQUEST_SCOPE);
                request.setAttribute("provinceName",request.getParameter("provinceName"));
            }
        %>
        <input type="hidden" name="deptIds" id="deptIds" value="${provinceCode}" />
        <input type="text" style="width: 90%" name="deptNames" id="deptNames" value="${provinceName}" onclick="getDeptName()"/>
    </div>
    <div class="btn btn-danger" style="margin-left: 15px;" id="collect_2_2_1">查询</div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</body>
</html>
