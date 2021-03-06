<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<head>
    <title>预约汇总</title>
    <style type="text/css">
        hr {
            margin: 0px 0px 10px;
            border: 1px solid #F00
        }
    </style>
</head>
<body>
<ul class="nav nav-tabs" style="position:relative;z-index: 1">
    <%
        Object object = request.getSession().getAttribute("globalUniqueUser");
        if (object != null) {
            UserEntity userEntity = (UserEntity) object;
            if ("UNI".equals(userEntity.getCategory())) {
    %>
    <li id="graph1" role="presentation" class="active" value="${one}"><a>当前周期</a></li>
    <li id="graph2" role="presentation" value="${two}"><a>上一周期</a></li>
    <li id="graph3" role="presentation" value="${three}"><a>前推第二周期</a></li>
    <li id="graph6" role="presentation" value="currentYear"><a>年度汇总</a></li>
    <%--jw 3.27--%>
    <%--<li id="graph8" role="presentation" value="lastYear"><a>上一年度汇总</a></li>--%>
        <%--jw 3.27--%>
    <%
    } else if ("PRO".equals(userEntity.getCategory())) {
    %>
    <li id="graph7" role="presentation" value="7"><a>年度汇总（省份的）</a></li>
    <%
            }
        }
    %>
</ul>
<br>

<br class="view_panel" id="record_view">
    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-1 col-sm-1" style="padding: 0px">
                全选 <input type="checkbox" id="check_all" name="__all_checkbox">
            </div>
            <input type="hidden" id="report_data" value="${one}">

            <%--<div class="btn btn-default" id="allDown">打包下载</div>--%>

            <div class="btn btn-danger" id="orderReportData">预约汇总</div>
            <div class="btn btn-danger" id="cancelReportData">取消预约汇总</div>
            <div class="btn btn-danger" style="float: right;margin-right:5px;" id="allDown">打包下载</div>
            <div class="btn btn-danger" style="float: right;margin-right:5px;" id="allZipDown">整体打包下载</div>
        </div>
    </div>
    <br/>
    <div id="dtGridContainer_2_1_2" class="dt-grid-container div0"></div>
    <div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
