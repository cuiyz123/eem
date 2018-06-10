<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<html>
<head>
    <title>设备类型管理</title>
    <style>
        div.left {
            margin: 0;
            padding: 0;
            float: left;
            width: 28%;
            height: 100%;
            overflow-y:scroll;
        }
        #treeDemo{
            margin-top:25px;
            margin-bottom:25px;
        }
        div.right {
            float: right;
            width: 71%;
        }
        #topSearch{
            position:fixed;
            top:0;
            left:0;
            width:28%;
            z-index: 1000;
        }
        #bottomFunc{
            position:fixed;
            bottom:0;
            left:0;
            width:28%;
        }
    </style>
    <script type="text/javascript">
        <!--
        var setting = {
            view: {
                selectedMulti: false
            },
            edit: {
                enable: true,
                showRemoveBtn: false,
                showRenameBtn: false
            },
            async:{
                autoParam:["objectId"],
                contentType:"application/x-www-form-urlencoded",
                enable:true,
                type:"post",
                url:"<%=path%>/eemDeviceTypeController.do?method=queryDeviceTypeList&id="
            },
            data: {
                simpleData: {
                    enable:true,
                    idKey: "objectId",
                    pIdKey: "parentID",
                    isParent:"parent",
                    rootPId: -1
                }
            },
            callback: {
                beforeClick:function(treeId, treeNode) {
                    $("#name_id").val(treeNode.name);
                    $("#shortName").val(treeNode.shortName);
                    $("#nodeType").val(treeNode.nodeType);
                    $("#vendorName").val(treeNode.vendorName);
                    $("#parentNodeName").val(treeNode.parentNodeName);
                    $("#remark").val(treeNode.remark);
                    $("#lastEditPerson").val(treeNode.lastEditPerson);
                    $("#lastEditDate").val(treeNode.lastEditDate);
                    changeInputReadOnly(true);
                    $("#buttons").hide();
                }
            }
        };
        //增加
        function add(e) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getSelectedNodes(),
                    treeNode = nodes[0];
            if (nodes.length == 0) {
                alert("请先选择一个节点");
                return;
            }
            $("#parentNodeId").val(treeNode.objectId);
            $("#parentNodeName").val(treeNode.name);
            $("#objectId").val("");
            $("#name_id").val("");
            $("#shortName").val("");
            $("#nodeType").val("");
            $("#vendorName").val("");
            $("#remark").val("");
            $("#lastEditPerson").val("");
            $("#lastEditDate").val("");
            changeInputReadOnly(false);
            $("#buttons").show();
        }
        //编辑
        function edit() {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getSelectedNodes(),
                    treeNode = nodes[0];
            if (nodes.length == 0) {
                alert("请先选择一个节点");
                return;
            }
            $("#objectId").val(treeNode.objectId);
            $("#name_id").val(treeNode.name);
            $("#shortName").val(treeNode.shortName);
            $("#nodeType").val(treeNode.nodeType);
            $("#vendorName").val(treeNode.vendorName);
            $("#parentNodeId").val(treeNode.parentID);
            $("#parentNodeName").val(treeNode.parentNodeName);
            $("#remark").val(treeNode.remark);
            $("#lastEditPerson").val(treeNode.lastEditPerson);
            $("#lastEditDate").val(treeNode.lastEditDate);
            changeInputReadOnly(false);
            $("#buttons").show();
        }
        //删除
        function remove(e) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getSelectedNodes(),
                    treeNode = nodes[0];
            if (nodes.length == 0) {
                alert("请先选择一个节点");
                return;
            }
            if(treeNode.objectId==-1){
                alert("根目录不能删除");
                return;
            }
            var r = confirm("确定要删除所选设备类型？");
            if(r){
                var flag = false;
                if(treeNode.isParent){
                    var rc = confirm("该节点下存在子节点，删除此节点将同步删除子节点，确定删除？");
                    if(rc){
                        flag = true;
                    }
                }else{
                    flag = true;
                }
                if(flag){
                    $.ajax({
                        type: "POST",
                        url: "<%=path%>/eemDeviceTypeController.do?method=deleteDeviceType",
                        data: {objectId:treeNode.objectId},
                        async: false,
                        success: function (data) {
                            var object = $.parseJSON(data);
                            if (object.success) {
                                alert("删除成功");
                                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                                var nodes = treeObj.getSelectedNodes();
                                if (nodes.length>0) {
                                    if(nodes[0].isParent){
                                        treeObj.reAsyncChildNodes(nodes[0], "refresh");
                                    }else{
                                        treeObj.reAsyncChildNodes(null, "refresh");
                                    }
                                }else{
                                    treeObj.reAsyncChildNodes(null, "refresh");
                                }
                                $("#buttons").hide();
                            }else{
                                alert(object.msg);
                            }
                        },
                        error: function (request) {
                            alert("删除失败");
                        }
                    });
                }
            }
        }
        $(document).ready(function () {
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("#name_id").val("根目录");
            $("#remark").val("此节点不能删除");
            $("#addLeaf").bind("click", {isParent: false}, add);
            $("#edit").bind("click", edit);
            $("#remove").bind("click", remove);
            $("#buttons").hide();
            changeInputReadOnly(true);

        });
        function changeInputReadOnly(v){
            $("#name_id").attr("readOnly",v);
            $("#shortName").attr("readOnly",v);
            $("#nodeType").attr("readOnly",v);
            $("#vendorName").attr("readOnly",v);
            $("#remark").attr("readOnly",v);
            $("#lastEditPerson").attr("readOnly",v);
            $("#lastEditDate").attr("readOnly",v);
        }
        function saveDeviceType(){
            $.ajax({
                type: "POST",
                url: "<%=path%>/eemDeviceTypeController.do?method=saveDeviceType",
                data: $('#deviceTypeForm').serialize(),
                async: false,
                success: function (data) {
                    var object = $.parseJSON(data);
                    if (object.success) {
                        alert("保存成功");
                        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                        var nodes = treeObj.getSelectedNodes();
                        if (nodes.length>0) {
                            if(nodes[0].isParent){
                                treeObj.reAsyncChildNodes(nodes[0], "refresh");
                            }else{
                                treeObj.reAsyncChildNodes(null, "refresh");
                            }
                        }else{
                            treeObj.reAsyncChildNodes(null, "refresh");
                        }
                        $("#buttons").hide();
                    }else{
                        alert(object.msg);
                    }
                },
                error: function (request) {
                    alert("保存失败");
                }
            });
        }
        function searchBtn(){
            var param = $.trim($("#searchInput").val());
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var node = treeObj.getNodeByParam("id", 0, null);
            if(param != ""){
//                param = encodeURI(encodeURI(param));
                treeObj.setting.async.otherParam=["param", param];
            }else {
                //搜索参数为空时必须将参数数组设为空
                treeObj.setting.async.otherParam=[];
            }
            treeObj.reAsyncChildNodes(node, "refresh");
        }
        //-->
    </script>
