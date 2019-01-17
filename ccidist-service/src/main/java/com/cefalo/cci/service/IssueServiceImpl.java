package com.cefalo.cci.service;

import static com.cefalo.cci.utils.StringUtils.isBlank;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.dao.IssueDao;
import com.cefalo.cci.enums.IssueStatus;
import com.cefalo.cci.enums.SortBy;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.event.model.Event;
import com.cefalo.cci.event.model.EventType;
import com.cefalo.cci.model.DriverInfo;
import com.cefalo.cci.model.Issue;
import com.cefalo.cci.model.Metadata;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.model.SectionImage;
import com.cefalo.cci.storage.Storage;
import com.cefalo.cci.utils.AtomUtils;
import com.cefalo.cci.utils.CollectionUtils;
import com.cefalo.cci.utils.DateUtils;
import com.cefalo.cci.utils.FileUtils;
import com.cefalo.cci.utils.XpathHelper;
import com.cefalo.cci.utils.locator.ResourceLocator;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

public class IssueServiceImpl implements IssueService {

    private static String CCI_PUBLIC_SIGNATURE = "cci:access-control-public";

    private final Logger logger = LoggerFactory.getLogger(IssueServiceImpl.class);

    @Inject
    private IssueDao issueDao;

    @Inject
    private Storage<Issue> epubStorage;

    @Inject
    private Storage<RxmlZipFile> rxmlStoarge;

    @Inject
    private ChangelogService eventService;

    @Inject
    private DriverInfoService driverInfoService;

    @Inject
    private RxmlService rxmlService;

    @Inject
    private DriverDao driverDao;

    @Inject
    private ApplicationConfiguration config;

    @Inject
    private EventManager eventManager;

    @Inject
    @Named("onDemandIssueGenerationLocks")
    private ConcurrentMap<Long, Object> onDemandIssueGenerationLocks;

    @Inject
    @Named("webservice")
    Provider<ResourceLocator> webserviceLocatorProvider;

    @Inject
    @Named("digitaldriver")
    Provider<ResourceLocator> digitaldriverWebAppLocatorProvider;

    @Inject
    private HttpClient httpClient;

    @Override
    public Issue getIssue(Long issueId) {
        return issueDao.getIssue(issueId);
    }

    @Override
    public List<Issue> getIssuesOfPublication(Publication publication) {
        return issueDao.getIssuesOfPublication(publication);
    }

    @Override
    public List<Issue> getOldIssueList(Date date) {
        return issueDao.getOldIssueList(date);
    }

    @Override
    public SyndFeed getIssuesAsAtomFeed(
            Organization organization,
            Publication publication,
            long start, long limit,
            String epubName,
            List<Long> epubNameIds,
            String toDateStr, Date toDate,
            String sortOrder, String sortBy,
            boolean draft) {
        checkArgument(start > 0 && limit > 0);

        List<IssueStatus> statusList = Lists.newArrayList(IssueStatus.PUBLISHED);
        if (draft) {
            statusList.add(IssueStatus.DRAFT);
        }

        // Remember that the DB layer expects 0 based indexing while we use 1 based indexing in the resource layer.
        return getIssueAsAtomFeed(
                issueDao.getIssueListByPublicationAndDeviceId(
                        publication.getId(),
                        start - 1, limit,
                        epubNameIds,
                        statusList,
                        sortOrder, sortBy,
                        toDate),
                organization,
                publication,
                start, limit,
                epubName, epubNameIds,
                toDateStr,
                sortOrder, sortBy,
                Boolean.toString(draft),
                (int) issueDao.getIssueCountByPublicationAndDeviceId(
                        publication.getId(),
                        epubNameIds,
                        statusList,
                        toDate));
    }

