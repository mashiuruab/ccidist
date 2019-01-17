<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<table style="width: 100%" class="table table-striped table-bordered table-hover">
    <tr>
        <th class="span1"><s:property value="getText('message.design.name')" /></th>
        <th class="span1"><s:property value="getText('message.epubName')" /></th>
        <th class="span1"><s:property value="getText('message.pre.generate')" /></th>
        <th class="span1"><s:property value="getText('message.os')" /></th>
        <th class="span1"><s:property value="getText('message.osv')" /></th>
        <th class="span1"><s:property value="getText('message.reader')" /></th>
        <th class="span1"><s:property value="getText('message.device.name')" /></th>
        <th class="span2"><s:property value="getText('message.start.date')" /> (UTC)</th>
        <th class="span1"><s:property value="getText('message.end.date')" /> (UTC)</th>
        <th class="span1"><s:property value="getText('message.button.edit')" /></th>
        <th class="span1"><s:property value="getText('message.button.delete')" /></th>
    </tr>

    <s:iterator value="driverInfoList" id="driver">
        <tr>
            <td><s:property value="%{#driver.designToEpubMapper.designName}" /></td>
            <td><s:property value="%{#driver.designToEpubMapper.epubName}" /></td>
            <td><s:property value="%{#driver.preGenerate}" /></td>
            <td><s:property value="%{#driver.os}" /></td>
            <td><s:property value="%{#driver.osVersion}" /></td>
            <td><s:property value="%{#driver.reader}" /></td>
            <td><s:property value="%{#driver.deviceName}" /></td>
            <td><s:date name="%{#driver.startDate}" format="yyyy-MM-dd" /></td>
            <td><s:date name="%{#driver.endDate}" format="yyyy-MM-dd" /></td>
            <td><a
                href="<s:url action="driverEdit"><s:param name="driverInfoId"><s:property value="id"/></s:param><s:param name="publicationId" value="%{publicationId}"/></s:url>"><s:property
                        value="getText('message.button.edit')" /></a></td>
            <td><a
                href="<s:url action="driverDelete"><s:param name="driverInfoId"><s:property value="id"/></s:param><s:param name="publicationId" value="%{publicationId}"/></s:url>" onclick= 'return confirm("<s:property value="getText('message.confirm.delete.driver')" />")'><s:property
                        value="getText('message.button.delete')" /></a></td>
        </tr>
    </s:iterator>
    <tr>
        <td colspan="11"><input class="btn" type="button"
            name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="issueList"><s:param name="publicationId" value="%{publicationId}"/></s:url>'" />&nbsp;&nbsp;
            <input class="btn" type="button" name="<s:property value="getText('message.create.driver')" />"
            value="<s:property value="getText('message.create.driver')" />"
            onclick="window.location='<s:url action="driverEdit"><s:param name="publicationId" value="%{publicationId}"/></s:url>'" /></td>
    </tr>
</table>
