<%@ page session="false" contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/struts-bootstrap-tags" prefix="sb"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
            </button>

            <a class="brand" href="<s:url action="orgList"/>"><s:text name="message.header.title" /></a>

            <div class="nav-collapse collapse">
                <ul class="nav pull-right">
                    <li><a
                        href="<s:url action="userEdit"><s:param name="userId" value="%{loggedInUser.id}"/></s:url>">
                            <s:text name="message.header.welcome">
                                <s:param value="%{loggedInUser.name}" />
                            </s:text>
                    </a></li>
                </ul>

                <ul class="nav">
                    <li><a href="<s:url value="/"/>"><s:text name="message.header.home" /></a></li>
                    <li><a href="<s:url value="/api/"/>" target="_blank"><s:text name="message.header.api" /></a></li>
                    <%-- Create a damned logout URL :-( --%>
                    <s:url forceAddSchemeHostAndPort="true" value="/" var="homeURL"></s:url>
                    <c:set var="logoutURL">${fn:replace(homeURL, "http://", "http://newuser:newpass@")}</c:set>
                    <li><a href="${logoutURL}"><s:text name="message.header.logout" /></a></li>
                </ul>
            </div>
        </div>
    </div>
</div>
