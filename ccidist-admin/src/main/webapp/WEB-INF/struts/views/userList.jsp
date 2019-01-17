<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<table style="width: 100%;" class="table table-striped table-bordered table-hover">
    <tr>
        <th class="span3"><s:property value="getText('message.name')" /></th>
        <th class="span3"><s:property value="getText('message.loginName')" /></th>
        <th class="span3"><s:property value="getText('message.organization')" /></th>
        <th class="span1"><s:property value="getText('message.role')" /></th>
        <th class="span1"><s:property value="getText('message.button.edit')" /></th>
        <th class="span1"><s:property value="getText('message.button.delete')" /></th>
    </tr>
    <s:iterator value="userList" var="user" id="user">
        <tr>
            <td><s:property value="name" /></td>
            <td><s:property value="loginName" /></td>
            <td><s:property value="userPrivilege.organization.name" /></td>
            <td><s:property value="userPrivilege.role.name" /></td>

            <td><a
                href="<s:url action="userEdit"><s:param name="userId"><s:property value="id"/></s:param></s:url>"><s:property
                        value="getText('message.button.edit')" /></a></td>
            <td><s:if test="%{#user.loginName != 'admin'}">
                    <a
                        href="<s:url action="userDelete"><s:param name="userId"><s:property value="id"/></s:param></s:url>"
                        onclick='return confirm("<s:property value="getText('message.confirm.delete')" />")'><s:property
                            value="getText('message.button.delete')" /></a>
                </s:if> <s:else>&nbsp;</s:else></td>
        </tr>
    </s:iterator>

    <tr>
        <td colspan="6"><input class="btn" type="button"
            name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="orgList"></s:url>'" />&nbsp;&nbsp;<input class="btn" type="button"
            name="<s:property value="getText('message.createUser')" />"
            value="<s:property value="getText('message.createUser')" />"
            onclick="window.location='<s:url action="userEdit"></s:url>'" /></td>
    </tr>
</table>