</head>
<body>
<div class="left">
    <div id="topSearch">
    <input type="text" style="width: 70%" id="searchInput"><span onclick="searchBtn()" class="btn btn-danger">搜索</span>
    </div>
    <ul id="treeDemo" class="ztree"></ul>
    <%
        Object object = request.getSession().getAttribute("globalUniqueUser");
        if(object!=null){
            UserEntity userEntity = (UserEntity)object;
            if(("UNI".equals(userEntity.getCategory())&&userEntity.getAdmin())||"root".equals(userEntity.getUserName())){
    %>
    <div id="bottomFunc">&nbsp;&nbsp;<span id="addLeaf" class="btn btn-danger">新增节点</span>&nbsp;&nbsp;<span id="edit" class="btn btn-danger">编辑节点</span>&nbsp;&nbsp;<span id="remove" class="btn btn-danger">删除节点</span></div>
    <%
            }
        }
    %>
</div>
<div class="right">
    <div class="big-title-metar">设备类型管理</div>
    <form action="" method="post" id="deviceTypeForm">
        <table class="table">
            <tr>
                <th>节点名称</th>
                <td><input type="text" id="name_id" name="name" style="width: 100%"><input type="hidden" id="objectId" name="objectId"></td>
                <th>字母简称</th>
                <td><input type="text" id="shortName" name="shortName" style="width: 100%"></td>
            </tr>
            <tr>
                <th>节点类型</th>
                <td><input type="text" id="nodeType" name="nodeType" style="width: 100%"></td>
                <th>父节点</th>
                <td><input type="text" id="parentNodeName" name="parentNodeName" style="width: 100%" readonly><input type="hidden" id="parentNodeId" name="parentID"></td>
            </tr>
            <tr>
                <th>生产厂商</th>
                <td colspan="3"><input type="text" id="vendorName" name="vendorName" style="width: 100%"></td>
            </tr>
            <tr>
                <th>备注</th>
                <td colspan="3"><textarea id="remark" name="remark" style="width: 100%"></textarea></td>
            </tr>
            <tr>
                <th>最后编辑人</th>
                <td><input type="text" id="lastEditPerson" style="width: 100%"></td>
                <th>最后编辑时间</th>
                <td><input type="text" id="lastEditDate" style="width: 100%"></td>
            </tr>
        </table>
        <div style="text-align: center" id="buttons">
            <span class="btn btn-danger" onclick="saveDeviceType()">保存</span>&nbsp;&nbsp;<span class="btn btn-danger">重置</span>
        </div>
    </form>
</div>
</body>
</html>
