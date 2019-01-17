<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<title>${it.publication.name} ${it.issue.id} for ${it.issue.driverInfo.designToEpubMapper.epubName}</title>
</head>
<body>
    <dl class="issue">
        <dt>organization</dt>
        <dd><a href="${it.organizationUri}">${it.organization.name}</a></dd>
        <dt>publication</dt>
        <dd><a href="${it.publicationUri}">${it.publication.name}</a></dd>
        <dt>issue</dt>
            <dd>${it.issue.name}</dd>
        <dt>date</dt>
            <dd id="date"><fmt:formatDate pattern="yyyy-MM-dd" value="${it.issue.created}"/></dd>
        <dt>status</dt>
            <dd>${it.statusMap[it.issue.status]}</dd>
        <c:if test = "${not empty it.imageUri}">
            <dt>cover-image</dt>
                <dd><a href="${it.imageUri}">Cover image</a></dd>
        </c:if>
        <dt>epub</dt>
            <dd><a href="${it.binaryUri}">Epub</a></dd>
        <dt>container</dt>
            <dd><a href="${it.containerUri}">Container</a></dd>
        <dt>events</dt>
            <dd><a href="${it.eventsUri}">Events</a></dd>
    </dl>
</body>
</html>
