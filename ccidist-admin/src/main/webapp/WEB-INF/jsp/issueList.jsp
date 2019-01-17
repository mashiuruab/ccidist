<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" trimDirectiveWhitespaces="true"
    language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>Issue List</title>
</head>
<body>
    <h1>Issue List</h1>
    <ul class="issues">
        <c:forEach items="${it.issueLinkMap}" var="link">
            <li><a href="${link.value}"><c:out value="issue for ${link.key}" /></a></li>
        </c:forEach>
    </ul>

</body>
</html>
