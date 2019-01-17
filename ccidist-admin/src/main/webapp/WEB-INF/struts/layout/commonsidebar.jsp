<%@ page session="false" contentType="application/xhtml+xml" pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true" language="java"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-bootstrap-tags" prefix="sb"%>
<%@ taglib uri="/struts-tags" prefix="s"%>


<s:if test="%{superUser}">
    <li class="nav-header">Administrative Actions</li>
    <%-- Java melody related links. --%>
    <s:if test="%{config.monitoringEnabled}">
        <li><a href="<s:url action="monitor"><s:param name="enabled" value="false"/></s:url>"><s:property
                    value="getText('message.disable.monitoring')" /></a></li>
        <li><a href="<s:url value="/monitoring"/>" target="_blank"><s:text name="message.sidebar.adminStats" /></a></li>

        <%-- The assumption here is that webserviceURL has a trailing slash. --%>
        <li><a href="<s:property value="%{config.webserviceURL}" />monitoring"  target="_blank"><s:text
                    name="message.sidebar.wsStats" /></a></li>
    </s:if>
    <s:else>
        <li><a href="<s:url action="monitor"><s:param name="enabled" value="true"/></s:url>"><s:property
                    value="getText('message.enable.monitoring')" /></a></li>
    </s:else>
    <%-- User list --%>
    <li><a href="<s:url action="userList"/>"><s:property value="getText('message.users')" /></a></li>
</s:if>