</div>
<script type="text/javascript">
    var __selectedFeedbacks = new Array();
    __show_metar_loading();
    var dtGridColumns_2_1_2 = [
        {
            id: 'fileName',
            title: '批量下载',
            type: 'string',
            columnClass: 'text-center',
            headerStyle: '15%',
            columnStyle:'15%',
            fastQueryType: 'lk',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return '<input type="checkbox" name="__feedback_processsInstID_checkbox" value="' + record.objectId + '"/>';
            }
        },
        {
            id: 'fileName',
            title: '文件名称',
            type: 'string',
            headerStyle: '25%',
            columnStyle:'25%',
            columnClass: 'text-center',
            fastQuery: true,
            fastQueryType: 'lk',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return "<a href='javascript:;' onclick='downReportData(" + record.objectId + ")'>"+"《"+value+"》"+"</a>";
            }
        },
        {
            id: 'creationTime',
            title: '更新时间',
            type: 'date',
            format: 'yyyy-MM-dd hh:mm:ss',
            columnClass: 'text-center',
            headerStyle: '15%',
            columnStyle:'15%',
            fastQuery: true,
            fastQueryType: 'range'
        },
        {id: 'dep', title: '汇总部门', type: 'string',  headerStyle: '15%',
            columnStyle:'15%', columnClass: 'text-center'},
        {id: 'depsWithDraw', title: '退回信息', type: 'string',  headerStyle: '5%',
            columnStyle:'5%', columnClass: 'text-center'}
    ];
    var dtGridOption_2_1_2 = {
        lang: 'zh-cn',
        ajaxLoad: true,
            loadURL: '<%=path%>/eemCommonController.do?method=queryReportExcel&reportData=' + encodeURIComponent(encodeURIComponent($("#report_data").val())),
        exportFileName: '汇总文件列表',
        columns: dtGridColumns_2_1_2,
        gridContainer: 'dtGridContainer_2_1_2',
        toolbarContainer: 'dtGridToolBarContainer_2_1_2',
        tools: 'refresh',
        pageSize: 10,
        pageSizeLimit: [10, 20, 50, 100]
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
        grid_2_1_2.load(function () {
            __hide_metar_loading();
            if (${isOrder}) {
                $("#cancelReportData").attr('disabled', 'disabled');
                $("#orderReportData").removeAttr('disabled');
            } else {
                $("#orderReportData").attr('disabled', 'disabled');
                $("#cancelReportData").removeAttr('disabled');
            }
        });
        $('#orderReportData').click(orderReportData);
        $('#cancelReportData').click(cancelOrder);
        $('#allDown').click(allDown);
        $('#allZipDown').click(allZipDown);
        $('.nav li').click(function () {
            var __this = $(this);
            $("#report_data").val(this.innerText);
            if (!__this.hasClass('active')) {
                $('.nav li').removeClass('active');
                __this.addClass('active');
            }
            $.ajax({
                type: "post",
                dataType: 'Json',
                url: "<%=path%>/eemCommonController.do?method=isButton&order=" + encodeURIComponent(encodeURIComponent(this.innerText)),
                async: false,
                success: function (obj) {
                    if (!obj.success) {
                        alert(obj.msg);
                    } else {
                        debugger
                        if (obj.msg) {
                            $("#cancelReportData").attr('disabled', 'disabled');
                            $("#orderReportData").removeAttr('disabled');

                        } else {
                            $("#orderReportData").attr('disabled', 'disabled');
                            $("#cancelReportData").removeAttr('disabled');
                        }
                    }
                },
                error: function (request) {
                    alert("加载失败!");
                }
            });

            grid_2_1_2.parameters = new Object();
//            grid_2_1_2.parameters['reportData']  = encodeURIComponent(encodeURIComponent($("#report_data").val()));
            grid_2_1_2.parameters['reportData'] = $("#report_data").val();
            grid_2_1_2.refresh(true);

        });

        $("#check_all").click(function () {
            if ($(this).attr("checked")) {
                $("input[type=checkbox]").each(function () {
                    $(this).attr("checked", true);
                });
            } else {
                $("input[type=checkbox]").each(function () {
                    $(this).attr("checked", false);
                });
            }
        });
    });


    function orderReportData() {
        var reportData = $("#report_data").val();
        $.ajax({
            type: "post",
            dataType: 'Json',
            url: "<%=path%>/eemCommonController.do?method=OrderReportDate&reportData=" + encodeURIComponent(encodeURIComponent(reportData)),
            async: false,
            success: function (obj) {
                if (!obj.success) {
                    alert(obj.msg);
//                    __hide_metar_loading();
                } else {
                    __hide_metar_loading();
                    alert("您已选择汇总" + reportData + "数据，为了不影响服务器的效率\n系统会在晚上00:30 自动进行汇总您可以在明天白\n天进行下载！");
                    $("#orderReportData").attr('disabled', 'disabled');
                    $("#cancelReportData").removeAttr('disabled');
//                    $("#cancelReportData").style.display = 'display';
                    grid_2_1_2.refresh(true);
                }
            },
            error: function (request) {
                alert("加载失败!");
            }
        });
    }

    function cancelOrder() {
        var reportData = $("#report_data").val();
        $.ajax({
            type: "post",
            dataType: 'Json',
            url: "<%=path%>/eemCommonController.do?method=cancelOrderReport&reportData=" + encodeURIComponent(encodeURIComponent(reportData)),
            async: false,
            success: function (obj) {
                if (!obj.success) {
                    alert(obj.msg);
//                    __hide_metar_loading();
                } else {
                    __hide_metar_loading();
                    alert("您已取消汇总" + reportData + "数据成功！");
                    $("#orderReportData").removeAttr('disabled');
                    $("#cancelReportData").attr('disabled', 'disabled');
                    grid_2_1_2.refresh(true);
                }
            },
            error: function (request) {
                alert("取消预约汇总失败");
            }
        });
    }

    function allDown() {
        __selectedFeedbacks = new Array();
        var feedbacks = $('input[name="__feedback_processsInstID_checkbox"]');
        var objectId = "";
        for (var i = 0; i < feedbacks.length; i++) {
            if (feedbacks[i].checked) {
                __selectedFeedbacks.push({processInstID: feedbacks[i].value});
            }
        }
        if (__selectedFeedbacks.length == 0) {
            alert('请选择下载的数据！');
            return;
        }
        for (var i = 0; i < __selectedFeedbacks.length; i++) {
            objectId += __selectedFeedbacks[i]['processInstID'] + ',';
//            if (i < __selectedFeedbacks.length - 1) {
//                objectId += ',';
//            }
        }
        window.location.href = "<%=path%>/eemCommonController.do?method=downReportExcel&eid=" + objectId + "&param=appoint";
    }
    function allZipDown() {
        var reportData = $("#report_data").val();
        $.ajax({
            type: "post",
            dataType: 'Json',
            url: "<%=path%>/eemCommonController.do?method=checkDownReportExcel&reportData=" + encodeURIComponent(encodeURIComponent(reportData)),
            async: false,
            success: function (obj) {
                if (!obj.success) {
                    alert(obj.msg);
                } else {
                    var r = confirm("确定要整体打包下载" + reportData + "数据？")
                    if (r) {
                        window.location.href = "<%=path%>/eemCommonController.do?method=downAllCollectExcel&reportData=" + encodeURIComponent(encodeURIComponent(reportData));
                    }
                }
            },
            error: function (request) {
                alert("验证失败");
            }
        });

    }
    function downReportData(objectId) {
        window.location.href = "<%=path%>/eemCommonController.do?method=downReportExcel&eid=" + objectId + "&param=appoint";
    }
    function customSearch_2_2_3() {
        grid_2_1_2.parameters = new Object();
        grid_2_1_2.parameters['reportData'] = $("#report_data").val();
        grid_2_1_2.refresh(true);
    }
</script>
</body>
</html>
