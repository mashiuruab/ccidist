<%@ page session="false" contentType="text/html" pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true" language="java"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<footer class="container-fluid">
    <div class="row-fluid">
        <div class="span6">
            <span>
                <s:url value="/version" var="versionURL"/>
                <s:text name="message.footer.version">
                    <s:param value="%{versionURL}"/>
                    <s:param value="%{config.versionManager.version}" />
                </s:text>
            </span>
        </div>
        
        <div class="span6 pull-right" style="text-align: right;">
            <span><s:text name="message.footer.copyright"/></span>
        </div>
    </div>
</footer>
