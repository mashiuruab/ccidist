<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div class="alert alert-error span11">
    <h1>
        <s:property value="getText('message.error')" />
    </h1>
    <p>
        <strong><s:property value="%{exception.message}" /></strong>
    </p>
</div>
