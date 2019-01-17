package com.cefalo.cci.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.*;

public class XpathHelper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document document = null;

    public XpathHelper() {

    }

    public XpathHelper(String html) {
        try {
            createBuilder();
            byte[] utf8ConvertedString = html.getBytes("UTF-8");
            document = builder.parse(new ByteArrayInputStream(utf8ConvertedString));
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public XpathHelper(InputStream inputStream) {
        try {
            createBuilder();
            document = builder.parse(new InputSource(new InputStreamReader(inputStream, "UTF-8")));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createBuilder() {
        try {
            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {

                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    log.info("Ignoring " + publicId + ", " + systemId);
                    return new InputSource(new StringReader(""));
                }
            });
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public NodeList getNodeListFromHtml(String expression) throws XPathExpressionException {
        NodeList nodeList = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            log.info("Xpath parsing error!!!");
            throw e;
        }
        return nodeList;
    }

    public String parseSingleNodeValue(String pattern) throws XPathExpressionException {
        NodeList nodeList = getNodeListFromHtml(pattern);

        if (nodeList != null && nodeList.item(0) != null) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    public String getSiblingAttributeValue(String pattern, String attribute) throws XPathExpressionException {
        NodeList nodeList = getNodeListFromHtml(pattern);
        if (nodeList != null) {
            Node node = nodeList.item(0);

            if (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(attribute) != null) {
                return node.getAttributes().getNamedItem(attribute).getTextContent();
            }
        }
        return "";
    }

    public String getNextSiblingElement(String value) {
        NodeList dtNodeList = document.getElementsByTagName("dt");
        NodeList ddNodeList = document.getElementsByTagName("dd");

        for (int i = 0; i < dtNodeList.getLength(); i++) {
            if (value.equals(dtNodeList.item(i).getTextContent().trim())) {
                return ddNodeList.item(i).getTextContent().trim();
            }
        }
        return "";
    }
}
