<%@ page session="false" contentType="application/xhtml+xml" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
         language="java"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-bootstrap-tags" prefix="sb"%>
<%@ taglib uri="/struts-tags" prefix="s"%>


<div class="span3">
    <div class="well sidebar-nav">
        <ul class="nav nav-list">
            <li class="nav-header">Available Actions</li>
            <li><a href="<s:url action="driverList"><s:param name="publicationId"><s:property value="%{publicationId}"/></s:param></s:url>"><s:property value="getText('message.driver.info')" /></a></li>
            <li><a href="<s:url action="issueList"><s:param name="publicationId" value="%{publicationId}"/></s:url>"><s:property value="getText('message.issue.link')"/></a></li>
            <li><a href="<s:url action="rules"><s:param name="publicationId"><s:property value="%{publicationId}"/></s:param></s:url>"><s:property value="getText('message.rules')" /></a></li>
            <jsp:include page="commonsidebar.jsp"/>
        </ul>
    </div>
</div>
