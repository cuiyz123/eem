<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<head>
    <title>草稿列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <style>

    </style>
</head>
<body>

<div class="big-title-metar">草稿查询</div>

<div id="dtGridContainer_2_1_2" class="dt-grid-container"></div>
<div id="dtGridToolBarContainer_2_1_2" class="dt-grid-toolbar-container"></div>
<script type="text/javascript">
    //映射内容
    var dtGridColumns_2_1_2 = [
        {
            id: 'theme',
            title: '工单主题',
            type: 'string',
            columnClass: 'text-center',
            columnStyle: 'width:250px',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return '<a href="<%=path%>/applyEditor_draft.do?reqId=' + record.reqId + '">' + value + '</a>';
            }
        },
        {
            id: '',
            title: '工单处理环节',
            type: 'string',
            columnClass: 'text-center',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                return '申请部门拟稿';
            }
        },
        {id: 'creationTime', title: '创建时间', type: 'date', columnClass: 'text-center'},
        {
            id: 'operation',
            title: '操作',
            type: 'string',
            columnClass: 'text-center',
            resolution: function (value, record, column, grid, dataNo, columnNo) {
                var content = '';
//                content += '<button class="btn btn-xs btn-default" onclick="alert(\'编辑用户：' + record.user_name + '\');"><i class="fa fa-edit"></i>  编辑</button>';
//                content += '  ';
                content += '<button class="btn btn-xs btn-danger" onclick="delDraft(\'' + record.reqId + '\' ,\'' + record.theme + '\');"><i class="fa fa-trash-o"></i>  删除</button>';
                return content;
            }
        }

    ];
    var dtGridOption_2_1_2 = {
        lang: 'zh-cn',
        ajaxLoad: true,
        loadURL: '<%=path%>/commDraftController.do?method=queryDraftList&entityName=WfDeployReq',
        exportFileName: '草稿列表',
        columns: dtGridColumns_2_1_2,
        gridContainer: 'dtGridContainer_2_1_2',
        toolbarContainer: 'dtGridToolBarContainer_2_1_2',
        tools: '',
        pageSize: 10,
        pageSizeLimit: [10, 20, 50]
    };
    var grid_2_1_2 = $.fn.DtGrid.init(dtGridOption_2_1_2);
    $(function () {
        grid_2_1_2.load();
    });

    function delDraft(entityId, theme) {
        if (confirm('确定要删除草稿' + theme + '吗?')) {
            $.ajax({
                url: '<%=path%>/commDraftController.do?method=delDraft&entityName=WfDeployReq&idProperty=&entityId=' + entityId,
                success: function (response) {
                    window.location.href = window.location.href;
                }
            });
        }
    }

</script>
</body>
</html>
