<%-- FIXME: This page has text/html because otherwise browsers refuse to render this as HTML. Sigh.... :-( --%>
<%@ page session="false" contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/struts-bootstrap-tags" prefix="sb"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><s:property value="%{pageTitle}"/></title>

<style type="text/css">
body {
    padding-top: 50px;
    padding-bottom: 20px;
    background-color: #EEEEEE;
}
</style>

<link rel="shortcut icon" href="<s:url value="/static/img/favicon.ico"/>" />

<%-- JQuery is dependency of all of bootstrap. --%>
<script type="text/javascript" src="<s:url value="/static/js/jquery.js"/>"></script>
<sb:head includeStylesResponsive="true" />

<style type="text/css">
.sidebar-nav {
    padding: 9px 0;
}

@media ( max-width : 980px) {
    /* Enable use of floated navbar text */
    .nav.pull-right {
        float: none;
    }
}

.navbar .brand {
    padding-left: 60px;
    background-image: url('<s:url value="/static/img/logo.png"/>');
    background-repeat: no-repeat;
}
</style>

<%-- The CSS files. --%>
<link rel="stylesheet" href="<s:url value="/static/css/datepicker.css"/>"></link>
<link rel="stylesheet" href="<s:url value="/static/select2340/select2.css"/>"></link>

<%-- The javascript files. --%>
<script type="text/javascript" src="<s:url value="/static/js/bootstrap-datepicker.js"/>"></script>
<script type="text/javascript" src="<s:url value="/static/select2340/select2.min.js"/>"></script>
<script type="text/javascript" src="<s:url value="/static/js/admin.js"/>"></script>
</head>

<body>
    <tiles:insertAttribute name="header" />

    <div class="container-fluid">
        <div class="row-fluid">
            <tiles:insertAttribute name="sidebar" ignore="true"/>
            
            <div class="span9">
                <div class="row-fluid">
                    
                    <s:actionerror/>
                    <s:actionmessage/>

                    <h4><s:property value="%{pageTitle}"/></h4>
                    <tiles:insertAttribute name="body" />
                </div>
            </div>
        </div>
    </div>
    <hr>
    
    <tiles:insertAttribute name="footer" />
</body>
</html>