    @SuppressWarnings("unchecked")
    SyndFeed getIssueAsAtomFeed(
            List<Issue> issues,
            Organization organization,
            Publication publication,
            long start, long limit,
            String epubName, List<Long> epubNameIds,
            String toDateStr,
            String sortOrder, String sortBy,
            String draft,
            long total) {
        ResourceLocator wsLocator = webserviceLocatorProvider.get();

        String publicationName = publication.getName();
        String organizationName = organization.getName();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sortOrder", sortOrder);
        queryParams.put("draft", draft);
        queryParams.put("sortBy", sortBy);

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");
        feed.setTitle(String.format("%s issues", publicationName));
        feed.setUri(wsLocator.getIssueListURI(organizationName, publicationName).toString());
        feed.setPublishedDate(new Date());

        SyndPerson syndPerson = new SyndPersonImpl();
        syndPerson.setName(publicationName);
        feed.getAuthors().add(syndPerson);

        List<SyndLink> links;
        if (!isBlank(toDateStr)) {
            queryParams.put("toDate", toDateStr);
        }
        queryParams.put("epubName", epubName);
        links = AtomUtils.getLinks(
                start, limit, total,
                wsLocator.getIssueListURI(organization.getId(), publication.getId()).toString(),
                queryParams);
        feed.setLinks(links);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (Issue issue : issues) {
            SyndEntry syndEntry = new SyndEntryImpl();
            syndEntry.setUri("urn:uuid:".concat(String.valueOf(issue.getId())));
            syndEntry.setUpdatedDate(issue.getUpdated());
            syndEntry.setPublishedDate(issue.getCreated());
            syndEntry.setTitle(issue.getName());
            syndEntry.setAuthor(publicationName);
            Map<String, String> relUriMap = new HashMap<String, String>();
            relUriMap.put(
                    "alternate",
                    wsLocator.getIssueURI(organization.getId(), publication.getId(), issue.getId()).toString());
            relUriMap.put(
                    "public",
                    wsLocator.getPublicIssueDetailURI(
                            organization.getId(),
                            publication.getId(),
                            issue.getId()).toString());
            syndEntry.setLinks(AtomUtils.generateSyndLinks(relUriMap));
            entries.add(syndEntry);
        }
        feed.setEntries(entries);

        return feed;
    }

    private void updateEpub(Issue issue, File blobFile) throws IOException {
        try (InputStream blobStream = new FileInputStream(blobFile)) {
            epubStorage.update(issue, blobStream);

            /* Force update issue when epub is updated */
            issue.setStale(false);
            updateIssue(issue);
        }
    }

    private void populateEventsQueue(Issue issue, File newEpubFile, File oldEpubFile) throws Exception {
        final Set<Path> visitedFiles = new HashSet<>();
        final Set<String> updatedSet = new HashSet<>();
        final Set<String> insertedSet = new HashSet<>();
        final Set<String> deletedSet = new HashSet<>();

        ImmutableMap<String, String> env = ImmutableMap.of("create", "false");
        try (FileSystem uploadedFS = FileSystems.newFileSystem(toJarUri(newEpubFile), env);
                FileSystem existingFS = FileSystems.newFileSystem(toJarUri(oldEpubFile), env);) {
            findDifference(existingFS, uploadedFS, visitedFiles, updatedSet, insertedSet);
            findDifference(uploadedFS, existingFS, visitedFiles, updatedSet, deletedSet);

            eventService.addChanges(issue.getId(), updatedSet, insertedSet, deletedSet);
        } catch (Exception ex) {
            logger.error("Error when trying to find difference of EPUBs.", ex);
            throw ex;
        }
    }

    private URI toJarUri(File file) {
        // TODO: Test this on "Windoze"
        return URI.create("jar:".concat(file.toURI().toString()));
    }

