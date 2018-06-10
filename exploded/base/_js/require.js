//@ sourceURL=require.js
function require(__linkName, callback) {
    $.ajax({
        url: __linkName,
        method: 'POST',
        async: true,
        success: function (response) {
            $('body').append('<script>' + response + '</script>');
            if (callback) {
                callback();
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    })
}

/*---初始化上传附件组件---*/
/**
 *
 * @param __file_uploader       type=file的input
 * @param __flowingObjectId     附件关联的对象ID
 * @param __flowingObjectTable  附件关联的对象表名
 * @param __type                模式，edit:编辑模式，可以上传附件     detail（默认）:只读模式，只显示附件列表
 * @param __callback            回调函数
 * @param iscounter             附件列表是否添加计数，Y（默认）：计数     N：不计数
 * @private
 */
function __init_attachment_function(__file_uploader, __flowingObjectId, __flowingObjectTable, __type, __callback , iscounter) {
    var __file_uploader_file_list_container = $('<ul class="__metar_attachment_list"></ul>');
    if(iscounter == 'N'){
        __file_uploader_file_list_container = $('<ul class="__metar_attachment_list_nocounter"></ul>');
    }
    $('#' + __file_uploader).parent().prepend(__file_uploader_file_list_container);
    $.ajax({
        url: _PATH + '/attachment.do?method=query',
        method: 'POST',
        async: true,
        dataType: 'json',
        data: {flowingFlag: 'Y', jsonData: '[{flowingObjectId:"' + __flowingObjectId + '" , flowingObjectTable:"' + __flowingObjectTable + '" , flowingObjectShardingId:"1"}]'},
        success: function (response) {
            if (response) {
                var __attachmentList = response.attachmentList;
                for (var i = 0; i < __attachmentList.length; i++) {
                    var __attachment_data = __attachmentList[i];
                    var __attachmentName = __attachment_data.attachmentName;
                    if (__type == 'edit') {
                        __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
                            '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>&nbsp;' +
                            '<a onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\')">删除</a>' +
                            '</li>'));
                    } else {
                        __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
                            '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>&nbsp;' +
                            '</li>'));
                    }

                }
                if (__callback) {
                    eval(__callback + '()');
                }

            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    });
    if (__type == 'edit') {
        $('#' + __file_uploader).uploadify({
            'swf': _PATH + '/base/_resources/uploadify.swf',
            'cancelImg': _PATH + '/base/_resources/uploadify-cancel.png',
            'uploader': _PATH + '/attachment.do?method=upload&globalUniqueID=' + _globalUniqueID + '&flowingFlag=Y&flowingObjectId=' + __flowingObjectId + '&flowingObjectTable=' + __flowingObjectTable + '&flowingObjectShardingId=1&attachmentTypeEnumId=1&attachmentFormatEnumId=1&shardingId=1&activityInstanceId=1&taskInstanceId=1&',
            'buttonText': '上传文件',
            'method': 'get',
            'auto': true,
            'fileObjName': 'uploadFiles',
            'multi': true,
            'simUploadLimit': 10,
            'onSelect': function (event, ID, fileObj) {
//                    alert('上传中...');
            },
            'onFallback': function () {
                alert("您未安装FLASH控件，无法上传图片！请安装FLASH控件后再试。");
            },
            'onUploadStart':function(fileObj){
              if(fileObj.size>12582912){
                  alert("您上传的文件大于12M,请重新上传！");
                  $('#' + __file_uploader).uploadify('stop');
                  $('#' + __file_uploader).uploadify('cancel', '*');
                  return ;
              }
            },
            'onUploadSuccess': function (file, data, response) {
                var returnData = eval('(' + data + ')');
                if (returnData.success) {
                    debugger;
                    var __attachment_data = eval('(' + returnData.data + ')');
                    var __attachmentName = __attachment_data.attachmentName;
                    __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
                        '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>&nbsp;' +
                        '<a onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\')">删除</a>' +
                        '</li>'));
                    if (__callback) {
                        eval(__callback + '()');
                    }
                } else {
                    alert('上传失败，请重新上传。');
                }
            }
        })
    } else {
        $('#' + __file_uploader).hide();
    }

}
/*---删除附件---*/
function __metar_delete_file(__attachmentId , __attachmentName) {
    if(confirm('确认要删除附件' + __attachmentName + '?')){
        $.ajax({
            url: _PATH + '/attachment.do?method=delete',
            method: 'POST',
            async: true,
            dataType: 'json',
            data: {flowingFlag: 'Y', jsonArr: '[{attachmentId:' + __attachmentId + '}]'},
            success: function (response) {
                if (response.success) {
                    $('#__attachment_' + __attachmentId).remove();
                } else {
                    alert('删除附件失败，请重试');
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert('删除附件失败，请重试');
            }
        })
    }

}

/*
 遍历所有class为__metar_enum的select控件，然后加载下拉数据
 */
function __loadValues4Select() {
    var selects = $('.__metar_enum');
    for (var i = 0; i < selects.length; i++) {
        var select = selects[i];
        var __select_id = $(select).attr('id');
        var __enumCode = $(select).attr('enumCode');
        var __emptyMsg = $(select).attr('emptyMsg');
        if (__enumCode) {
            if(__emptyMsg){
                __loadEnumValuesByType(__select_id, __enumCode,__emptyMsg);
            }else{
                __loadEnumValuesByType(__select_id, __enumCode);
            }
        }
    }
}

/*
 加载枚举下拉数据
 */
function __loadEnumValuesByType(__select_id, __enumCode,__emptyMsg) {
    $.ajax({
        url: _PATH + '/commEnumController.do?method=getEnumByType',
        method: 'POST',
        async: true,
        dataType: 'json',
        data: {enumItemCode: __enumCode, orgId: '', status: 1},
        success: function (response) {
            if (response) {
                var __select_value = $('#' + __select_id).attr('enumValue');
                if(__emptyMsg){
                    document.getElementById(__select_id).options.add(new Option(__emptyMsg, -1));
                }
                for (var i = 0; i < response.length; i++) {
                    var emunEntity = response[i];
                    if (emunEntity.enumValueId == __select_value) {
                        document.getElementById(__select_id).options.add(new Option(emunEntity.enumValueName, emunEntity.enumValueId));
                    } else {
                        document.getElementById(__select_id).options.add(new Option(emunEntity.enumValueName, emunEntity.enumValueId));
                    }
                }
                $('#' + __select_id).val(__select_value);
//                __init_query_select($('#' + __select_id));
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    })
}

/*---返回上一步---*/
function __exitFromFrame() {
    location.href = _PATH + _winParams.__returnUrl;
}

/*---弹出窗口---*/
function __open_metar_window(__window, __title, __width, __height, __callback) {
//    __open_metar_window_parent(__window , __title , __width , __height , __callback);
//    return;
    if (parent.__open_metar_window_parent) {
        parent.__open_metar_window_parent(__window, __title, __width, __height, __callback);
    } else {
        __open_metar_window_parent(__window, __title, __width, __height, __callback);
    }
}
function __open_metar_window_parent(__window, __title, __width, __height, __callback) {
    var __window_window = $('#__' + __window + '_container');
    if (!document.getElementById('__' + __window + '_container')) {
        __window_window = $('<div id="__' + __window + '_container" class="__link_dialog_container modal"></div>');
        var __window_header = $('<div class="modal-header">' + __title + '</div>');
        var __window_header_close_btn = $('<div class="close">×</div>');
        __window_header_close_btn.click(function () {
            $(__window_window).modal('hide');
        });
        __window_header.append(__window_header_close_btn);
        var __window_body = $('<div class="modal-body"></div>');
        __window_window.append(__window_header);
        __window_window.append(__window_body);
        if (__width) {
            __window_window.css('width', __width);
            __window_window.css('margin-left', -__width / 2);
        }
        if (__height) {
            __window_window.css('height', __height);
            __window_body.css('height', __height - 85);
            __window_window.css('margin-top', -__height / 2);
        }
        __window_window.modal({backdrop: 'static',show:true});
        $('body').append(__window_window);
        __callback(__window_body);
        drag(__window_window.get(0), __window_header.get(0));
    }else{
        var __window_header_close_btn = $('<div class="close">×</div>');
        __window_header_close_btn.click(function () {
            $(__window_window).modal('hide');
        });
        $(".modal-header").html(__title).append(__window_header_close_btn);
    }
    __window_window.modal({backdrop: 'static',show:true});
    return __window_window;
}

/*---改变所在iframe的大小---*/
function __resize_up_iframe() {
//    alert(document.body.scrollHeight + ',' + document.documentElement.clientHeight);
    $(window.parent.document).find('#' + _parent_iframe).css('height', document.body.scrollHeight + 20);
}

/*
 显示等待提示
 */
function __show_metar_loading() {
//    $('#__progress_bar').stop().css('width' , '1px');
////    alert(123);
//    setTimeout(function(){
//        $('#__progress_bar').stop().animate({width:'100%'} , 5000) ;
//    } , 100)
    if ($('#__metar_progress_box').length > 0) {
        $('#__metar_progress_box').stop().show();
    } else {
        var __metar_progress_box = $('<div id="__metar_progress_box" class="__metar_progress_box"></div>');
        __metar_progress_box.append('<img src="' + _PATH + '/base/_resources/loading.gif"/>');
        $('body').append(__metar_progress_box);
    }
}

/*
 隐藏等待提示
 */
function __hide_metar_loading() {
    $('#__metar_progress_box').stop().hide();
}

/*
 弹出树
 @param  __open_btn_id       弹出树的触发元素ID
 @param  __tree_type         弹出树的类型（1、组织树 2、人员树   3、派发树）
 @param  __tree_title        弹出树的标题
 @param  __callback          选择数据后，回调函数
 @param  __root_org_id       根节点组织ID
 @param  __root_as_company   是否将根节点组织ID所属公司作为根节点(0、是，获取根节点组织ID所属公司，作为根节点    1、否)，默认为0
 @param  __select_type       选择类型（checkbox、多选（默认）    radio、单选）
 */
function __open_tree(__open_btn_id, __tree_type, __tree_title, __callback, __root_org_id, __root_as_company, __select_type) {
    __open_tree_customParam(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, {type: __tree_type, rootOrgId: __root_org_id, rootAsCompany: __root_as_company});
}

function __open_tree2(__open_btn_id, __tree_type, __tree_title, __callback, __root_org_id, __root_as_company, __select_type) {
    __open_tree_customParam2(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, {type: __tree_type, rootOrgId: __root_org_id, rootAsCompany: __root_as_company});
}


/**
 *
 * @param __settings{
 *     open_btn_id: 弹出树的触发元素ID
 *     tree_type:   弹出树的类型（1、组织树 2、人员树   3、派发树  4、专业处室组织树   5、专业处室人员树）
 *     tree_title:  弹出树的标题
 *     callback:    选择数据后，回调函数
 *     select_type: 选择类型（checkbox、多选（默认）    radio、单选）
 *     root_org_id: 根节点组织ID
 *     root_as_company:是否将根节点组织ID所属公司作为根节点(0、是，获取根节点组织ID所属公司，作为根节点    1、否)，默认为0
 * }
 *
 * @private
 */
function __open_tree_config(__settings){
    __open_tree_customParam(__settings.open_btn_id, __settings.tree_type, __settings.tree_title, __settings.callback, __settings.select_type, {type: __settings.tree_type, rootOrgId: __settings.root_org_id, rootAsCompany: __settings.root_as_company});
}

//不确定的查询数据参数，放入一个对象
function __open_tree_customParam(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, __dataParameter) {
    if (__select_type != 'radio') {
        __select_type = 'checkbox';
    }
    if (!__dataParameter.rootOrgId) {
        __dataParameter.rootOrgId = '';
    }
    if (!__dataParameter.rootAsCompany) {
        __dataParameter.rootAsCompany = 0;
    }
    __dataParameter.type = __tree_type;

    var __dialog_height = document.body.scrollHeight > 450 ? 450 : document.body.scrollHeight<300 ? 300 :document.body.scrollHeight;
    if (parent.__open_tree) {
        __dialog_height = parent.document.body.scrollHeight > 450 ? 450 : document.body.scrollHeight<300 ? 300 :document.body.scrollHeight;
    }
    __open_metar_window(__open_btn_id, __tree_title, 400, __dialog_height, function (__window) {
            var __ztree_panel_0 = $('<ul id="' + __open_btn_id + '_tree" class="ztree"></ul>');
            var __btns_div = $('<div class="__dialog_panel_btns"></div>');
            if (__tree_type == 1 || __tree_type == 4) {
                var __ztree_0 = __init_metar_org_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, __dialog_height, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    if (__ztree_0.getCheckedNodes().length == 0) {
                        alert('请选择');
                    }  else {
                        __window.parent().modal('hide');
                        var __return_checked_nodes = new Array();
                        for (var i = 0; i < __ztree_0.getCheckedNodes().length; i++) {
                            var __return_org = __ztree_0.getCheckedNodes()[i];
                            if (!__return_org.isParent) {
                                __return_checked_nodes.push(__return_org);
                            } else if (!__return_org.children) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            } else if (__return_org.children.length == 0) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            }
                        }
                        __callback(__return_checked_nodes);
                    }
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    __ztree_0.checkAllNodes(false);
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 2 || __tree_type == 5) {
                var __ztree_0 = __init_metar_person_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, null, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    var __selectedPersons = __ztree_0.getCheckedNodes();
                    var __selectedSearchPersons = __ztree_0.getSelectedPersons();
                    if (__ztree_0.search) {
                        if (__selectedSearchPersons.length == 0) {
                            alert('请选择人员');
                            return;
                        } else {
                            __callback(__selectedSearchPersons);
                        }
                    } else {
                        if (__selectedPersons.length == 0) {
                            alert('请选择人员');
                            return;
                        } else {
                            var __return_checked_nodes = new Array();
                            for (var i = 0; i < __selectedPersons.length; i++) {
                                var __return_person = __selectedPersons[i];
                                if (!__return_person.isParent) {
                                    __return_checked_nodes.push(__return_person);
                                }
                            }
                            __callback(__return_checked_nodes);
                        }
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    __ztree_0.checkAllNodes(false);
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 3) {
                var __ztree_array = __init_metar_dispatch_tree_panel(__ztree_panel_0, __select_type, __dataParameter, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_org = __ztree_array[0];
                    var __selectedOrgs = __ztree_org.getCheckedNodes();
                    var __org_null = true;
                    var __select_person_null = true;
                    var __search_person_null = true;
                    var __return_persons = new Array();
                    if (__selectedOrgs.length > 0) {
                        __org_null = false;
                    }
                    if (__ztree_array.length > 1) {
                        var __ztree_person = __ztree_array[1];
                        var __selectedPersons = __ztree_person.getCheckedNodes();
                        var __selectedSearchPersons = __ztree_person.getSelectedPersons();
                        if (__ztree_person.search) {
                            if (__selectedSearchPersons.length > 0) {
                                __select_person_null = false;
                                __return_persons = __selectedSearchPersons;
                            }
                        } else {
                            if (__selectedPersons.length > 0) {
                                __search_person_null = false;
                                __return_persons = __selectedPersons;
                            }
                        }
                    }
                    if (__org_null && __select_person_null && __search_person_null) {
                        alert('请选择组织或者人员');
                        return;
                    } else {
                        var __return_nodes = __selectedOrgs.concat(__return_persons);
                        var __return_checked_nodes = new Array();
                        debugger;
                        for (var i = 0; i < __return_nodes.length; i++) {
                            var __return_node = __return_nodes[i];
                            if (!__return_node.isParent) {
                                __return_checked_nodes.push(__return_node);
                            }
                        }
                        __callback(__return_checked_nodes);
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    var __ztree_org = __ztree_array[0];
                    __ztree_org.checkAllNodes(false);
                    if (__ztree_array.length > 1) {
                        var __ztree_person = __ztree_array[1];
                        __ztree_person.checkAllNodes(false);
                    }
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            }

            function __init_metar_org_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 1;
                if (__height) {
                    __ztree_panel.css('height', __height - 100);
                }
                __ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                var __ztree_0 = __init_ztree_panel(__ztree_panel, __select_type, __dataParameter);
                return __ztree_0;
            }

            function __init_metar_person_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 2;
                var __ztree_0 = {};
                var __ztree_query_result_data;
                var __ztree_query_panel = $('<div class="input-group"></div>');
                var __ztree_query_input = $('<input type="text" class="form-control">');
                var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
                var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
                var __ztree_query_result_panel = $('<div id="1111" class=""></div>');
                __ztree_query_result_panel.css('height', __dialog_height - 140);
                __ztree_query_result_panel.css('overflow', 'auto');
                __ztree_query_result_panel.css('margin-top', 5);
                __ztree_query_result_panel.hide();
                __ztree_query_btn.click(function () {
                    __ztree_0.search = true;
                    var __ztree_query_keyword = __ztree_query_input.val();
                    __ztree_query_keyword = trim(__ztree_query_keyword);
                    __ztree_query_input.val(__ztree_query_keyword);
                    if (__ztree_query_keyword == '') {
                        __ztree_query_input.addClass('metar_not_null');
                        __ztree_query_input.focus();
                        return;
                    } else {
                        __ztree_query_input.removeClass('metar_not_null');
                    }
                    __ztree_panel.hide();
                    __ztree_query_result_panel.empty().show();
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=queryPerson',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {name: __ztree_query_keyword},
                        success: function (data) {
                            __ztree_query_result_data = data;
                            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                            for (var i = 0; i < data.length; i++) {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName + '</td></tr>');
                            }
                            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                        },
                        error: function () {
                            alert("error");
                        }
                    })
                });
                __ztree_query_clear_btn.click(function () {
                    __ztree_query_input.val('');
                    __ztree_query_result_panel.empty().hide();
                    __ztree_panel.show();
                    __ztree_0.search = false;
                });

                __ztree_query_panel.append(__ztree_query_input);
                __ztree_query_panel.append(__ztree_query_clear_btn);
                __ztree_query_panel.append(__ztree_query_btn);
                __window_container.append(__ztree_query_panel);
                __window_container.append(__ztree_query_result_panel);
                if (__height) {
                    __ztree_panel.css('height', __height);
                } else {
                    __ztree_panel.css('height', __dialog_height - 135);
                }
                __ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                __ztree_0 = __init_ztree_panel(__ztree_panel, __select_type, __dataParameter);
                __ztree_0.getSelectedPersons = function () {
                    var __return_selected_persons_array = new Array();
//                var __person_check_list = document.getElementsByName(__open_btn_id + '_person_check');
                    var __person_check_list = __ztree_query_result_panel.find('input');
                    for (var j = 0; j < __person_check_list.length; j++) {
                        if (__person_check_list[j].checked) {
                            var __person = __ztree_query_result_data[__person_check_list[j].value];
                            __return_selected_persons_array.push({id: __person.userId, userName: __person.userName, label: __person.trueName});
                        }
                    }
                    return __return_selected_persons_array;
                }
                return __ztree_0;
            }

            function __init_metar_dispatch_tree_panel(__ztree_org_panel, __select_type, ___dataParameter, __dialog_height) {
                var __ztree_array = new Array();
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 125);
                __ztree_org_container_panel.css('overflow', 'auto');
                var __ztree_pserson_container_panel;
                var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
                __ztree_tab_panel.css('margin-top', -10);
                var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
                var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
                __ztree_tab_panel.append(__ztree_tab_btn_0);
                __ztree_tab_panel.append(__ztree_tab_btn_1);
                __ztree_tab_btn_0.click(function () {
                    __active_tab($(this));
                    __active_tree_panel(__ztree_org_container_panel);
                });
                __ztree_tab_btn_1.click(function () {
                    if (!__ztree_pserson_container_panel) {
                        __ztree_pserson_container_panel = $('<div></div>');
                        var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_1" class="ztree"></ul>');
                        __ztree_pserson_container_panel.append(__ztree_pserson_panel);
                        __window.append(__ztree_pserson_container_panel);
                        __dataParameter.type = 2;
                        var __ztree_1 = __init_metar_person_tree_panel(__ztree_pserson_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                        __ztree_array.push(__ztree_1);
                    }
                    __active_tab($(this));
                    __active_tree_panel(__ztree_pserson_container_panel);
                    return __ztree_pserson_container_panel;
                });
                __window.append(__ztree_tab_panel);
                function __active_tab(__ztree_tab_btn) {
                    __ztree_tab_panel.find('li.active').removeClass("active");
                    __ztree_tab_btn.addClass("active");
                }

                function __active_tree_panel(__tree_panel) {
                    var __tree_panels = __window.children();
                    for (var x = 1; x < __tree_panels.length; x++) {
                        $(__tree_panels[x]).hide();
                    }
                    $(__tree_panel).show();
                }

                __ztree_org_container_panel.append(__ztree_org_panel);
                __window.append(__ztree_org_container_panel);
                __dataParameter.type = 1;
                var __ztree_0 = __init_metar_org_tree_panel(__ztree_org_container_panel, __ztree_org_panel, __select_type, ___dataParameter, null, null);
                __ztree_array.push(__ztree_0);
                return __ztree_array;
            }

            /* 2015-10-11 之前的算法
             var __ztree_panel_1;
             var __ztree_0;
             var __ztree_1;
             var __btns_div = $('<div class="__dialog_panel_btns"></div>');
             if(__tree_type == 3){
             var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
             __ztree_tab_panel.css('margin-top' , -10);
             var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
             var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
             __ztree_tab_panel.append(__ztree_tab_btn_0);
             __ztree_tab_panel.append(__ztree_tab_btn_1);
             __ztree_tab_btn_0.click(function(){
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_0);
             });
             __ztree_tab_btn_1.click(function(){
             if(!__ztree_panel_1){
             __ztree_panel_1 = $('<ul id="'+__open_btn_id+'_tree_tab_1" class="ztree"></ul>');
             __ztree_panel_1.css('height' , 380);
             __ztree_panel_1.css('overflow' , 'auto');
             __window.append(__ztree_panel_1);
             __ztree_1 = __init_ztree_panel(__ztree_panel_1 , 2);
             }
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_1);
             });
             __window.append(__ztree_tab_panel);
             function __active_tab(__ztree_tab_btn){
             __ztree_tab_panel.find('li.active').removeClass("active");
             __ztree_tab_btn.addClass("active");
             }
             function __active_tree_panel(__tree_panel){
             var __tree_panels = __window.children();
             for(var x = 1 ; x < __tree_panels.length ; x++){
             $(__tree_panels[x]).hide();
             }
             $(__tree_panel).show();
             }
             } else {
             if(__tree_type == 2){
             var __ztree_query_panel = $('<div class="input-group"></div>');
             var __ztree_query_input = $('<input type="text" class="form-control">');
             var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
             var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
             var __ztree_query_result_panel = $('<div class=""></div>');
             __ztree_query_result_panel.css('height' , 360);
             __ztree_query_result_panel.css('overflow' , 'auto');
             __ztree_query_result_panel.css('margin-top' , 5);
             __ztree_query_result_panel.hide();
             __ztree_query_btn.click(function(){
             var __ztree_query_keyword = __ztree_query_input.val();
             __ztree_query_keyword = trim(__ztree_query_keyword);
             __ztree_query_input.val(__ztree_query_keyword);
             if(__ztree_query_keyword == ''){
             __ztree_query_input.addClass('metar_not_null');
             __ztree_query_input.focus();
             return;
             }
             __ztree_panel_0.hide();
             __ztree_query_result_panel.empty().show();
             $.ajax({
             url : _PATH + '/commTreeController.do?method=queryPerson',
             type : 'POST',
             async : true,
             dataType : 'json',
             data : {name : __ztree_query_keyword},
             success: function (data) {
             var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
             var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
             for (var i = 0 ; i < data.length ; i++) {
             ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + data[i].userName + '" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.orgName + '</td></tr>');
             }
             ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
             __ztree_query_result_panel.append(____ztree_query_result_panel_table);
             },
             error: function () {
             alert("error");
             }
             })
             });
             __ztree_query_clear_btn.click(function(){
             __ztree_query_input.val('');
             __ztree_query_result_panel.hide();
             __ztree_panel_0.show();
             });
             __ztree_query_panel.append(__ztree_query_input);
             __ztree_query_panel.append(__ztree_query_clear_btn);
             __ztree_query_panel.append(__ztree_query_btn);
             __window.append(__ztree_query_panel);
             __window.append(__ztree_query_result_panel);
             }
             }

             if(__tree_type == 3){
             __ztree_panel_0.css('height' , 380);
             __ztree_panel_0.css('overflow' , 'auto');
             } else if(__tree_type == 2){
             __ztree_panel_0.css('height' , 365);
             __ztree_panel_0.css('overflow' , 'auto');
             }
             __window.append(__ztree_panel_0);
             __ztree_0 = __init_ztree_panel(__ztree_panel_0 , __tree_type == 3 ? 1 : __tree_type);
             var __btn_submit = $('<span class="btn btn-danger">确定</span>');
             __btn_submit.click(function(){
             if(__ztree_0.getCheckedNodes().length == 0){
             alert('请选择');
             } else {
             __window.parent().modal('hide');
             __callback(__ztree_0.getCheckedNodes());
             }
             });
             var __btn_clear = $('<span class="btn btn-default">清空</span>');
             __btn_clear.click(function(){
             __ztree_0.checkAllNodes(false);
             __window.parent().modal('hide');
             __callback('');
             });
             var __btn_close = $('<span class="btn btn-default">关闭</span>');
             __btn_close.click(function(){
             __window.parent().modal('hide');
             });
             __btns_div.append(__btn_submit);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_clear);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_close);
             __window.parent().append(__btns_div);
             */
        }
    )
    ;
}

