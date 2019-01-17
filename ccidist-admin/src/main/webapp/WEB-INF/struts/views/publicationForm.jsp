<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div class="well">
    <s:form cssClass="form-horizontal" validate="true">
        <s:hidden name="publicationCreateDate" value="%{publicationCreateDate}" />
        <s:hidden name="organizationId" value="%{organizationId}" />

        <s:textfield cssClass="span6" disabled="true" key="message.orgId" value="%{organizationId}" />

        <s:if test="%{createdDate > 0}">
            <s:textfield cssClass="span6" disabled="true" requiredLabel="true" key="message.publication.id"
                value="%{publicationId}" />
            <s:hidden name="publicationId" value="%{publicationId}" />
        </s:if>
        <s:else>
            <s:textfield cssClass="span6" key="message.publication.id" requiredLabel="true" name="publicationId" />
        </s:else>

        <s:textfield cssClass="span6" requiredLabel="true" key="message.publication.name" name="publicationName"
            value="%{publicationName}" />

        <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="publicationList"><s:param name="organizationId"><s:property value="organizationId"/></s:param></s:url>'" />&nbsp;&nbsp;
        <s:submit cssClass="btn" key="message.submit" name="submit" method="saveOrUpdate" />
    </s:form>
</div>
