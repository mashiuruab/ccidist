<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<script type="text/javascript">
    $(document).ready(function() {
        setupDatePickerHandlers("toDate");

        $('#select_all').click(function(ev) {
            $('.delete').prop('checked', $(this).prop('checked'));
        });
    });
</script>

<div class="well well-small">
    <s:form cssClass="form-horizontal" validate="true" method="GET">
        <div class="container-fluid span12">
            <s:hidden name="publicationId" value="%{publicationId}" />

            <div class="row-fluid">
                <div class="span6">
                    <s:textfield key="message.start" name="start" value="%{start}" cssClass="span4" />
                </div>
                <div class="span6">
                    <s:textfield key="message.limit" name="limit" value="%{limit}" cssClass="span4" />
                </div>
            </div>

            <div class="row-fluid">
                <div class="span6">
                    <s:textfield key="message.to.date" name="toDate" value="%{toDate}" cssClass="span12" />
                </div>
                <div class="span6">
                    <s:select key="message.epubName" cssClass="dropdown span12" headerKey=""
                        headerValue="Any EPub" name="epubName" value="%{epubName}"
                        list="allEpubNames" />
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <s:select key="message.issue.status" cssClass="dropdown span6" headerKey="-1"
                        headerValue="Any Status" name="issueStatus" value="%{issueStatus}" listKey="value"
                        listValue="statusName" list="issueStatusList" />
                </div>
                <div class="span6" style="text-align: center;">
                    <s:submit cssClass="btn" key="message.button.search" name="submit"/>
                </div>
            </div>
        </div>
    </s:form>
</div>
<div class="well">
    <s:form validate="true">
        <table class="table table-striped table-bordered table-hover">
            <tr>
                <th class="span4"><s:property value="getText('message.issue.name')" /></th>
                <th class="span3"><s:property value="getText('message.epubName')" /></th>
                <th class="span2"><s:property value="getText('message.create.date')" /> (UTC)</th>
                <th class="span2"><s:property value="getText('message.issue.status')" /></th>
                <th class="span1"><span><s:property value="getText('message.button.delete')" /></span>
                    &nbsp; <input type="checkbox" class="checkbox" name="select_all" id="select_all"/></th>
            </tr>

            <s:hidden name="publicationId" value="%{publicationId}" />

            <s:iterator value="issueList" var="issue" id="issue">
                <tr>
                    <td><s:property value="%{#issue.name}" /></td>
                    <td><s:property value="%{#issue.driverInfo.designToEpubMapper.epubName}" /></td>
                    <td><s:date name="%{#issue.created}" format="yyyy-MM-dd" /></td>
                    <td><s:checkbox key="message.issue.status.published" name="status_%{#issue.id}"
                            value="%{#issue.status != 1}" /></td>
                    <td><s:checkbox cssClass="delete" name="delete_%{#issue.id}" /></td>
                </tr>
            </s:iterator>
            <tr>
                <td colspan="3"><input class="btn" type="button"
                    name="<s:property value="getText('message.back')" />"
                    value="<s:property value="getText('message.back')" />"
                    onclick="window.location='<s:url action="publicationList"><s:param name="organizationId"><s:property value="%{publication.organization.id}"/></s:param></s:url>'" />
                </td>
                <td colspan="2" style="text-align: center"><s:submit cssClass="btn" key="message.submit"
                        name="submit" method="updateIssues" theme="simple" /></td>
            </tr>
        </table>
    </s:form>
</div>