    private void findDifference(
            final FileSystem comparedFromFS,
            final FileSystem comparedToFS,
            final Set<Path> visitedFiles,
            final Set<String> updatedSet,
            final Set<String> newSet) throws IOException {
        Files.walkFileTree(comparedToFS.getPath("/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path comparedToPath, BasicFileAttributes attrs) throws IOException {
                Path comparedFromPath = comparedFromFS.getPath(comparedToPath.toString());

                if (visitedFiles.contains(comparedToPath)) {
                    return FileVisitResult.CONTINUE;
                } else if (Files.exists(comparedFromPath)) {
                    visitedFiles.add(comparedToPath);

                    ByteSource fromSource = FileUtils.toByteSource(comparedFromPath);
                    ByteSource toSource = FileUtils.toByteSource(comparedToPath);

                    if (!fromSource.contentEquals(toSource)) {
                        updatedSet.add(comparedToPath.toString());
                    }
                } else {
                    newSet.add(comparedToPath.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    @Transactional
    public Issue createIssue(RxmlZipFile rxmlFile, DriverInfo driverInfo) throws Exception {
        Issue issue = issueDao.createIssue(
                rxmlFile.getPublication(),
                rxmlFile.getIssueName(),
                driverInfo,
                rxmlFile,
                IssueStatus.DRAFT.getValue(),
                rxmlFile.getIssueDate());

        eventManager.post(new Event(EventType.CREATE, Issue.class, issue.getId(), null));
        return issue;
    }

    @Override
    @Transactional
    public void createIssueAndEpub(
            RxmlZipFile rxmlFile,
            DriverInfo driverInfo,
            File epubFile) throws Exception {
        Issue issue = null;
        try (InputStream inputStream = new FileInputStream(epubFile)) {
            // First create the entry in the issue table.
            issue = createIssue(rxmlFile, driverInfo);

            // Now create the binary file for the issue.
            epubStorage.create(issue, inputStream);

            // Now update the issue to mark it as "NOT stale anymore"
            issue.setStale(false);
            updateIssue(issue);
        }
    }

    private Metadata parseMetaData(final Issue issue) throws Exception {
        Closer closer = Closer.create();
        try (InputStream containerXmlStream = epubStorage.getFragment(issue, URI.create(CONTAINER_RELATIVE_PATH))) {
            XpathHelper xpathHelper = new XpathHelper(containerXmlStream); // This will close the inputStream
            NodeList nodeList = xpathHelper.getNodeListFromHtml("container/rootfiles/rootfile/@full-path");
            if (nodeList == null) {
                throw new IllegalArgumentException(String.format("Parsing error is %s",
                        "container/rootfiles/rootfile/@full-path"));
            }
            Node node = nodeList.item(0);

            String relativePath = node.getTextContent();
            relativePath = relativePath.substring(0, relativePath.lastIndexOf("/") + 1);

            URI contentUri = new URI(null, null, null, -1, node.getTextContent(), null, null);
            InputStream contentInputStream = closer.register(epubStorage.getFragment(issue, contentUri));

            Metadata metadata = createMetadataObject(contentInputStream);
            metadata.setCoverImageLink(relativePath + metadata.getCoverImageLink());
            if (metadata.getSectionCoverImages() != null) {
                for (SectionImage sectionImage : metadata.getSectionCoverImages()) {
                    sectionImage.setValue(relativePath + sectionImage.getValue());
                }
            }

            return metadata;
        } finally {
            closer.close();
        }
    }

    private Metadata createMetadataObject(InputStream inputStream)
            throws IllegalArgumentException, XPathExpressionException {
        XpathHelper xpathHelper = new XpathHelper(inputStream);
        Metadata metaData = new Metadata();
        metaData.setCoverImageLink(xpathHelper.getSiblingAttributeValue(
                "package/manifest/item[contains(@properties, 'cover-image')]", "href"));
        metaData.setSectionCoverImages(parseAndGetSectionImages(
                "package/manifest/item[contains(@properties, 'cci:access-control-public')]", "href", xpathHelper));
        return metaData;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createRxmlFileWithIssues(RxmlZipFile rxmlFile, File uploadedRxmlFile) throws Exception {
        try (InputStream zipFileInputStream = new FileInputStream(uploadedRxmlFile)) {
            // First create the RxmlZipFile
            rxmlService.createRxmlFile(rxmlFile);

            // Now create the binary
            rxmlStoarge.create(rxmlFile, zipFileInputStream);
        }

        Publication publication = rxmlFile.getPublication();
        publication.getOrganization();
        List<DriverInfo> preGenDrivers = driverInfoService.getDrivers(
                publication.getId(),
                rxmlFile.getDesignName(),
                rxmlFile.getCreated(),
                true);

        for (DriverInfo driverInfo : preGenDrivers) {
            File generatedEpubFile = createEpubWithCciDriver(rxmlFile, uploadedRxmlFile, driverInfo);
            try {
                createIssueAndEpub(rxmlFile, driverInfo, generatedEpubFile);
            } finally {
                FileUtils.deleteRecursive(generatedEpubFile);
            }
        }

        List<DriverInfo> nonPreGenDrivers = driverInfoService.getDrivers(
                publication.getId(),
                rxmlFile.getDesignName(),
                rxmlFile.getCreated(),
                false);
        for (DriverInfo driverInfo : nonPreGenDrivers) {
            createIssue(rxmlFile, driverInfo);
        }
    }

    private void updateEpubs(
            List<DriverInfo> driverInfoList,
            RxmlZipFile rxmlZipFile,
            File rxmlFileOnDisk) throws Exception {
        String tmpDirFullPath = config.getTmpDir();
        File oldFileDir = new File(tmpDirFullPath, "old");

        for (DriverInfo driverInfo : driverInfoList) {
            Issue issue = getIssueByZipAndDriver(rxmlZipFile, driverInfo);
            boolean forceEpubGeneration = false;
            if (issue == null) {
                // We were unable to find an issue. This can actually mean only one thing. Someone deleted it from the
                // /admin GUI. So, we'll just recreate it.
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "Seems like the issue for Rxml: {} and Driver: {} has been deleted by someone. We'll re-create it.",
                            rxmlZipFile.getId(),
                            driverInfo.getId());
                }

                issue = createIssue(rxmlZipFile, driverInfo);
                forceEpubGeneration = driverInfo.isPreGenerate();
            }

            if (!forceEpubGeneration && !epubStorage.exists(issue)) {
                // This issue EPUB has never been generated. So, we don't need to generate it now. We can just wait for
                // on-demand.
                continue;
            }

            File newGeneratedEpubFile = createEpubWithCciDriver(rxmlZipFile, rxmlFileOnDisk, driverInfo);

            File oldEpubFile = new File(oldFileDir, String.format("%s_%s", issue.getId(), UUID.randomUUID()));
            try (InputStream oldInputStream = epubStorage.get(issue)) {
                // In the previous "forceGeneration" case, the storage does not really have the file yet. So, there will
                // be no events. Thats why this null check is quite important.
                if (oldInputStream != null) {
                    FileUtils.writeToFile(oldInputStream, oldEpubFile);
                    populateEventsQueue(issue, newGeneratedEpubFile, oldEpubFile);
                }

                updateEpub(issue, newGeneratedEpubFile);
            } finally {
                FileUtils.deleteRecursive(oldEpubFile);
                FileUtils.deleteRecursive(newGeneratedEpubFile);
            }
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateRXMlFileWithIssues(RxmlZipFile rxmlFile, File rxmlFileOnDisk) throws Exception {
        try (InputStream inputStream = new FileInputStream(rxmlFileOnDisk);) {
            rxmlStoarge.update(rxmlFile, inputStream);

            rxmlFile.setUpdated(DateUtils.convertDateWithTZ(new Date()));
            rxmlService.updateRxmlZipFile(rxmlFile);
        } catch (Exception e) {
            throw e;
        }

        List<DriverInfo> driverInfos = driverInfoService.getDrivers(
                rxmlFile.getPublication().getId(),
                rxmlFile.getDesignName(),
                rxmlFile.getCreated());
        updateEpubs(driverInfos, rxmlFile, rxmlFileOnDisk);
    }

    @Override
    public List<Issue> getIssueListByZipFileId(long zipFileId) {
        return issueDao.getIssueListByZipFileId(zipFileId);
    }

    @Override
    public List<Issue> getAdminIssueListBySearchQuery(
            String publicationId,
            int start, int limit,
            String deviceType,
            List<IssueStatus> statusList,
            Date startDate) {
        List<Long> deviceTypeIds;
        if (!isBlank(deviceType)) {
            deviceTypeIds = driverDao.getDesignToEpubMapperIds(publicationId, deviceType);
        } else {
            deviceTypeIds = new ArrayList<>();
        }

        return issueDao.getIssueListByPublicationAndDeviceId(
                publicationId,
                start - 1, limit,
                deviceTypeIds,
                statusList,
                "asc", SortBy.CREATED.getValue(),
                startDate);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteIssue(Issue issue) {
        String publicationId = issue.getPublication().getId();
        long issueId = issue.getId();

        ResourceLocator resourceLocator = webserviceLocatorProvider.get();
        resourceLocator.getIssueURI(issue.getPublication().getOrganization().getId(), publicationId, issueId);

        try {
            RxmlZipFile rxmlFile = issue.getRxmlZipFile();

            issueDao.deleteIssue(issue);
            epubStorage.delete(issue);

            // Post the issue delete event.
            eventManager.post(new Event(EventType.DELETE, Issue.class, issueId, publicationId));

            // If no other issue exists for the related RXML file, then delete the RXML file as well.
            long rxmlId = rxmlFile.getId();
            List<Issue> allIssuesForRxml = getIssueListByZipFileId(rxmlId);
            if (CollectionUtils.isEmpty(allIssuesForRxml)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Deleting RXML: {} since it no longer has any issue related to it.", rxmlId);
                }
                rxmlService.delete(rxmlFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateStatusAndDeleteIssue(Map<Long, IssueStatus> issueIdStatusMap, List<Long> issueIdToBeDeleted) {
        // First update the status
        for (Map.Entry<Long, IssueStatus> entry : issueIdStatusMap.entrySet()) {
            Issue issue = issueDao.getIssue(entry.getKey());

            if (issue.getStatus() != entry.getValue().getValue()) {
                issue.setStatus(entry.getValue().getValue());

                updateIssue(issue);
            }
        }

        // Now delete issues.
        for (Long issueId : issueIdToBeDeleted) {
            deleteIssue(issueDao.getIssue(issueId));
        }
    }

    private File createEpubWithCciDriver(
            RxmlZipFile rxmlFile,
            File rxmlFileOnDisk,
            DriverInfo driverInfo) throws Exception {
        Stopwatch timer = new Stopwatch().start();

        String generatedEpubName = String.format(
                "%s-%s-%s-%s.epub",
                driverInfo.getPublication().getId(),
                rxmlFile.getFileName(),
                driverInfo.getDeviceName(),
                UUID.randomUUID().toString());
        File generatedEpubFile = new File(config.getTmpDir(), generatedEpubName);

        ResourceLocator digitaldriver = digitaldriverWebAppLocatorProvider.get();

        digitaldriver.addQueryParameter("-i", rxmlFileOnDisk.getAbsolutePath());
        digitaldriver.addQueryParameter("-o", generatedEpubFile.getAbsolutePath());

        digitaldriver.addQueryParameter("-organization", driverInfo.getPublication().getOrganization().getId());

        if (!isBlank(driverInfo.getDeviceName())) {
            digitaldriver.addQueryParameter("-device", driverInfo.getDeviceName());
        }

        if (!isBlank(driverInfo.getOs())) {
            digitaldriver.addQueryParameter("-os", driverInfo.getOs());
        }

        if (!isBlank(driverInfo.getOsVersion())) {
            digitaldriver.addQueryParameter("-osv", driverInfo.getOsVersion());
        }

        if (!isBlank(driverInfo.getReader())) {
            digitaldriver.addQueryParameter("-reader", driverInfo.getReader());
        }

        HttpResponse response = null;
        try {
            HttpGet request = new HttpGet(digitaldriver.getURI());
            response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        String.format(
                                "Error while trying to ccidist-digitaldriver. URI: %s, Method: %s. Status: %s",
                                request.getURI().toString(),
                                request.getMethod(),
                                response.getStatusLine().getStatusCode()));
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("ccidist-digitaldriver response for {} request to {} = {}",
                            request.getMethod(),
                            request.getURI().toString(),
                            EntityUtils.toString(response.getEntity()));
                }
            }

            // Make sure that we have a non-zero sized EPUB file.
            if (!generatedEpubFile.exists() || generatedEpubFile.length() == 0) {
                throw new IllegalArgumentException(String.format(
                        "Driver Software epub generation error. EPUB file is empty. EPUB: %s. RXML: %s. Driver: %s",
                        generatedEpubFile.getAbsolutePath(),
                        rxmlFile.getId(),
                        driverInfo.getId()));
            }

            return generatedEpubFile;
        } catch (Exception ex) {
            logger.error("Error while creating EPUB with Digital Driver.", ex);
            throw ex;
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }

            timer.stop();
            if (logger.isTraceEnabled()) {
                logger.trace("Time required to generate EPUB file from {}({}) : {}",
                        rxmlFileOnDisk.getName(),
                        FileUtils.getHumanReadableFileSize(rxmlFileOnDisk),
                        timer);
            }
        }
    }

    @Override
    public List<SectionImage> getSectionImageLinks(Issue issue) throws Exception {
        Metadata metaData = parseMetaData(issue);
        return metaData.getSectionCoverImages();
    }

    @Override
    public String getCoverImageLink(Issue issue) throws Exception {
        Metadata metaData = parseMetaData(issue);
        return metaData.getCoverImageLink();
    }

    private List<SectionImage> parseAndGetSectionImages(String pattern, String attribute, XpathHelper xpathHelper) {
        NodeList nodeList = null;
        try {
            nodeList = xpathHelper.getNodeListFromHtml(pattern);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        List<SectionImage> sectionImageList = new ArrayList<>();
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                String nodeValue = "";
                Node node = nodeList.item(i);
                if (node.hasAttributes()) {
                    if (node.getAttributes().getNamedItem(attribute) != null) {
                        nodeValue = node.getAttributes().getNamedItem(attribute).getTextContent();
                    }
                    SectionImage sectionImage = new SectionImage();
                    sectionImage.setKey(getItemKeyOfMetaData(node.getAttributes().getNamedItem("properties")
                            .getNodeValue().split(" ")));
                    sectionImage.setValue(nodeValue);
                    sectionImageList.add(sectionImage);
                }
            }
            return sectionImageList;
        }
        return null;
    }

    private String getItemKeyOfMetaData(String[] splitArray) {
        if (splitArray.length > 1) {
            for (String value : splitArray) {
                if (CCI_PUBLIC_SIGNATURE.equals(value)) {

                } else if (value.contains("cci:group-")) {
                    return value.replace("cci:group-", "");
                } else if ("cover-image".equals(value)) {
                    return value;
                } else {
                    return "public";
                }
            }
        }
        return "public";
    }

    @Override
    public void updateIssue(Issue issue) {
        issueDao.updateIssue(issue);

        ResourceLocator resourceLocator = webserviceLocatorProvider.get();
        resourceLocator.getIssueURI(
                issue.getPublication().getOrganization().getId(),
                issue.getPublication().getId(),
                issue.getId());

        eventManager.post(new Event(EventType.UPDATE, Issue.class, issue.getId(), null));
    }

    @Override
    @Transactional
    public void generateEpubIfNecessary(Issue issue) throws IOException {
        if (isEpubOutdated(issue)) {
            // We need to make sure that only one request does the "on-demand" generation.
            Long issueId = issue.getId();

            // This will be used to let others know that I am "Generating"...
            Object lockObject = new Object();

            // Checkout the javadoc for ConcurrentMap#putIfAbsent. If this returns null, it means that we are the first
            // one trying to generate the EPUB. So, we synchronize on this object. If this is not null, it means that
            // someone else is already generating this EPUB. So, we synchronize & sleep on it.
            Object currentLock = onDemandIssueGenerationLocks.putIfAbsent(issueId, lockObject);

            // We need to do a try/catch here to remove the lock in the finally clause.
            try {
                synchronized (currentLock != null ? currentLock : lockObject) {
                    if (isEpubOutdated(issue)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Going to generate On-Demand EPUB for Issue: {}", issue.getId());
                        }

                        String tmpDirPath = config.getTmpDir();
                        String randomRxmlFileName = String.format(
                                "%s_%s",
                                issue.getRxmlZipFile().getFileName(),
                                UUID.randomUUID().toString());
                        File rxmlFile = new File(tmpDirPath, randomRxmlFileName);

                        try (InputStream rxmlInputStream = rxmlStoarge.get(issue.getRxmlZipFile())) {
                            FileUtils.writeToFile(rxmlInputStream, rxmlFile);
                        }

                        File epubFile = createEpubWithCciDriver(issue.getRxmlZipFile(), rxmlFile, issue.getDriverInfo());
                        try (InputStream inputStream = new FileInputStream(epubFile)) {
                            if (epubStorage.exists(issue)) {
                                epubStorage.update(issue, inputStream);
                            } else {
                                epubStorage.create(issue, inputStream);
                            }

                            if (issue.isStale()) {
                                // If the issue is stale, then mark it as fresh :-)
                                issue.setStale(false);
                                updateIssue(issue);
                            }
                        } finally {
                            FileUtils.deleteRecursive(epubFile);
                        }

                        FileUtils.deleteRecursive(rxmlFile);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Saved a duplicate on demand issue generation for: {}", issue.getId());
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Error when trying to generate EPUB for issue: {}", issue.getId(), ex);
                throw new RuntimeException(ex);
            } finally {
                // Only remove if I put the lock there.
                onDemandIssueGenerationLocks.remove(issueId, lockObject);
            }
        }
    }

    @Override
    public boolean isEpubOutdated(Issue issue) throws IOException {
        return issue.isStale() || !epubStorage.exists(issue);
    }

    @Override
    public List<Issue> getIssueByDriverInfo(DriverInfo driverInfo) {
        return issueDao.getIssueByDirverInfo(driverInfo);
    }

    private Issue getIssueByZipAndDriver(RxmlZipFile rxmlFile, DriverInfo driverInfo) {
        return issueDao.getIssueByZipAndDriverId(rxmlFile.getId(), driverInfo.getId());
    }
}
