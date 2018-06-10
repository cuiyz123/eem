<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePage.jsp" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<head>
    <title>配置省级部门向省份上报的</title>
    <%--<script type="text/javascript" src="${pageContext.request.contextPath}/comm/js/jquery.form.js"></script>--%>
</head>
<body>
<div class="big-title-metar">配置省级部门</div>
<form id="disForm" method="post">

    <table class="table" table-form-6>

        <tr>
            <th>主送单位<font color=red>*</font></th>
            <td colspan="5">
                <div class="input-group">
                    <input type="hidden" class="form-control __metar_check_form"
                           name="attribute1" id="toDepartmentHidden"/>
                    <textarea type="text" name="attribute2" class="form-control __metar_check_form"
                        id="toDepartmentInput"  rows="4" readonly>${orgNames}</textarea>
                    <span id="__dispatch_tree_mainSent" class="input-group-addon glyphicon glyphicon-th"></span>
                </div>
            </td>
        </tr>

    </table>

    <div align="center" style="margin-top: 20px">
            <span class="btn btn-danger" id="submitDisForm" onclick="submitDisForm();">提交</span> &nbsp;&nbsp;
    </div>

</form>
</body>
<script type="text/javascript">
    $('#__dispatch_tree_mainSent').click(function () {
        __open_tree2(this.id,1, '组织树', function (selectedNodes) {
            var deptInf = getDeptInfo(selectedNodes);
            $('#toDepartmentHidden').val(deptInf[0]);
            $('#toDepartmentInput').val(deptInf[1]);
        });
    });
    $('#__dispatch_tree_copySent').click(function () {
        __open_tree(this.id, 3, '组织树', function (selectedNodes) {
            var names = '';
            var ids = '';
            for (var i = 0; i < selectedNodes.length; i++) {
                var isParent = selectedNodes[i].isParent;
//                if (isParent == false) {
                if(selectedNodes[i].type == 1){
                    ids = ids+selectedNodes[i].id+":ORG,";
                    names = names+selectedNodes[i].fullName+',';
                } else {
                    debugger;
                    ids = ids+selectedNodes[i].id+":MEMBER,";
                    names = names+selectedNodes[i].label+',';
                }

//                    names = names + selectedNodes[i].fullName + ','
//                    ids += selectedNodes[i].id + ":ORG,";
//                } else {
//                    names = '';
//                    ids = '';
//                }

            }
            if (names.length > 1 && ids.length > 1) {
                names = names.substring(0, names.length - 1);
                ids = ids.substring(0, ids.length - 1);
            }
            $('#copyToDepartment').val(ids);
            $('#copyToDepartmentNames').val(names);
        });
    });

    function getDeptInfo(selectedNodes) {
        var strDepName = "";
        var strDepId = "";
        for (var x = 0; x < selectedNodes.length; x++) {
            debugger;
            if (selectedNodes[x].type == 1){
                strDepId += selectedNodes[x].id + ":ORG,";
                strDepName += selectedNodes[x].fullName + ",";
            }
            else{
                strDepId += selectedNodes[x].id + ":MEMBER,";
                strDepName += selectedNodes[x].label + ",";
            }

        }
        strDepName = strDepName.substring(0, strDepName.length - 1);
        strDepId = strDepId.substring(0, strDepId.length - 1);
        var deptInf = new Array();
        deptInf[0] = strDepId;
        deptInf[1] = strDepName;
        return deptInf;
    }



    $('#contactPersonName__tree').click(function () {
        __open_tree(this.id, 2, '人员树', function (selectedNodes) {
            if ('' == selectedNodes) {
                $('#contactPersonName').val("");
                $('#taskContactInformation').val("");
            } else {
                $('#contactPersonName').val(selectedNodes[0].label);
                $('#taskContactInformation').val(selectedNodes[0].mobilePhone);
            }

        }, '', '', 'radio');
    });
    $('#nextDeal__tree').click(function () {
        __open_tree(this.id, 2, '人员树', function (selectedNodes) {
            if ('' == selectedNodes) {
                $('#nextDeal_show_name').val("");
                $('#nextDeal_show').val("");
            } else {
                $('#nextDeal_show_name').val(selectedNodes[0].userName);
                $('#nextDeal_show').val(selectedNodes[0].label);
            }

        }, '', '', 'radio');
    });



    //提交
    function submitDisForm() {

        if(""==$('#toDepartmentHidden').val()){
            alert("请选择部门！！！");
            return ;
        }
        __show_metar_loading();

        $.ajax({
            type: "POST",
            url: _PATH + "/eemCommonController.do?method=submitOrg",
            data: $('#disForm').serialize(),
            async: false,
            dataType: "json",
            success: function (response) {
                if (response.success) {
                    window.location.replace(_PATH + "/base/frame/todo.jsp");
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


    }


    function submitDisForm2() {
        __show_metar_loading();
        getTaskInstance();
        $.ajax({
            type: "POST",
            url: _PATH + "/dispatch.do?method=submitDispatch",
            data: $('#disForm').serialize(),
            async: false,
            dataType: "json",
            success: function (response) {
                if (response.success) {
                    window.location.replace(_PATH + "/base/page/refreshParentPage.jsp");
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
    }

    //保存
    function saveDisForm() {
        __show_metar_loading();
       // var url = "<%=request.getContextPath()%>/dispatch.do?method=saveDispatch";
        var url = "dispatch.do?method=saveDispatch";
        $('#disForm').attr("action", url);
        $('#disForm').submit();

    }
    ;
    //调度审核、签发，再次保存
    function saveDisForm2() {
        if (aa) {
            return;
        }
        /* if(!$('#theme').val()){
         alert('请填写调度单主题');
         $('#theme').focus();
         return;
         } */
        if (!_checkPhone(document.getElementById("disForm"))) {
            return;
        }

        if (!__metar_check_form(document.getElementById("disForm"))) {
            return;
        }
        if (!_checkDate(document.getElementById("disForm"))) {
            return;
        }
        getTaskInstance();
        aa++;
        $('#SPECIALITY_TYPE_id').attr("disabled", false);
        var url = "dispatch.do?method=SaveDispatch2";
        $('#disForm').attr("action", url);
        $('#disForm').submit();

    }
    ;


    function getTaskInstance() {
        var winParams = parent._winParams;
        if (winParams) {
            for (var key in winParams) {
                $('#disForm').append('<input type="hidden" name="' + key + '" value="' + winParams[key] + '"/>');
            }
        }
    }

    //字数限制
    window.onload = function () {
        limitLength('remarks');
        limitLength('taskSummary');
    }
    function limitLength(str) {
        document.getElementById(str).onkeydown = function () {
            if (this.value.length >= 500) {
                this.value = this.value.substr(0, 500)
            }
        }
    }

    function ajaxSave() {
        $("#disForm").ajaxSubmit({
            type: 'post',
            url: "${pageContext.request.contextPath}/dispatch.do?method=SubmitDispatch",
            dataType: 'Json',
            success: function (data) {
                if (data.success == true) {
                    debugger;
                    __hide_metar_loading();
                    __open_workNumber_window(data.workOrder, '调度单编号', data.url);
                    return;
//                    location.href="base/frame/todo.jsp";
                }
                if (data.success == false) {
                    alert("提交失败");
                    __hide_metar_loading();
                }
            },
            error: function (XmlHttpRequest, textStatus, errorThrown) {
                alert("提交失败");
                __hide_metar_loading();
            }

        });
    }
    function ajaxSave2() {
        $("#disForm").ajaxSubmit({
            type: 'post',
            url: "${pageContext.request.contextPath}/dispatch.do?method=SaveDispatch",
            dataType: 'Json',
            success: function (data) {
                if (data.success == true) {
                    __hide_metar_loading();
                    __open_workNumber_window(data.workOrder, '调度单编号', "base/page/dispatchDraftManage.jsp");
                    return;
//                    location.href="base/frame/todo.jsp";
                }
                if (data.success == false) {
                    alert("提交失败");
                    __hide_metar_loading();
                }
            },
            error: function (XmlHttpRequest, textStatus, errorThrown) {
                alert("提交失败");
                __hide_metar_loading();
            }

        });
    }

    function ssd(workNumbers, new_url) {
        __open_metar_window(workNumbers, '调度单编号：', 360, 130, function (window_body) {
            var workNumber = $('<label>调度单编号:' + workNumbers + '</label>');
            window_body.append(workNumber);
            __hide_metar_loading();

        }, function () {
            location.href = new_url;
        });
    }

</script>
</html>
