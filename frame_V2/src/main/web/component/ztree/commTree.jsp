<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<jsp:include page="../../base/basePage.jsp"/>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Insert title here方法</title>
    <script type="text/javascript" >
        var __tree;
        var arr=[];
        var key=[];
        var value=[];
        var Node=[];
        function getMenu(){
            for (var i = 0; i < data.length; i++) {
                Node[i] = { id: i + 1, pId: 0, name: data[i].GradeName, open: true };
                Node[i].bookNodes=[];
                for (var j = 0; j < data[i].Books.length; j++) {
                    Node[i].bookNodes[j]={ id: 111, pId: i + 1, name: data[i].Books[j].BookName, open: true};
                }
            }
            return Node;
        } ;
        var setting = {

            check: { /**复选框**/
            nocheckInherit: false,
                enable: true,
                chkStyle: "checkbox",
                chkboxType: { "Y": "ps", "N": "ps" }
            },
            view: {
                //dblClickExpand: false,
                expandSpeed: 300 //设置树展开的动画速度，IE6下面没效果，
            },
            async:{
                autoParam:["id"],
                contentType:"application/x-www-form-urlencoded",
                enable:true,
                type:"post",
                url:"<%=path%>/commTreeController.do?method=createDisPatchTree2&type=<%=request.getParameter("type")%>"
            },

            data: {
                key:{
                    name:"label"
                } ,
                simpleData: {
                    enable:true,
                    idKey: "id",
                    pIdKey: "parentId",
                    isParent:"parent",
                    rootPId: ""
                }
            },
            callback: {
                // onClick:zTreeBeforeClick,
                onAsyncSuccess: onAsyncSuccess,
                beforeClick: beforeClick
            }
        };
        function onAsyncSuccess(event, treeId, treeNode, msg) {
            /*    debugger;
             var zTreeObj = $.fn.zTree.getZTreeObj("treeDemo")
             var zTree = zTreeObj.getCheckedNodes(false);
             for (var i = 0; i < zTree.length; i++) {
             if (zTree[i].isParent) {
             zTreeObj.checkNode(zTree[i], true);
             }
             } */

        }
        function beforeClick(treeId, treeNode) {
            //var zTree = $.fn.zTree.getZTreeObj("tree");
            // userFrame.attr("src",treeNode.file);;
        }
        function onCheck(e, treeId, treeNode) {
            // alert("onCheck");
        }
        function zTreeBeforeClick() {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = treeObj.getSelectedNodes();
            for (var i = 0, l = nodes.length; i < l; i++) {
                treeObj.checkNode(nodes[i], true, true);
            }
        }
        var citynodes = [      /**自定义的数据源，ztree支持json,数组，xml等格式的**/  ];

        $(document).ready(function(){//初始化ztree对象

            __tree = $.fn.zTree.init($("#tree"), setting);
            /*  $.post("<%=path%>/commTreeController.do?method=createDisPatchTree2&type=<%=request.getParameter("type")%>",function(){
             $.fn.zTree.init($("#tree"), setting);
             });*/
            // var zTreeDemo = $.fn.zTree.init($("#cityTree"),setting, citynodes);
        });


        function chose_confirm(){
//            console.info('getSelectedNodes================' + __tree.getSelectedNodes());
//            console.info('getCheckedNodes=================' + __tree.getCheckedNodes());
            return;
            var treeObj = $.fn.zTree.getZTreeObj("tree");
            var num = treeObj.getCheckedNodes(true);
            for(var i=0;i<num.length;i++){
                debugger;
                if(num[i].level==1){
                    alert(num[i].id+"   "+num[i].label);
                    var v=num[i].label;
                    var k=num[i].id;
                    value.push(v);
                    key.push(k);
                }
                // var nn="{'"+num[i].id+"':'"+num[i].name+"'}";
                // arr.push(nn);
            }
            window.opener.returnName(value);
            window.close();

        }
        function chose_reset(n){
            var i;
            for(i=1;i<=n;i++)
            {
                eval("document.all.check"+i).checked=false;
            }

        }
        function hid(){

            document.getElementById("tre").hide();

        }

        function queryPerson(){
            alert("sdfsfsd")   ;

            $.ajax({

                url : '<%=path%>/commTreeController.do?method=queryPerson',
                method : 'POST',
                async : true,
                dataType : 'json',
                data : {name : document.getElementById("per").value},

                success: function (msg) {
                    alert(document.getElementById("per").value)  ,
                            msg = eval('(' + msg + ')'); //转为json对象

                    var data = msg.data;
                    for (var i = 0, j = data.length; i < j; i++)
                        $('#person').append('<input name="id" type="checkbox"  > ' + data[i].orgId + '|' + data[i].trueName + '</input><br>');
                },
                error: function () {
                    alert("error");
                }
            })
        }



    </script>


</head>
<body>
<checkbox ></checkbox>
人员查询 :<input id="per" value="请输入姓名" onchange="queryPerson()"   >
<input type="button" value="查询" onclick="hid()"> <br>
<div id="person" ></div>
<div id="tre" >
    <form name="dep_form" method="post">

        <table border=0 cellspacing=0 cellpadding=0 width='100%'>
            <tr>
                <td  class="form_name1" style='text-align:left'><b> 部门选择 </b></td>
                <td  class="form_name1" style="text-align:right">


                    <input type=button class=button value=确定 onclick="JavaScript:chose_confirm()" style='width:50'>&nbsp;&nbsp;
                    <%-- <input	type=button class=button value=重置 onclick="JavaScript:chose_reset()" style='width:50'>--%>
                </td>
            </tr>
            <tr>
                <td colspan="2" >
                    <div id="treeDiv">
                        <ul id="tree" class="ztree"></ul>
                    </div>
                </td>
            </tr>
        </table>

        <br>
        <br>

    </form>
</div>

</body>
</html>