<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<title>Organization</title>
</head>
<body>
    <h3>${it.organizationName}</h3>
    <ul id="publications">
        <c:forEach items="${it.publicationMap}" var="publicationMap">
            <c:set var="publicationUri"><c:out value="${publicationMap.value}"/></c:set>
            <li id="${publicationMap.key.id}"><a href="${publicationUri}"><c:out value="${publicationMap.key.name}" /></a></li>
        </c:forEach>
    </ul>
</body>
</html>