function __open_tree_customParam2(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, __dataParameter) {
    if (__select_type != 'radio') {
        __select_type = 'checkbox';
    }
    if (!__dataParameter.rootOrgId) {
        __dataParameter.rootOrgId = '';
    }
    if (!__dataParameter.rootAsCompany) {
        __dataParameter.rootAsCompany = 0;
    }
    __dataParameter.type = __tree_type;

    var __dialog_height = document.body.scrollHeight > 450 ? 450 : document.body.scrollHeight<300 ? 300 :document.body.scrollHeight;
    if (parent.__open_tree) {
        __dialog_height = parent.document.body.scrollHeight > 450 ? 450 : document.body.scrollHeight<300 ? 300 :document.body.scrollHeight;
    }
    __open_metar_window(__open_btn_id, __tree_title, 400, __dialog_height, function (__window) {
            var __ztree_panel_0 = $('<ul id="' + __open_btn_id + '_tree" class="ztree"></ul>');
            var __btns_div = $('<div class="__dialog_panel_btns"></div>');
            if (__tree_type == 1 || __tree_type == 4) {
                var __ztree_0 = __init_metar_org_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, __dialog_height, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    if (__ztree_0.getCheckedNodes().length == 0) {
                        alert('请选择');
                    }  else {
                        __window.parent().modal('hide');
                        var __return_checked_nodes = new Array();
                        for (var i = 0; i < __ztree_0.getCheckedNodes().length; i++) {
                            var __return_org = __ztree_0.getCheckedNodes()[i];
                            if (!__return_org.isParent) {
                                __return_checked_nodes.push(__return_org);
                            } else if (!__return_org.children) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            } else if (__return_org.children.length == 0) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            }
                        }
                        __callback(__return_checked_nodes);
                    }
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    __ztree_0.checkAllNodes(false);
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 2 || __tree_type == 5) {
                var __ztree_0 = __init_metar_person_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, null, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    var __selectedPersons = __ztree_0.getCheckedNodes();
                    var __selectedSearchPersons = __ztree_0.getSelectedPersons();
                    if (__ztree_0.search) {
                        if (__selectedSearchPersons.length == 0) {
                            alert('请选择人员');
                            return;
                        } else {
                            __callback(__selectedSearchPersons);
                        }
                    } else {
                        if (__selectedPersons.length == 0) {
                            alert('请选择人员');
                            return;
                        } else {
                            var __return_checked_nodes = new Array();
                            for (var i = 0; i < __selectedPersons.length; i++) {
                                var __return_person = __selectedPersons[i];
                                if (!__return_person.isParent) {
                                    __return_checked_nodes.push(__return_person);
                                }
                            }
                            __callback(__return_checked_nodes);
                        }
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    __ztree_0.checkAllNodes(false);
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 3) {
                var __ztree_array = __init_metar_dispatch_tree_panel(__ztree_panel_0, __select_type, __dataParameter, __dialog_height);
                var __btn_submit = $('<span class="btn btn-danger">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_org = __ztree_array[0];
                    var __selectedOrgs = __ztree_org.getCheckedNodes();
                    var __org_null = true;
                    var __select_person_null = true;
                    var __search_person_null = true;
                    var __return_persons = new Array();
                    if (__selectedOrgs.length > 0) {
                        __org_null = false;
                    }
                    if (__ztree_array.length > 1) {
                        var __ztree_person = __ztree_array[1];
                        var __selectedPersons = __ztree_person.getCheckedNodes();
                        var __selectedSearchPersons = __ztree_person.getSelectedPersons();
                        if (__ztree_person.search) {
                            if (__selectedSearchPersons.length > 0) {
                                __select_person_null = false;
                                __return_persons = __selectedSearchPersons;
                            }
                        } else {
                            if (__selectedPersons.length > 0) {
                                __search_person_null = false;
                                __return_persons = __selectedPersons;
                            }
                        }
                    }
                    if (__org_null && __select_person_null && __search_person_null) {
                        alert('请选择组织或者人员');
                        return;
                    } else {
                        var __return_nodes = __selectedOrgs.concat(__return_persons);
                        var __return_checked_nodes = new Array();
                        debugger;
                        for (var i = 0; i < __return_nodes.length; i++) {
                            var __return_node = __return_nodes[i];
                            if (!__return_node.isParent) {
                                __return_checked_nodes.push(__return_node);
                            }
                        }
                        __callback(__return_checked_nodes);
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    var __ztree_org = __ztree_array[0];
                    __ztree_org.checkAllNodes(false);
                    if (__ztree_array.length > 1) {
                        var __ztree_person = __ztree_array[1];
                        __ztree_person.checkAllNodes(false);
                    }
                    __window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            }

            function __init_metar_org_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 1;
                if (__height) {
                    __ztree_panel.css('height', __height - 100);
                }
                __ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                var __ztree_0 = __init_ztree_panel2(__ztree_panel, __select_type, __dataParameter);
                return __ztree_0;
            }

            function __init_metar_person_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 2;
                var __ztree_0 = {};
                var __ztree_query_result_data;
                var __ztree_query_panel = $('<div class="input-group"></div>');
                var __ztree_query_input = $('<input type="text" class="form-control">');
                var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
                var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
                var __ztree_query_result_panel = $('<div id="1111" class=""></div>');
                __ztree_query_result_panel.css('height', __dialog_height - 140);
                __ztree_query_result_panel.css('overflow', 'auto');
                __ztree_query_result_panel.css('margin-top', 5);
                __ztree_query_result_panel.hide();
                __ztree_query_btn.click(function () {
                    __ztree_0.search = true;
                    var __ztree_query_keyword = __ztree_query_input.val();
                    __ztree_query_keyword = trim(__ztree_query_keyword);
                    __ztree_query_input.val(__ztree_query_keyword);
                    if (__ztree_query_keyword == '') {
                        __ztree_query_input.addClass('metar_not_null');
                        __ztree_query_input.focus();
                        return;
                    } else {
                        __ztree_query_input.removeClass('metar_not_null');
                    }
                    __ztree_panel.hide();
                    __ztree_query_result_panel.empty().show();
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=queryPerson',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {name: __ztree_query_keyword},
                        success: function (data) {
                            __ztree_query_result_data = data;
                            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                            for (var i = 0; i < data.length; i++) {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName + '</td></tr>');
                            }
                            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                        },
                        error: function () {
                            alert("error");
                        }
                    })
                });
                __ztree_query_clear_btn.click(function () {
                    __ztree_query_input.val('');
                    __ztree_query_result_panel.empty().hide();
                    __ztree_panel.show();
                    __ztree_0.search = false;
                });

                __ztree_query_panel.append(__ztree_query_input);
                __ztree_query_panel.append(__ztree_query_clear_btn);
                __ztree_query_panel.append(__ztree_query_btn);
                __window_container.append(__ztree_query_panel);
                __window_container.append(__ztree_query_result_panel);
                if (__height) {
                    __ztree_panel.css('height', __height);
                } else {
                    __ztree_panel.css('height', __dialog_height - 135);
                }
                __ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                __ztree_0 = __init_ztree_panel(__ztree_panel, __select_type, __dataParameter);
                __ztree_0.getSelectedPersons = function () {
                    var __return_selected_persons_array = new Array();
//                var __person_check_list = document.getElementsByName(__open_btn_id + '_person_check');
                    var __person_check_list = __ztree_query_result_panel.find('input');
                    for (var j = 0; j < __person_check_list.length; j++) {
                        if (__person_check_list[j].checked) {
                            var __person = __ztree_query_result_data[__person_check_list[j].value];
                            __return_selected_persons_array.push({id: __person.userId, userName: __person.userName, label: __person.trueName});
                        }
                    }
                    return __return_selected_persons_array;
                }
                return __ztree_0;
            }

            function __init_metar_dispatch_tree_panel(__ztree_org_panel, __select_type, ___dataParameter, __dialog_height) {
                var __ztree_array = new Array();
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 125);
                __ztree_org_container_panel.css('overflow', 'auto');
                var __ztree_pserson_container_panel;
                var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
                __ztree_tab_panel.css('margin-top', -10);
                var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
                var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
                __ztree_tab_panel.append(__ztree_tab_btn_0);
                __ztree_tab_panel.append(__ztree_tab_btn_1);
                __ztree_tab_btn_0.click(function () {
                    __active_tab($(this));
                    __active_tree_panel(__ztree_org_container_panel);
                });
                __ztree_tab_btn_1.click(function () {
                    if (!__ztree_pserson_container_panel) {
                        __ztree_pserson_container_panel = $('<div></div>');
                        var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_1" class="ztree"></ul>');
                        __ztree_pserson_container_panel.append(__ztree_pserson_panel);
                        __window.append(__ztree_pserson_container_panel);
                        __dataParameter.type = 2;
                        var __ztree_1 = __init_metar_person_tree_panel(__ztree_pserson_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                        __ztree_array.push(__ztree_1);
                    }
                    __active_tab($(this));
                    __active_tree_panel(__ztree_pserson_container_panel);
                    return __ztree_pserson_container_panel;
                });
                __window.append(__ztree_tab_panel);
                function __active_tab(__ztree_tab_btn) {
                    __ztree_tab_panel.find('li.active').removeClass("active");
                    __ztree_tab_btn.addClass("active");
                }

                function __active_tree_panel(__tree_panel) {
                    var __tree_panels = __window.children();
                    for (var x = 1; x < __tree_panels.length; x++) {
                        $(__tree_panels[x]).hide();
                    }
                    $(__tree_panel).show();
                }

                __ztree_org_container_panel.append(__ztree_org_panel);
                __window.append(__ztree_org_container_panel);
                __dataParameter.type = 1;
                var __ztree_0 = __init_metar_org_tree_panel(__ztree_org_container_panel, __ztree_org_panel, __select_type, ___dataParameter, null, null);
                __ztree_array.push(__ztree_0);
                return __ztree_array;
            }

            /* 2015-10-11 之前的算法
             var __ztree_panel_1;
             var __ztree_0;
             var __ztree_1;
             var __btns_div = $('<div class="__dialog_panel_btns"></div>');
             if(__tree_type == 3){
             var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
             __ztree_tab_panel.css('margin-top' , -10);
             var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
             var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
             __ztree_tab_panel.append(__ztree_tab_btn_0);
             __ztree_tab_panel.append(__ztree_tab_btn_1);
             __ztree_tab_btn_0.click(function(){
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_0);
             });
             __ztree_tab_btn_1.click(function(){
             if(!__ztree_panel_1){
             __ztree_panel_1 = $('<ul id="'+__open_btn_id+'_tree_tab_1" class="ztree"></ul>');
             __ztree_panel_1.css('height' , 380);
             __ztree_panel_1.css('overflow' , 'auto');
             __window.append(__ztree_panel_1);
             __ztree_1 = __init_ztree_panel(__ztree_panel_1 , 2);
             }
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_1);
             });
             __window.append(__ztree_tab_panel);
             function __active_tab(__ztree_tab_btn){
             __ztree_tab_panel.find('li.active').removeClass("active");
             __ztree_tab_btn.addClass("active");
             }
             function __active_tree_panel(__tree_panel){
             var __tree_panels = __window.children();
             for(var x = 1 ; x < __tree_panels.length ; x++){
             $(__tree_panels[x]).hide();
             }
             $(__tree_panel).show();
             }
             } else {
             if(__tree_type == 2){
             var __ztree_query_panel = $('<div class="input-group"></div>');
             var __ztree_query_input = $('<input type="text" class="form-control">');
             var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
             var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
             var __ztree_query_result_panel = $('<div class=""></div>');
             __ztree_query_result_panel.css('height' , 360);
             __ztree_query_result_panel.css('overflow' , 'auto');
             __ztree_query_result_panel.css('margin-top' , 5);
             __ztree_query_result_panel.hide();
             __ztree_query_btn.click(function(){
             var __ztree_query_keyword = __ztree_query_input.val();
             __ztree_query_keyword = trim(__ztree_query_keyword);
             __ztree_query_input.val(__ztree_query_keyword);
             if(__ztree_query_keyword == ''){
             __ztree_query_input.addClass('metar_not_null');
             __ztree_query_input.focus();
             return;
             }
             __ztree_panel_0.hide();
             __ztree_query_result_panel.empty().show();
             $.ajax({
             url : _PATH + '/commTreeController.do?method=queryPerson',
             type : 'POST',
             async : true,
             dataType : 'json',
             data : {name : __ztree_query_keyword},
             success: function (data) {
             var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
             var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
             for (var i = 0 ; i < data.length ; i++) {
             ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + data[i].userName + '" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.orgName + '</td></tr>');
             }
             ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
             __ztree_query_result_panel.append(____ztree_query_result_panel_table);
             },
             error: function () {
             alert("error");
             }
             })
             });
             __ztree_query_clear_btn.click(function(){
             __ztree_query_input.val('');
             __ztree_query_result_panel.hide();
             __ztree_panel_0.show();
             });
             __ztree_query_panel.append(__ztree_query_input);
             __ztree_query_panel.append(__ztree_query_clear_btn);
             __ztree_query_panel.append(__ztree_query_btn);
             __window.append(__ztree_query_panel);
             __window.append(__ztree_query_result_panel);
             }
             }

             if(__tree_type == 3){
             __ztree_panel_0.css('height' , 380);
             __ztree_panel_0.css('overflow' , 'auto');
             } else if(__tree_type == 2){
             __ztree_panel_0.css('height' , 365);
             __ztree_panel_0.css('overflow' , 'auto');
             }
             __window.append(__ztree_panel_0);
             __ztree_0 = __init_ztree_panel(__ztree_panel_0 , __tree_type == 3 ? 1 : __tree_type);
             var __btn_submit = $('<span class="btn btn-danger">确定</span>');
             __btn_submit.click(function(){
             if(__ztree_0.getCheckedNodes().length == 0){
             alert('请选择');
             } else {
             __window.parent().modal('hide');
             __callback(__ztree_0.getCheckedNodes());
             }
             });
             var __btn_clear = $('<span class="btn btn-default">清空</span>');
             __btn_clear.click(function(){
             __ztree_0.checkAllNodes(false);
             __window.parent().modal('hide');
             __callback('');
             });
             var __btn_close = $('<span class="btn btn-default">关闭</span>');
             __btn_close.click(function(){
             __window.parent().modal('hide');
             });
             __btns_div.append(__btn_submit);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_clear);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_close);
             __window.parent().append(__btns_div);
             */
        }
    )
    ;
}

