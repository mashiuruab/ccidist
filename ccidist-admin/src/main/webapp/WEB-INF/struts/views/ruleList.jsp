<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<table class="table table-striped table-bordered table-hover">
    <tr>
        <th class="span2"><s:property value="getText('message.epubName')" /></th>
        <th class="span2"><s:property value="getText('message.device.name')" /></th>
        <th class="span1"><s:property value="getText('message.width')" /></th>
        <th class="span1"><s:property value="getText('message.height')" /></th>
        <th class="span2"><s:property value="getText('message.os')" /></th>
        <th class="span1"><s:property value="getText('message.osv')" /></th>
        <th class="span1"><s:property value="getText('message.reader')" /></th>
        <th class="span1"><s:property value="getText('message.button.edit')" /></th>
        <th class="span1"><s:property value="getText('message.button.delete')" /></th>
    </tr>

    <s:iterator value="rulesList" id="rule" status="counter">
        <tr>
            <td><s:property value="%{#rule.designToEpubMapper.epubName}" /></td>
            <td><s:property value="%{#rule.deviceName}" /></td>
            <s:if test="%{#rule.width > 0}">
                <td><s:property value="%{#rule.width}" /></td>
            </s:if>
            <s:else>
                <td>&nbsp;</td>
            </s:else>
            <s:if test="%{#rule.height > 0}">
                <td><s:property value="%{#rule.height}" /></td>
            </s:if>
            <s:else>
                <td>&nbsp;</td>
            </s:else>
            
            <td><s:property value="%{#rule.os}" /></td>
            <td><s:property value="%{#rule.osv}" /></td>
            <td><s:property value="%{#rule.readerVersion}" /></td>
            <td>
                <a href="<s:url action="rulesForm"><s:param name="ruleId"><s:property value="id"/></s:param><s:param name="publicationId"><s:property value="%{#rule.publication.id}"/></s:param></s:url>"><s:property
                        value="getText('message.button.edit')" /></a>
            </td>
            <td>
                <a href="<s:url action="rulesDelete"><s:param name="ruleId"><s:property value="id"/></s:param><s:param name="publicationId"><s:property value="%{#rule.publication.id}"/></s:param></s:url>" onclick= 'return confirm("<s:property value="getText('message.confirm.delete')" />")'><s:property
                        value="getText('message.button.delete')" /></a>
            </td>
        </tr>
    </s:iterator>
    <tr>
        <td colspan="10"><input class="btn" type="button"
            name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="issueList"><s:param name="publicationId" value="%{publicationId}"/></s:url>'" />&nbsp;&nbsp;
            <input class="btn" type="button"
            name="<s:property value="getText('message.create.matching.rules')" />"
            value="<s:property value="getText('message.create.matching.rules')" />"
            onclick="window.location='<s:url action="rulesForm"><s:param name="publicationId" value="%{publicationId}"></s:param></s:url>'" />
        </td>
    </tr>
</table>
