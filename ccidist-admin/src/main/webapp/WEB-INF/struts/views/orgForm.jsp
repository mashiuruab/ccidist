<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div class="well">
    <s:form cssClass="form-horizontal" validate="true">
        <s:hidden name="createdDate" value="%{createdDate}" />
        
        <s:if test="%{createdDate > 0}">
            <%-- This is an Edit form --%>
            <s:hidden name="organizationId" value="%{organizationId}" />
            <s:textfield key="message.orgId" requiredLabel="true" name="organizationId" disabled="true"
                cssClass="span8" />
        </s:if>
        <s:else>
            <s:textfield key="message.orgId" name="organizationId" requiredLabel="true" cssClass="span8" />
        </s:else>

        <s:textfield key="message.orgName" requiredLabel="true" name="organizationName"
            value="%{organizationName}" cssClass="span8" />
            
        <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="orgList"></s:url>'" />&nbsp;&nbsp;
        <s:submit cssClass="btn" key="message.submit" name="submit" method="saveOrUpdate" />
    </s:form>
</div>
