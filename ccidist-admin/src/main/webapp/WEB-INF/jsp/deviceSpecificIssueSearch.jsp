<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:cci="http://www.ccieurope.com/ccidist" xml:lang="en">
<head>
<title>Device Specific Issue Search</title>
</head>
<body>
    <div>
        <h2>Issue Search</h2>
        <form id="issueSearch" action="<c:out value="${it.issueSearchURI}"/>" method="get"
                enctype="application/x-www-form-urlencoded">
            <dl>
                <dt>
                    <label for="start">Start</label>
                </dt>
                <dd>
                    <input type="text" name="start" id="start" />
                </dd>
            </dl>
            <dl>
                <dt>
                    <label for="limit">Limit</label>
                </dt>
                <dd>
                    <input type="text" name="limit" id="limit" />
                </dd>
            </dl>
            <dl>
                <dt>
                    <label>Epub name</label>
                </dt>
                <dd>
                    <%-- We render the pinned down search form. --%>
                    <input type="hidden" name="epubName" id="epubName" value="${it.epubName}" /> <span>${it.epubName}</span>
                </dd>
            </dl>
            <dl>
                <dt>
                    <label for="toDate">To date</label>
                </dt>
                <dd>
                    <input type="text" name="toDate" id="toDate" />yyyy-MM-dd(Ex.2009-01-01) or
                    yyyy-MM-ddZ(Ex.2009-01-01+0600)
                </dd>
            </dl>
            <dl>
                <dt>
                    <label for="sortOrder">Sort order</label>
                </dt>
                <dd>
                    <select id="sortOrder" name="sortOrder">
                        <option value="desc">Descending</option>
                        <option value="asc">Ascending</option>
                    </select>
                </dd>
            </dl>
            <dl>
                <dt>
                    <label for="draft">Draft</label>
                </dt>
                <dd>
                    <input type="checkbox" id="draft" name="draft" value="true" />
                </dd>
            </dl>

            <dl>
                <dd>
                    <input type="submit" name="submit" />
                </dd>
            </dl>
        </form>
    </div>
</body>
</html>
