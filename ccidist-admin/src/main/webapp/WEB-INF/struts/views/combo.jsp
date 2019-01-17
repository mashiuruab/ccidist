<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<script type="text/javascript">
$(document).ready(function() {
    $("#designName").select2({
        maximumSelectionSize: 1,
        tags:[
            <s:iterator value="uniqueDesignNameList" id="un" status="counter">
                '<s:property value="%{#un}" escapeJavaScript="true" />',
            </s:iterator>
        ]
    });

    <%-- Create the design to epubName map here --%>
    var designToEpubMap = {
    <s:iterator value="publicationEpubMapping">
        '<s:property escapeJavaScript="true" value="key"/>' : [
            <s:iterator value="value">'<s:property escapeJavaScript="true" />',</s:iterator>
        ],
    </s:iterator>
    };

    var allEpubMapping = {
    <s:iterator value="allEpubMapping">
        '<s:property escapeJavaScript="true" value="key"/>' : [
            <s:iterator value="value">'<s:property escapeJavaScript="true" />',</s:iterator>
        ],
    </s:iterator>
    };

    var initialDesign = '<s:property value="%{designName}" escapeJavaScript="true"/>';
    var initialEpub = '<s:property value="%{epubName}" escapeJavaScript="true"/>';

    $('#epubName').select2({
        maximumSelectionSize: 1,
        tags: function(query) {
            var selectedDesign = $('#designName').val();
            var allEpubNames = allEpubMapping[selectedDesign];
            var alreadyUsed = designToEpubMap[selectedDesign];

            var autoCompletion = [];
            if (allEpubNames != null) {
                $.each(allEpubNames, function(index, value) {
                    if ($.inArray(value, alreadyUsed) == -1) {
                        autoCompletion.push(value);
                    }
                });
            }

            if (initialDesign == selectedDesign && initialEpub != '') {
                autoCompletion.push(initialEpub);
            }

            return autoCompletion;
        }
    });
});
</script>

<%-- The width style attribute has to be provided to support responsive design with select2. --%>
<s:textfield cssStyle="width: 50%" cssClass="span6" requiredLabel="true" key="message.designName"
    name="designName" value="%{designName}" id="designName" />
<s:textfield cssStyle="width: 50%" cssClass="span6" requiredLabel="true" key="message.epubName" name="epubName"
    value="%{epubName}" id="epubName" />
