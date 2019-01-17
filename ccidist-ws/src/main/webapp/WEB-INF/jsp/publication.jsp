<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:cci="http://www.ccieurope.com/ccidist" xml:lang="en">
<head>
<title>Publication</title>
</head>
<body>
    <h1>Publication</h1>
    <h3>
        <c:out escapeXml="true" value="${it.publication.name}" />
    </h3>

    <%-- The issue search form --%>
    <jsp:include page="issueSearch.jsp" />

    <div>
        <h2>Generate access token</h2>
        <form id="accesstoken" action="${it.tokenURI}" method="post" enctype="application/x-www-form-urlencoded">
            <dl>
                <dt>issueLink</dt>
                <dd>
                    <input type="text" name="issueLink" id="issueLink" />
                </dd>
            </dl>
            <dl>
                <dd>
                    <input type="submit" name="submit" />
                </dd>
            </dl>
        </form>
    </div>

    <jsp:include page="deviceMatch.jsp" />

    <div id="uploadcciobjectxml">
        <dl>
            <dt>uploadcciobjectxml</dt>
            <dd>
                <a href="<c:out value="${it.cciObjectXMLUploadURI}"/>">uploadcciobjectxml</a>
            </dd>
        </dl>
    </div>
</body>
</html>