function __init_ztree_panel(__ztree_panel, __select_type, __dataParameter) {
    var __current_tree;
    __dataParameter = $.param(__dataParameter);
    var __ztree_setting = {
        check: {
            /**复选框**/
            nocheckInherit: false,
            enable: true,
            chkStyle: 'checkbox',
            chkboxType: {'Y': 'ps', 'N': 'ps'}
        },
        async: {
            autoParam: ['id'],
            contentType: 'application/x-www-form-urlencoded',
            enable: true,
            dataFilter: __zTreeDataFilter,
            type: 'post',
            url: _PATH + '/commTreeController.do?method=createDisPatchTree2&' + __dataParameter
        },
        data: {
            key: {
                name: 'label'
            },
            simpleData: {
                enable: true,
                idKey: "nodeId",
                pIdKey: "parentId",
                isParent: "parent",
                rootPId: ""
            }
        },
        callback: {
            beforeCheck : function(treeId, treeNode){
                if(__select_type == 'radio' && !treeNode.checked){
                    __current_tree.checkAllNodes(false);
                }
                return true;
            }
        }
    }
    __current_tree = $.fn.zTree.init(__ztree_panel, __ztree_setting, null);
    return __current_tree;
}

function __init_ztree_panel2(__ztree_panel, __select_type, __dataParameter) {
    var __current_tree;
    __dataParameter = $.param(__dataParameter);
    var __ztree_setting = {
        check: {
            /**复选框**/
            nocheckInherit: false,
            enable: true,
            chkStyle: 'checkbox',
            chkboxType: {'Y': 'ps', 'N': 'ps'}
        },
        async: {
            autoParam: ['id'],
            contentType: 'application/x-www-form-urlencoded',
            enable: true,
            dataFilter: __zTreeDataFilter,
            type: 'post',
            url: _PATH + '/commTreeController.do?method=createDisPatchTree3&' + __dataParameter
        },
        data: {
            key: {
                name: 'label'
            },
            simpleData: {
                enable: true,
                idKey: "nodeId",
                pIdKey: "parentId",
                isParent: "parent",
                rootPId: ""
            }
        },
        callback: {
            beforeCheck : function(treeId, treeNode){
                if(__select_type == 'radio' && !treeNode.checked){
                    __current_tree.checkAllNodes(false);
                }
                return true;
            }
        }
    }
    __current_tree = $.fn.zTree.init(__ztree_panel, __ztree_setting, null);
    return __current_tree;
}

