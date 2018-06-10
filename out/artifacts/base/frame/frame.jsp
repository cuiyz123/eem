<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<jsp:include page="../basePage.jsp"/>
<%
    String __fromPage = request.getParameter("fromPage");
    String __area = request.getParameter("__area");
    if(__area != null && !"".equals(__area)){
        String __taskInstance = "{}";
        request.setAttribute("__buildJSON" , java.net.URLDecoder.decode(__area , "UTF8"));
        request.setAttribute("__taskInstance" , __taskInstance);
    }
%>
<html>
<head>
    <style>

    </style>
</head>
<body>
<div id="__link_bar"></div>
<div id="__page_big_title" class="big-title-metar"></div>
<div id="__areaName_list"></div>
<div id="__component_container"></div>
<div id="__link_container"></div>
<%--<jsp:include page="../page/receiveForm.jsp"></jsp:include>--%>
<%--${__buildJSON}--%>
<%--${__taskInstance}--%>

<script>
var _winParams={};
_winParams.__returnUrl = '<%=request.getParameter("__returnUrl")%>';
if('${__taskInstance}' != '{}'){
    _winParams.taskInst=${__taskInstance};
    _winParams.activityInstName=_winParams.taskInst.activityInstName||null;
    _winParams.activityInstID=_winParams.taskInst.activityInstID;
    _winParams.activityDefID=_winParams.taskInst.activityDefID||null;
    _winParams.processingChainId=_winParams.taskInst.processingChainId||null;
    _winParams.processModelId=_winParams.taskInst.processModelId||null;
    _winParams.processModelName=_winParams.taskInst.processModelName;
    _winParams.processInstID=_winParams.taskInst.processInstID||null;
    _winParams.taskInstID=_winParams.taskInst.taskInstID||'';
    _winParams.shard=_winParams.taskInst.shard||null;
//新增
    _winParams.jobCode=_winParams.taskInst.jobCode||'';
    _winParams.jobID=_winParams.taskInst.jobID||'';
    _winParams.numColumn1=_winParams.taskInst.numColumn1||0;
    _winParams.productcode=_winParams.taskInst.productcode||null;
    _winParams.majorcode=_winParams.taskInst.majorcode||null;
    _winParams.businessId=_winParams.taskInst.businessId||null;
    _winParams.businessId=_winParams.taskInst.businessId||null;
    _winParams.currentState=_winParams.taskInst.currentState||null;
    _winParams.rootProcessInstId=_winParams.taskInst.rootProcessInstId||null;
    _winParams.businessCode=_winParams.businessCode||null;
    _winParams.createDate=_winParams.taskInst.createDate||null;
    //用于存储汇总组编号
    _winParams.strColumn4=_winParams.taskInst.strColumn4||null;
    delete _winParams.taskInst;

}


var __$__processingObjectId = 0;
var __$__processingObjectTable = 0;

var TASKLIST;

var __$__last_processingObjectId = 0;
var __$__last_processingObjectTable = 0;

var __buildJSON = eval('(${__buildJSON})');
var __links = __buildJSON.links;
if(__links){
    for(var i = 0 ; i < __links.length ; i++){
        var __link = __links[i];
        var __linkDataArray = __link.split('@');
        var __action = $('<a>' + __linkDataArray[0] + '</a>');
        var __link_file = __link_name = __linkDataArray[0];
        if(__linkDataArray.length == 2){
            __link_file = __linkDataArray[1];
        }
        __bindAction2Link(__link_name , __action , __link_file);
        $('#__link_bar').append(__action);
    }
    var __back_action = $('<a>返回</a>');
    __back_action.click(function(){
        __exitFromFrame();
    });
    $('#__link_bar').append(__back_action);
} else {
    var __close_action = $('<a>关闭</a>');
    __close_action.click(function(){
        window.close();
    });
    $('#__link_bar').append(__close_action);
}

