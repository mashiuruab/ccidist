<%@ page session="false" contentType="application/xhtml+xml;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div>
    <h2>Device matching</h2>
    <form action="${it.matchedContentURI}" id="deviceMatch" method="get" enctype="application/x-www-form-urlencoded">
        <dl>
            <dt>
                <label for="device">Device Name</label>
            </dt>
            <dd>
                <input type="text" name="device" id="device" />
            </dd>
        </dl>
        <dl>
            <dt>
                <label for="width">Device Width</label>
            </dt>
            <dd>
                <input type="text" name="width" id="width" />
            </dd>
        </dl>
        <dl>
            <dt>
                <label for="height">Device Height</label>
            </dt>
            <dd>
                <input type="text" name="height" id="height" />
            </dd>
        </dl>
        <dl>
            <dt>
                <label for="os">OS</label>
            </dt>
            <dd>
                <input type="text" name="os" id="os" />
            </dd>
        </dl>
        <dl>
            <dt>
                <label for="osv">OS Version</label>
            </dt>
            <dd>
                <input type="text" name="osv" id="osv" />
            </dd>
        </dl>
        <dl>
            <dt>
                <label for="readerVersion">Reader Version</label>
            </dt>
            <dd>
                <input type="text" name="readerVersion" id="readerVersion" />
            </dd>
        </dl>
        <dl>
            <dd>
                <input type="submit" name="submit" />
            </dd>
        </dl>
    </form>
</div>
