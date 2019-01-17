<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<table style="widt: 100%" class="table table-striped table-bordered table-hover">
    <tr>
        <th class="span10"><s:property value="getText('message.publication.name')" /></th>
        <th class="span1"><s:property value="getText('message.button.edit')" /></th>
        <th class="span1"><s:property value="getText('message.button.delete')" /></th>
    </tr>
    <s:iterator value="publications" id="publication">
        <tr>
            <td align="center" class="span10"><a
                href="<s:url action="issueList"><s:param name="publicationId"><s:property value="id"/></s:param></s:url>"><s:property value="name" /></a></td>
            <td align="center" class="span1"><a
                href="<s:url action="publicationEdit"><s:param name="publicationId"><s:property value="id"/></s:param><s:param name="organizationId"><s:property value="organization.id"/></s:param></s:url>"><s:property
                        value="getText('message.button.edit')" /></a></td>
            <td align="center" class="span1"><a
                href="<s:url action="publicationDelete"><s:param name="publicationId"><s:property value="id"/></s:param><s:param name="organizationId"><s:property value="organization.id"/></s:param></s:url>"
                onclick='return confirm("<s:property value="getText('message.confirm.delete.publication')" />")'> <s:property
                        value="getText('message.button.delete')" /></a></td>
        </tr>
    </s:iterator>
    <tr>
        <td colspan="3"><input class="btn" type="button"
            name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="orgList"></s:url>'" />&nbsp;&nbsp;<input class="btn"
            type="button" name="<s:property value="getText('message.publicationList.new')" />"
            value="<s:property value="getText('message.publicationList.new')" />"
            onclick="window.location='<s:url action="publicationEdit"><s:param name="organizationId"><s:property value="organizationId"/></s:param></s:url>'" />
        </td>
    </tr>
</table>