var __areas = __buildJSON.areas;
var __componentModels;
var __areaName;
var __areaName_div;
if(__areas){
    for(var i in __areas){
        __areaName = __areas[i].areaName;
        __componentModels = __areas[i].componentModels;
        if(__componentModels.length > 0){
            var __componentModel = __componentModels[__componentModels.length - 1];
            __areaName_div = $('<div><a href="'+_PATH+'/base/frame/frame.jsp?__area=' + encodeURIComponent(encodeURIComponent(JsonObjectToString(__areas[i]))) + '" target="_blank">' + __areaName + '</a></div>');
        } else {
            __areaName_div = $('<div>' + __areaName + '</div>');
        }
        $('#__areaName_list').append(__areaName_div);
    }
} else {
    __areaName = __buildJSON.areaName;
    __componentModels = __buildJSON.componentModels;
    if(__componentModels.length > 0){
        var __componentModel = __componentModels[__componentModels.length - 1];
        __areaName_div = $('<div><a href="'+_PATH+'/base/frame/frame.jsp?__area=' + encodeURIComponent(JsonObjectToString(__buildJSON)) + '" target="_blank">' + __areaName + '</a></div>');
    } else {
        __areaName_div = $('<div>' + __areaName + '</div>');
    }
    $('#__areaName_list').append(__areaName_div);
}

__areaName_div.empty().append($('<div>' + __areaName + '</div>'));
$('#__page_big_title').append(__areaName);

if(__componentModels){
    for(var __i = 0 ; __i < __componentModels.length ; __i++){
        var componentModel = __componentModels[__i];
        var __componentDiv = $('<div></div>');
        $('#__component_container').append(__componentDiv);
        var __component_name = componentModel.component;
        if(__component_name.indexOf(".do") > -1){
            __componentDiv.load(_PATH + '/' + __component_name + '?strColumn4='+_winParams.strColumn4+'&type=' + componentModel.type + '&component=' + componentModel.component + '&activityDefID=' + componentModel.activityDefID + '&processInstID=' + componentModel.processInstID + '&fromPage=<%=__fromPage%>' + '&currentProcessInstID=' + _winParams.processInstID + '&taskInstID=' + _winParams.taskInstID);
        } else {
            __componentDiv.load(_PATH + '/base/page/' + __component_name + '.jsp?type=' + componentModel.type + '&component=' + componentModel.component + '&activityDefID=' + componentModel.activityDefID + '&processInstID=' + componentModel.processInstID + '&fromPage=<%=__fromPage%>' + '&currentProcessInstID=' + _winParams.processInstID + '&taskInstID=' + _winParams.taskInstID);
        }
    }
}


function __bindAction2Link(__link_name , __action , __link_file){
    require('base/js/' + __link_file + '.js' , function(){
        var __link_dialog_show = true;
        __action.bind('click' , function(){
            var __link_dialog = $('#__' + __link_file + '_container');
            if(!document.getElementById('__' + __link_file + '_container') || !__link_dialog_show){
                __link_dialog = $('<div id="__' + __link_file + '_container" class="__link_dialog_container modal"></div>');
                var __link_dialog_header = $('<div class="modal-header">' + __link_name + '</div>');
                var __link_dialog_header_close_btn = $('<div class="close">×</div>');
                __link_dialog_header_close_btn.bind('click' , function(){
//                    debugger;
//                    alert(__link_dialog instanceof jQuery);
                    __link_dialog.modal('hide');
                    if(__link_dialog.css('display') != 'none'){
                        $(__link_dialog).modal('hide');
                    }
                });
                __link_dialog_header.append(__link_dialog_header_close_btn);
                __link_dialog.append(__link_dialog_header);
                __link_dialog.append($('<div class="modal-body" id="__' + __link_file+ '_dialog_body"></div>'));
                $('body').append(__link_dialog);
                if(eval(__link_file + '(\'__' + __link_file+ '_dialog_body\')') == false){
                    __link_dialog_show = false;
                }
                drag(__link_dialog.get(0) , __link_dialog_header.get(0));
            }
//            $('#__' + __link_file + '_container').show();
            if(__link_dialog_show){
                __link_dialog.modal({backdrop: 'static',show:true});
            }
        });
    });
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

</script>
</body>
</html>