function __zTreeDataFilter(treeId, parentNode, responseData) {
//    if (responseData) {
//        for(var i =0; i < responseData.length; i++) {
//            responseData[i].nocheck = true;
//        }
//    }
    return responseData;
};

/*-------------------------- +
 拖拽函数
 +-------------------------- */
function drag(oDrag, handle, cursor) {
    var disX = dixY = 0;
    handle = handle || oDrag;
    //handle.style.cursor = "move";
    handle.onmousedown = function (event) {
        var event = event || window.event;

        var NS = navigator.appName == 'Netscape';//当前浏览器的类型 Netscape ,Microsoft Internet Explorer
        if (event.button == 2) { //単鼠标右击是不拖动对象
            //alert(event.button);
            return;
        }

        handle.style.cursor = cursor || "move"; //鼠标移动对象时的样式
        disX = event.clientX - oDrag.offsetLeft;
        disY = event.clientY - oDrag.offsetTop;

        document.onmousemove = function (event) {
            var event = event || window.event;
            var iL = event.clientX - disX;
            var iT = event.clientY - disY;
//            alert(iL + ',' + iT);
            var bgLeft = window.pageXOffset
                || document.documentElement.scrollLeft
                || document.body.scrollLeft || 0;

            var bgTop = window.pageYOffset
                || document.documentElement.scrollTop
                || document.body.scrollTop || 0;

            if (document.documentMode != null && typeof(document.documentMode) != "undefined" && document.documentMode < 7) {
                DOMwidth = document.documentElement.scrollWidth + bgLeft;
                DOMheight = document.documentElement.scrollHeight + bgTop;
            } else {
                DOMwidth = document.documentElement.clientWidth + bgLeft;
                DOMheight = document.documentElement.clientHeight + bgTop;
            }
            var maxL = DOMwidth - oDrag.offsetWidth;
            var maxT = DOMheight - oDrag.offsetHeight;

            //李强修改，修改拖拽方法，针对样式position: fixed;相对于浏览器窗口进行定位
            bgLeft = bgTop = 0;
            if (DOMwidth < document.body.clientWidth) {
                maxL = document.body.clientWidth - oDrag.offsetWidth;
            }
            if (DOMheight < document.body.clientHeight) {
                maxT = document.body.clientHeight - oDrag.offsetHeight;
            }


            iL >= maxL && (iL = maxL);
            iT >= maxT && (iT = maxT);
            iL <= bgLeft && (iL = bgLeft);
            iT <= bgTop && (iT = bgTop);
//            alert(iL + ',' + iT);
            oDrag.style.left = iL + "px";
            oDrag.style.top = iT + "px";
            oDrag.style.margin = 0 + "px";

            return false
        };

        document.onmouseup = function () {
            document.onmousemove = null;
            document.onmouseup = null;
            this.releaseCapture && this.releaseCapture()
            handle.style.cursor = ""; //鼠标移动对象时的样式
        };
        this.setCapture && this.setCapture();
        return false
    };
    /*//最大化按钮
     oMax.onclick = function ()
     {
     oDrag.style.top = oDrag.style.left = 0;
     oDrag.style.width = document.documentElement.clientWidth - 2 + "px";
     oDrag.style.height = document.documentElement.clientHeight - 2 + "px";
     this.style.display = "none";
     oRevert.style.display = "block";
     };
     //还原按钮
     oRevert.onclick = function ()
     {
     oDrag.style.width = dragMinWidth + "px";
     oDrag.style.height = dragMinHeight + "px";
     oDrag.style.left = (document.documentElement.clientWidth - oDrag.offsetWidth) / 2 + "px";
     oDrag.style.top = (document.documentElement.clientHeight - oDrag.offsetHeight) / 2 + "px";
     this.style.display = "none";
     oMax.style.display = "block";
     };
     //最小化按钮
     oMin.onclick = oClose.onclick = function ()
     {
     oDrag.style.display = "none";
     var oA = document.createElement("a");
     oA.className = "open";
     oA.href = "javascript:;";
     oA.title = "还原";
     document.body.appendChild(oA);
     oA.onclick = function ()
     {
     oDrag.style.display = "block";
     document.body.removeChild(this);
     this.onclick = null;
     };
     };
     //阻止冒泡
     oMin.onmousedown = oMax.onmousedown = oClose.onmousedown = function (event)
     {
     this.onfocus = function () {this.blur()};
     (event || window.event).cancelBubble = true
     };*/
}


