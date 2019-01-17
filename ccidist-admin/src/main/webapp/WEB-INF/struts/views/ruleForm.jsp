<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
         language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<script type="text/javascript">
    var currentDesignName = '<s:property value="%{designName}" />'
    var currentEpubName = '<s:property value="%{epubName}" />'
    var designToEpubMap = {
        <s:iterator value="designToEpubMapper" status="designToEpubMapperStatus">
        '<s:property escapeJavaScript="true" value="key"/>' : [
            <s:iterator value="value" status="valueStatus">'<s:property escapeJavaScript="true" />'<s:if test="!#valueStatus.last">,</s:if></s:iterator>
        ]<s:if test="!#designToEpubMapperStatus.last">,</s:if>
        </s:iterator>
    };

    function addOptions(selectContainer, values) {
        if (values != null) {
            $.each(values, function(index, val) {
                if (val == currentEpubName)
                    selectContainer.append($("<option>", {value: val, html: val, selected: true}));
                else
                    selectContainer.append($("<option>", {value: val, html: val, selected: false}));
            });
        }
    }
    $(document).ready(function() {
        var epubValues = designToEpubMap[currentDesignName];
        var epubNameSelect = $('[name="epubName"]');

        epubNameSelect.html("");
        addOptions(epubNameSelect, epubValues);

        $("select[name='designName']").change(function() {
            var currentSelection = $(this).val();
            var epubValues = designToEpubMap[currentSelection];
            var epubNameSelect = $('[name="epubName"]');

            epubNameSelect.html("");
            addOptions(epubNameSelect, epubValues);
        });

    });
</script>

<div class="well">
<s:form cssClass="form-horizontal" validate="true">
    <s:hidden name="ruleId" value="%{ruleId}"/>
    <s:hidden name="publicationId" value="%{publicationId}"/>

    <s:textfield cssClass="span6" disabled="true" key="message.publication.id" name="publicationId"
                 value="%{publicationId}"/>

    <s:textfield cssClass="span6" key="message.device.name" name="deviceName" value="%{deviceName}"/>

    <s:textfield cssClass="span3" key="message.width" name="width" value="%{width > 0 ? width : ''}"/>
    <s:textfield cssClass="span3" key="message.height" name="height" value="%{height > 0 ? height: ''}"/>

    <s:textfield cssClass="span6" key="message.os" name="os" value="%{os}"/>
    <s:textfield cssClass="span6" key="message.osv" name="osv" value="%{osv}"/>
    <s:textfield cssClass="span6" key="message.reader" name="readerVersion" value="%{readerVersion}"/>

    <s:select cssClass="span6" id="designName" label="Design Name" headerKey="" headerValue="Select Design Name"
              list="uniqueDesignNameList" value="%{designName}" requiredLabel="true" name="designName"/>

    <s:select cssClass="span6" id="epubName" label="Epub Name" headerKey="" headerValue="Select Epub Name"
              list="#{''}" requiredLabel="true" name="epubName"/>

    <input class="btn" type="button" name="<s:property value="getText('message.back')" />"
           value="<s:property value="getText('message.back')" />"
           onclick="window.location='<s:url action="rules"><s:param name="publicationId" value="%{publicationId}"></s:param></s:url>'"/>
    &nbsp;
    <s:submit cssClass="btn" key="message.submit" name="submit" method="saveOrUpdate"/>
</s:form>
</div>
