<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../base.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>黑马应用配置管理</title>
</head>
<body>
    <div id="frameContent" class="content-wrapper" style="margin-left:0px;">
        <section class="content-header">
            <h1>
                配置管理
                <small>添加配置</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="all-admin-index.html"><i class="fa fa-dashboard"></i> 首页</a></li>
            </ol>
        </section>
        <section class="content">
            <div class="box-body">
                <div class="nav-tabs-custom">
                    <ul class="nav nav-tabs">
                        <li class="active">
                            <a href="#tab-form" data-toggle="tab">添加配置</a>
                        </li>
                    </ul>
                    <div class="tab-content">
                        <form id="editForm" action="${ctx}/config/add.do" method="post">
                            <input type="hidden" name="id" value="${id}">
                            <div class="tab-pane active" id="tab-form">
                                <div class="row data-type">
                                    <div class="col-md-2 title">配置名称</div>
                                    <div class="col-md-10 data">
                                        <input type="text" class="form-control" placeholder="配置名称" name="configName">
                                    </div>
                                    <div class="col-md-2 title">配置值</div>
                                    <div class="col-md-10 data line-height36">
                                        <input type="text" class="form-control" placeholder="配置值" name="configValue">
                                    </div>
                                    <div class="col-md-2 title"></div>
                                    <div class="col-md-10 data text-center">
                                        <button type="button" onclick='document.getElementById("editForm").submit()'  class="btn bg-maroon">保存</button>
                                        <button type="button" class="btn bg-default" onclick="history.back(-1);">返回</button>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

        </section>
    </div>
</body>

</html>