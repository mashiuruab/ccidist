<%@ page session="false" contentType="application/xhtml+xml" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-bootstrap-tags" prefix="sb"%>
<%@ taglib uri="/struts-tags" prefix="s"%>


<s:if test="%{superUser}">
    <div class="span3">
        <div class="well sidebar-nav">
            <ul class="nav nav-list">
                <jsp:include page="commonsidebar.jsp"/>
            </ul>
        </div>
    </div>
</s:if>