/*---表单校验---*/
function __metar_check_form(__form) {
    __show_metar_loading();
    var __check_result = true;
    var __elements;
    var __first_null_element;
    if (__form) {
        __elements = $(__form).find('.__metar_check_form');
    } else {
        __elements = $('.__metar_check_form');
    }
    for (var i = 0; i < __elements.length; i++) {
        var __element = $(__elements[i]);
        if (__element) {
            var __current_element_null = false;
            var __value = __element.val();
            if (__value == '') {
                __current_element_null = true;
            } else {
                __value = trim(__value);
                if (__value == '') {
                    __current_element_null = true;
                } else {
                    __element.removeClass('metar_not_null');
                }
            }
            if (__current_element_null) {
                __element.addClass('metar_not_null');
                __check_result = false;
                if (!__first_null_element) {
                    __first_null_element = __element;
                }
            }
        }
    }
    if (__first_null_element) {
        $("html,body").animate({scrollTop: __first_null_element.offset().top});
        if (!__first_null_element.attr('onfocus') || __first_null_element.attr('onfocus').indexOf('WdatePicker') == -1) {
            __first_null_element.focus();
        }
        __first_null_element = '';
    }
    if (!__check_result) {
        __hide_metar_loading();
        alert('请完成必填项');
    }
    return __check_result;
}

