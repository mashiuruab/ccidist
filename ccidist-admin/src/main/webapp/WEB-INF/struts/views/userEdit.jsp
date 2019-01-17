<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="well">
    <s:form cssClass="form-horizontal">
        <s:hidden name="userId" value="%{userId}" />
        <s:textfield key="message.userName" requiredLabel="true" name="userName" value="%{userName}"
            cssClass="span6" />

        <s:if test="%{createForm}">
            <s:textfield key="message.loginName" requiredLabel="true" name="loginName" value="%{loginName}"
                cssClass="span6" />
        </s:if>
        <s:else>
            <s:textfield readonly="true" key="message.loginName" requiredLabel="true" name="loginName"
                value="%{loginName}" cssClass="span6" />
        </s:else>

        <s:password key="message.password" name="password" cssClass="span6" />
        <s:password key="message.retype.password" name="retypePassword" cssClass="span6" />

        <%-- If Super User is working with some other user, ONLY then show organization & role info --%>
        <s:if test="%{createForm || (superUser && loginName != 'admin')}">
            <s:select key="message.organization" cssClass="dropdown span4" headerKey=""
                headerValue="Please Select Organization" value="%{organizationId}" list="organizationList"
                name="organizationId" listKey="id" listValue="name" requiredLabel="true" />
            <s:select key="message.role" cssClass="dropdown span4" headerKey="-1"
                headerValue="Please Select Role" value="%{roleId}" list="roleList" name="roleId" listKey="id"
                listValue="name" requiredLabel="true" />
        </s:if>

        <s:if test="%{superUser}">
            <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
                value="<s:property value="getText('message.back')" />"
                onclick="window.location='<s:url action="userList"/>'" />
        </s:if>
        <s:else>
            <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
                value="<s:property value="getText('message.back')" />"
                onclick="window.location='<s:url action="orgList"/>'" />
        </s:else>
        &nbsp;
        <s:submit cssClass="btn" key="message.submit" theme="simple" name="submit" method="saveOrUpdate" />
    </s:form>
</div>
