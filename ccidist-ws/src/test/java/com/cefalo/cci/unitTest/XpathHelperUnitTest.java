package com.cefalo.cci.unitTest;

import com.cefalo.cci.utils.XpathHelper;
import com.google.common.io.Closeables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ServicesTestModule.class })
public class XpathHelperUnitTest {

    @Test
    public void getNodeListFromHtmlTest() throws XPathExpressionException {
        String fileStr = readFileAsString("/test.html");
        XpathHelper xpathHelper = new XpathHelper(fileStr);
        String expression = "html/body/table[@id='table']/tr[@id='tr1']/td/ul[@class='organizations']/li";
        NodeList nodeList = xpathHelper.getNodeListFromHtml(expression);
        assertEquals(3, nodeList.getLength());

        String linkExpression = "html/body/table[@id='table']/tr[@id='tr2']/td/ul[@class='organizations']/li";
        NodeList linkNodeList = xpathHelper.getNodeListFromHtml(linkExpression);
        assertEquals(4, linkNodeList.getLength());

        String emptyCheck = "html/body/table[@id='table']/tr[@id='tr3']/td/ul[@class='organizations']/li";
        NodeList emptyList = xpathHelper.getNodeListFromHtml(emptyCheck);
        assertEquals(0, emptyList.getLength());

        String exceptNameSpcStr = readFileAsString("/testWithoutNameSpaceTest.html");
        String exceptNameSpcExp = "html/body/table[@id='table']/tr[@id='tr1']/td/ul[@class='organizations']/li";
        xpathHelper = new XpathHelper(exceptNameSpcStr);
        NodeList exceptNameSpcNodeList = xpathHelper.getNodeListFromHtml(exceptNameSpcExp);
        assertEquals(3, exceptNameSpcNodeList.getLength());

        String atomFileStr = readFileAsString("/atom.xml");
        String atomFileLinkExp = "feed/link";
        xpathHelper = new XpathHelper(atomFileStr);
        NodeList atomLinkNodeList = xpathHelper.getNodeListFromHtml(atomFileLinkExp);
        assertEquals(4, atomLinkNodeList.getLength());

        atomFileLinkExp = "feed/link/@rel";
        atomLinkNodeList = xpathHelper.getNodeListFromHtml(atomFileLinkExp);
        assertEquals(4, atomLinkNodeList.getLength());

        String atomFileEntryExp = "feed/entry";
        NodeList atomEntryNodeList = xpathHelper.getNodeListFromHtml(atomFileEntryExp);
        assertEquals(3, atomEntryNodeList.getLength());

        String publicationXhtml = readFileAsString("/publication.xhtml");
        String deviceTypeExp = "html/body/dl/dd/ul/li";
        xpathHelper = new XpathHelper(publicationXhtml);
        NodeList deviceNodeList= xpathHelper.getNodeListFromHtml(deviceTypeExp);
        assertEquals(3, deviceNodeList.getLength());
        String anchor = "html/body/dl/dd/a";
        NodeList linkTemplateNodeList= xpathHelper.getNodeListFromHtml(anchor);
        assertEquals(1, linkTemplateNodeList.getLength());

        String packageXhtml = readFileAsString("/package.xml");
        String itemXpathExp = "package/manifest/item";
        xpathHelper = new XpathHelper(packageXhtml);
        NodeList itemNodeList = xpathHelper.getNodeListFromHtml(itemXpathExp);
        assertEquals(9, itemNodeList.getLength());

        itemXpathExp = "package/spine[@toc='ncx']/itemref/@idref";
        itemNodeList = xpathHelper.getNodeListFromHtml(itemXpathExp);
        assertEquals(4, itemNodeList.getLength());

        String epubXml = readFileAsString("/regime.xml");
        xpathHelper = new XpathHelper(epubXml);
        String creatorExp = "package/metadata/creator";
        String titleExp = "package/metadata/title";
        String issueExp = "package/metadata/meta[@property='cci:issue']";
        String deviceExp = "package/metadata/meta[@property='cci:device']";
        itemNodeList = xpathHelper.getNodeListFromHtml(creatorExp);
        assertEquals("polaris", itemNodeList.item(0).getTextContent());
        itemNodeList = xpathHelper.getNodeListFromHtml(titleExp);
        assertEquals("addressa", itemNodeList.item(0).getTextContent());
        itemNodeList = xpathHelper.getNodeListFromHtml(issueExp);
        assertEquals("regime", itemNodeList.item(0).getTextContent());
        itemNodeList = xpathHelper.getNodeListFromHtml(deviceExp);
        assertEquals("ipad", itemNodeList.item(0).getTextContent());
    }

    public String readFileAsString(String fileName) {
        int ch;
        StringBuffer strContent = new StringBuffer("");
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(fileName);
            while ((ch = inputStream.read()) != -1) {
                strContent.append((char) ch);
            }
            Closeables.close(inputStream, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strContent.toString();
    }
}
