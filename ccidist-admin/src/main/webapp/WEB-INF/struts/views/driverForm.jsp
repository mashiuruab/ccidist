<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<script type="text/javascript">
$(document).ready(function() {
    setupDatePickerHandlers("startDate");
    setupDatePickerHandlers("endDate");
});
</script>


<div class="well">
    <s:form cssClass="form-horizontal" validate="true">
        <s:hidden name="driverInfoId" value="%{driverInfoId}" />
        <s:hidden name="publicationId" value="%{publicationId}"/>

        <s:textfield cssClass="span6" key="message.publication.id" disabled="true"  name="publicationId" value="%{publicationId}" />
        
        <jsp:include page="/WEB-INF/struts/views/combo.jsp"/>

        <s:checkbox key="message.pre.generate" name="preGenerate" value="%{preGenerate}"/>

        <s:textfield cssClass="span6" key="message.device.name" name="deviceName" value="%{deviceName}" />
        <s:textfield cssClass="span6" key="message.os"  name="os" value="%{os}" />
        <s:textfield cssClass="span6" key="message.osv" name="osVersion" value="%{osVersion}" />
        <s:textfield cssClass="span6" key="message.reader" name="reader" value="%{reader}" />

        <s:textfield cssClass="span4 input-append date" label="Start Date (UTC)" name="startDate" value="%{startDate}"/>
        <s:textfield cssClass="span4 input-append date" label="End Date (UTC)" name="endDate" value="%{endDate}"/>

        <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
            value="<s:property value="getText('message.back')" />"
            onclick="window.location='<s:url action="driverList"><s:param name="publicationId" value="%{publicationId}"/></s:url>'" />
        &nbsp;
        <s:submit cssClass="btn" key="message.submit" name="submit" method="saveOrUpdate" />
    </s:form>
</div>
