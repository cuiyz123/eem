<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    String __parent_iframe = request.getParameter("__parent_iframe");
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<meta http-equiv="X-UA-Compatible" content="IE=8">
<style>

</style>
<script type="text/javascript">
    var _PATH = "<%=path%>";
    var _BASEPATH = "<%=basePath%>";
    var _parent_iframe = "<%=__parent_iframe%>";
    var _globalUniqueID = "${globalUniqueID}";

</script>
<%--DT Grid begin--%>
<!-- jQuery -->
<script type="text/javascript"
        src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/jquery/jquery.min.js"></script>
<script>
    $.ajaxSetup({cache: false});
    $(function () {
        $.ajaxSetup({cache: false});
    });
    $(document).ready(function(){
        $('.click-loading').click(function(){
            __show_metar_loading();
        });
    });

</script>
<!-- bootstrap -->
<script type="text/javascript"
        src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/js/bootstrap.min.js"></script>
<link rel="stylesheet" type="text/css"
      href="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/css/bootstrap.min.css"/>
<%--  <!--[if lt IE 9]>
  <script type="text/javascript"  src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/plugins/ie/html5shiv.js"/>
  <script type="text/javascript"  src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/plugins/ie/respond.js"/>
  <![endif]-->
  <!--[if lt IE 8]>
  <script type="text/javascript"  src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/plugins/ie/json2.js"></script>
  <![endif]-->--%>
<!-- font-awesome -->
<link rel="stylesheet" type="text/css"
      href="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/fontAwesome/css/font-awesome.min.css" media="all"/>
<!-- dtGrid -->
<script type="text/javascript" src="<%=path%>/component/jquery.dtGrid.v1.1.9/jquery.dtGrid.js"></script>
<script type="text/javascript" src="<%=path%>/component/jquery.dtGrid.v1.1.9/i18n/zh-cn.js"></script>
<link rel="stylesheet" type="text/css" href="<%=path%>/component/jquery.dtGrid.v1.1.9/jquery.dtGrid.css"/>

<!-- datePicker -->
<%--<script type="text/javascript" src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/datePicker/WdatePicker.js"--%>
<%--defer="defer"></script>--%>
<%--<link rel="stylesheet" type="text/css"--%>
<%--href="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/datePicker/skin/WdatePicker.css"/>--%>
<%--<link rel="stylesheet" type="text/css"--%>
<%--href="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/datePicker/skin/whyGreen/datepicker.css"/>--%>
<script type="text/javascript" src="<%=path%>/component/My97DatePicker/WdatePicker.js"></script>
<link rel="stylesheet" type="text/css" href="<%=path%>/component/My97DatePicker/skin/WdatePicker.css"/>
<%--DT Grid end--%>

<!-- zui -->
<%--<link href="<%=path%>/component/zui-master/dist/css/zui.min.css" rel="stylesheet"/>--%>
<!-- jQuery (ZUI中的Javascript组件依赖于jQuery) -->
<%--<script src="<%=path%>/component/zui-master/dist/js/jquery.min.js"></script>--%>
<!-- ZUI Javascript组件 -->
<%--<script src="<%=path%>/component/zui-master/dist/js/zui.min.js"></script>--%>
<%--<link href="<%=path%>/component/zui-master/dist/lib/datetimepicker/datetimepicker.min.css" rel="stylesheet"/>--%>
<%--<script src="<%=path%>/component/zui-master/dist/lib/datetimepicker/datetimepicker.js"></script>--%>
<script src="<%=path%>/base/_js/require.js"></script>
<link rel="stylesheet" type="text/css" href="<%=path%>/base/_css/metarnet.css"/>
<link rel="stylesheet" href="<%=path%>/component/ztree/css/zTreeStyle.css" type="text/css">
<script type="text/javascript" src="<%=path%>/component/ztree/js/jquery.ztree.core-3.5.js"></script>
<script type="text/javascript" src="<%=path%>/component/ztree/js/jquery.ztree.excheck-3.5.js"></script>
<script type="text/javascript" src="<%=path%>/component/ztree/js/jquery.ztree.exedit.js"></script>
<script type="text/javascript" src="<%=path%>/component/jquery-uploadify/jquery.uploadify.min.js"></script>
<meta name="viewport" content="width=device-width, location=no , initial-scale=1.0, maximum-scale=1.0, minimun-scale=1.0 , minimal-ui , user-scalable=no, target-densitydpi=device-dpi"/>
<div id="__progress" class="progress">
    <div id="__progress_bar" class="progress-bar" role="progressbar" aria-valuenow="1" aria-valuemin="0" aria-valuemax="100">

    </div>
</div>