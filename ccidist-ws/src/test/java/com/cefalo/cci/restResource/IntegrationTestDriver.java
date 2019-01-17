package com.cefalo.cci.restResource;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTestDriver extends JerseyTest {
    static final String PACKAGE_NAME = "com.cefalo.cci.restResource";
    static final String TMPDIR_PROPERTY = "java.io.tmpdir";
    static final String TMPDIR_NAME = "ccidist_tmp";

    public IntegrationTestDriver() {
        super(new WebAppDescriptor.Builder(PACKAGE_NAME).build());
    }

    @Test
    public void runTests() throws Exception {
        OrganizationIntegrationTest organizationTest = new OrganizationIntegrationTest();
        organizationTest.doTest();

        PublicationIntegrationTest publicationTest = new PublicationIntegrationTest(
                organizationTest.getPublicationDetailLink());
        publicationTest.doTest();

        IssueRelatedIntegrationTest issueTest = new IssueRelatedIntegrationTest(publicationTest.getUploadLink(),
                publicationTest.getAccessTokenLink(), publicationTest.getIssueSearchLink());
        issueTest.doTest();
        emptyTempDirectoryTest();
    }

    private void emptyTempDirectoryTest() {
        File tmpDir = new File(System.getProperty(TMPDIR_PROPERTY), TMPDIR_NAME);
        assertNotNull("Tmp Directory existing expected: ", tmpDir);
        for (File file : tmpDir.listFiles()) {
            assertTrue("Emtpy Temporary Directory check", file.isDirectory());
        }
    }
}
