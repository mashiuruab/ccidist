<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<title>${it.publication.name} ${it.issue.id} for ${it.issue.driverInfo.designToEpubMapper.epubName}</title>
</head>
<body>
    <c:if test="${not empty it.imageMap}">
        <c:forEach items="${it.imageMap}" var="image">
            <ul class="${image.key}">
                <c:forEach items="${image.value}" var="val">
                    <li><a href="${val}">${val}</a></li>
                </c:forEach>
            </ul>
        </c:forEach>
    </c:if>
</body>
</html>
