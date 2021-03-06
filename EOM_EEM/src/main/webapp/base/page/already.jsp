<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>已办列表</title>
    <script type="text/javascript">
        __show_metar_loading();
        //映射内容
        var dtGridColumns_2_1_2 = [
            {
                id: 'tpinputname',
                title: '文件名称',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "《"+value+"-"+record.operorgname+"》已审核";
                }
            },
            {
                id: 'operusertruename',
                title: '上报人',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
            },
            {
                id: 'operorgname',
                title: '上报人部门',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'operuserphone',
                title: '电话',
                type: 'string',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
            },
            {
                id: 'dateGrading',
                title: '统计粒度',
                headerStyle: '15%',
                columnStyle:'15%',
                type: 'string',
                columnClass: 'text-center'
            },
            {
                id: 'creationtime',
                title: '上报时间',
                type: 'date',
                format:'yyyy-MM-dd hh:mm:ss',
                headerStyle: '15%',
                columnStyle:'15%',
                columnClass: 'text-center'
//                headerStyle: 'width:160px'
            },{
                id: 'processing_status',
                title: '审核状态',
                type: 'string',
//                headerStyle: 'width:80px',
                headerStyle: '5%',
                columnStyle:'5%',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    if(value=="Y"){
                        return "通过";
                    }else if(value=="N"){
                        return "驳回";
                    }else{
                        return "";
                    }
                }
            },
            {
                id: 'audit_info',
                title: '审核意见',
                type: 'string',
                headerStyle: '10%',
                columnStyle:'10%'
//                headerStyle: 'width:200px'
            },
            {
                id: 'objectid',
                title: '操作',
                type: 'string',
//                headerStyle: 'width:80px',
                headerStyle: '5%',
                columnStyle:'5%',
                columnClass: 'text-center',
                resolution: function (value, record, column, grid, dataNo, columnNo) {
                    return "<a href='javascript:;' onclick='showReportData("+value+")'>查看</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='javascript:;' onclick='downReportData("+value+")'>下载</a>";
                }}
        ];
        var dtGridOption_2_1_2 = {
            lang : 'zh-cn',
            ajaxLoad : true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryDataList&type=already',
            exportFileName : '数据查询列表',
            columns : dtGridColumns_2_1_2,
            gridContainer : 'dtGridContainer_2_1_2',
            toolbarContainer : 'dtGridToolBarContainer_2_1_2',
            tools:'refresh',
            pageSize : 50,
            pageSizeLimit : [10, 20, 50]
        };
        var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
        $(function () {
            grid_2_1_2.load(function(){
                __hide_metar_loading();
            });
            $('#custom_search_2_2_3').click(customSearch_2_2_3);
        });
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
        function showReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=showReportData&objectId=" + objectId+"&type=already";
        }
        function downReportData(objectId){
            window.location.href = "<%=path%>/reportController.do?method=downReportData&objectId=" + objectId;
        }
    </script>
</head>
<body>
<input type="hidden" id="tempIds" value="${tempIds}">
<div class="big-title-metar">已办查询</div>
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
    </div>
</div>
<div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>

</body>
</html>
