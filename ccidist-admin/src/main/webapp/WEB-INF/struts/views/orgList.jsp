<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<table class="table table-striped table-bordered table-hover">
    <tr>
        <th class="span10"><s:property value="getText('message.orgName')" /></th>
        <s:if test="%{superUser}">
            <th class="span1"><s:property value="getText('message.button.edit')" /></th>
            <th class="span1"><s:property value="getText('message.button.delete')" /></th>
        </s:if>
    </tr>
    <s:iterator value="organizationsBasedOnUser" id="organization">
        <tr>
            <td class="span10"><a
                href="<s:url action="publicationList"><s:param name="organizationId"><s:property value="id"/></s:param></s:url>"><s:property
                        value="name" /></a></td>
            <s:if test="%{superUser}">
                <td class="span1"><a
                    href="<s:url action="orgEdit"><s:param name="organizationId"><s:property value="id"/></s:param></s:url>"><s:property
                            value="getText('message.button.edit')" /></a></td>
                <td class="span1"><a
                    href="<s:url action="orgDelete"><s:param name="organizationId"><s:property value="id"/></s:param></s:url>"
                    onclick='return confirm("<s:property value="getText('message.confirm.delete.organization')"/>")'>
                        <s:property value="getText('message.button.delete')" />
                </a></td>
            </s:if>
        </tr>
    </s:iterator>
    <s:if test="%{superUser}">
        <tr>
            <td colspan="3"><input class="btn" type="button"
                name="<s:property value="getText('message.orgList.new')" />"
                value="<s:property value="getText('message.orgList.new')" />"
                onclick="window.location='<s:url action="orgEdit"></s:url>'" /></td>
        </tr>
    </s:if>
</table>