/*---生成table-tree---*/
function __init_metar_tree_table(__settings) {
    __show_metar_loading();
    if ($('#' + __settings.container).html() != '') {
        return;
    }
//    setTimeout(function(){
    var __tree_table_loading = $('<table class="table table-horizon table-fixed"></table>');
    var __tree_table_tbody_loading = $('<tbody></tbody>');
    var __tree_table_tr_loading = $('<tr></tr>');
    for (var j = 0; j < __settings.columns.length; j++) {
        var __tree_table_th = $('<th></th>');
        var __column = __settings.columns[j];
        __tree_table_th.append(__column.title);
        if (__column.width) {
            __tree_table_th.width(__column.width);
        }
        __tree_table_tr_loading.append(__tree_table_th);
    }
    __tree_table_tbody_loading.append(__tree_table_tr_loading);
    __tree_table_loading.append(__tree_table_tbody_loading);
    $('#' + __settings.container).append(__tree_table_loading);
//    } , 1);

    $.ajax({
        url: __settings.loadUrl,
        method: 'POST',
        async: true,
        dataType: 'json',
        success: function (response) {
            if (response) {
                var showOnly = response.showOnly;
                var data = response.data;
//                console.info(response);
                var __tree_table = $('<table class="table table-horizon table-fixed"></table>');
                var __tree_table_tbody = $('<tbody></tbody>');
                for (var i = 0; i < data.length; i++) {
                    var __treeNode = data[i];
                    __treeNode.showOnly = showOnly;
                    var __tree_table_tr = $('<tr data-tt-id="' + __treeNode.id + '" data-tt-parent-id="' + __treeNode.parentId + '"></tr>');
                    for (var j = 0; j < __settings.columns.length; j++) {
                        var __tree_table_td = $('<td></td>');
                        var __column = __settings.columns[j];
                        if (showOnly && __column.hiddenInShow) {
                            continue;
                        }
                        var __column_value;
                        if (__column_value = __treeNode[__column.column] || __column.value) {
                            if (__column.wrapFunction) {
                                __column_value = __column.wrapFunction(__treeNode, __column_value);
                            }
                            if (__column.width) {
                                __tree_table_td.width(__column.width);
                                if (__column.overflowHidden) {
                                    var __span = $('<span class="overflow-hidden">' + __column_value + '</span>');
                                    __span.width(__column.width);
                                    __tree_table_td.append(__span);
                                } else {
                                    __tree_table_td.append('<span>' + __column_value + '</span>');
                                }
                            } else {
                                __tree_table_td.append('<span>' + __column_value + '</span>');
                            }
//                            __tree_table_td.append('<span>' + __column_value + '</span>');
                        }

                        __tree_table_tr.append(__tree_table_td);
                    }
                    __tree_table_tbody.append(__tree_table_tr);
                }

                var __tree_table_tr_title = $('<tr></tr>');
                for (var j = 0; j < __settings.columns.length; j++) {
                    var __tree_table_th = $('<th></th>');
                    var __column = __settings.columns[j];
                    if (showOnly && __column.hiddenInShow) {
                        continue;
                    }
                    __tree_table_th.append(__column.title);
                    if (__column.width) {
                        __tree_table_th.width(__column.width);
                    }
                    __tree_table_tr_title.append(__tree_table_th);
                }
                __tree_table_tbody.prepend(__tree_table_tr_title);
                __tree_table.append(__tree_table_tbody);
                __tree_table.treetable({
                    expandable: true,
                    initialState: 'collapsed'
                });
                $('#' + __settings.container).empty().append(__tree_table);
                if (__settings.callback) {
                    __settings.callback(response);
                }
                __hide_metar_loading();
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            __hide_metar_loading();
        }
    })
}


/*---删除左右两端的空格---*/
function trim(str) {
    return str.replace(/(^\s*)|(\s*$)/g, "");
}

/*---json 转 String---*/
function JsonObjectToString(o) {
    var arr = [];
    var fmt = function (s) {
        if (typeof s == 'object' && s != null) return JsonObjectToString(s);
        return /^(string|number)$/.test(typeof s) ? "\"" + s + "\"" : s;
    };

    if (o instanceof Array) {
        for (var i in o) {
            arr.push(fmt(o[i]));
        }
        return '[' + arr.join(',') + ']';

    }
    else {
        for (var i in o) {
            arr.push("\"" + i + "\":" + fmt(o[i]));
        }
        return '{' + arr.join(',') + '}';
    }
}

function __init_query_select(select) {
    var value = select.val();
//    alert(value);
}

