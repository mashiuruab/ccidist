package com.cefalo.cci.listener;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.config.MimeTypeConfiguration;
import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.dao.DriverDaoImpl;
import com.cefalo.cci.dao.EventsDao;
import com.cefalo.cci.dao.EventsDaoImpl;
import com.cefalo.cci.dao.IssueDao;
import com.cefalo.cci.dao.IssueDaoImpl;
import com.cefalo.cci.dao.MatchingRulesDao;
import com.cefalo.cci.dao.MatchingRulesDaoImpl;
import com.cefalo.cci.dao.OrganizationDao;
import com.cefalo.cci.dao.OrganizationDaoImpl;
import com.cefalo.cci.dao.PublicationDao;
import com.cefalo.cci.dao.PublicationDaoImpl;
import com.cefalo.cci.dao.RxmlDao;
import com.cefalo.cci.dao.RxmlDaoImpl;
import com.cefalo.cci.dao.UsersDao;
import com.cefalo.cci.dao.UsersDaoImpl;
import com.cefalo.cci.event.listener.EventListener;
import com.cefalo.cci.event.listener.VarnishListener;
import com.cefalo.cci.event.manager.EventManager;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.service.DriverInfoService;
import com.cefalo.cci.service.DriverInfoServiceImpl;
import com.cefalo.cci.service.ChangelogService;
import com.cefalo.cci.service.ChangelogServiceImpl;
import com.cefalo.cci.service.IssueService;
import com.cefalo.cci.service.IssueServiceImpl;
import com.cefalo.cci.service.MatchingService;
import com.cefalo.cci.service.MatchingServiceImpl;
import com.cefalo.cci.service.OrganizationService;
import com.cefalo.cci.service.OrganizationServiceImpl;
import com.cefalo.cci.service.PublicationService;
import com.cefalo.cci.service.PublicationServiceImpl;
import com.cefalo.cci.service.RxmlService;
import com.cefalo.cci.service.RxmlServiceImpl;
import com.cefalo.cci.service.TokenGenerator;
import com.cefalo.cci.service.TokenGeneratorImpl;
import com.cefalo.cci.service.UsersService;
import com.cefalo.cci.service.UsersServiceImpl;
import com.cefalo.cci.service.cache.DeviceMatchCacheKey;
import com.cefalo.cci.utils.PropertyUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.ServletModule;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class TestApplicationServicesModule extends ServletModule {
    private static final String PROPERTY_FILE_NAME = "/TestApplication.properties";
    private static final String MIME_PROPERTY_FILE_NAME = "/mime.properties";

    private final File cacheDirectory;

    public TestApplicationServicesModule(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    protected void configureServlets() {
        bindDAOs();
        bindServices();
        bindEventListener();
        bindPropertyFiles();
        bindHttpClient();

        // Configure JPA provider. Pretty much everything in the application depends on the DB. So, we initialize this
        // here.
        configureJPA();
    }

    private void configureJPA() {
        install(new JpaPersistModule("ccidistJpaUnit"));
        // This filter must be configured first for the whole application. Otherwise any code using JPA will act
        // surprisingly.
        filter("/*").through(PersistFilter.class);
    }

    private void bindServices() {
        bind(OrganizationService.class).to(OrganizationServiceImpl.class);
        bind(PublicationService.class).to(PublicationServiceImpl.class);
        bind(ChangelogService.class).to(ChangelogServiceImpl.class);
        bind(UsersService.class).to(UsersServiceImpl.class);
        bind(DriverInfoService.class).to(DriverInfoServiceImpl.class);
        bind(RxmlService.class).to(RxmlServiceImpl.class);

        bind(new TypeLiteral<Cache<DeviceMatchCacheKey, List<MatchingRules>>>() {
        }).toInstance(CacheBuilder
                .newBuilder()
                .concurrencyLevel(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .<DeviceMatchCacheKey, List<MatchingRules>> build());
        // The locks for computing device matches.
        bind(new TypeLiteral<ConcurrentMap<DeviceMatchCacheKey, Object>>() {
        }).toInstance(new ConcurrentHashMap<DeviceMatchCacheKey, Object>());
        bind(MatchingService.class).to(MatchingServiceImpl.class);

        bind(new TypeLiteral<ConcurrentMap<Long, Object>>() {
        }).annotatedWith(Names.named("onDemandIssueGenerationLocks")).toInstance(new ConcurrentHashMap<Long, Object>());
        bind(IssueService.class).to(IssueServiceImpl.class);

        bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
    }

    private void bindEventListener() {
        bind(EventListener.class).to(VarnishListener.class).in(Scopes.SINGLETON);
        bind(EventManager.class).in(Scopes.SINGLETON);
    }

    private void bindDAOs() {
        bind(OrganizationDao.class).to(OrganizationDaoImpl.class);
        bind(PublicationDao.class).to(PublicationDaoImpl.class);
        bind(IssueDao.class).to(IssueDaoImpl.class);
        bind(EventsDao.class).to(EventsDaoImpl.class);
        bind(UsersDao.class).to(UsersDaoImpl.class);
        bind(MatchingRulesDao.class).to(MatchingRulesDaoImpl.class);
        bind(DriverDao.class).to(DriverDaoImpl.class);
        bind(RxmlDao.class).to(RxmlDaoImpl.class);
    }

    private void bindPropertyFiles() {
        Properties properties = PropertyUtils.readPropertiesFile(PROPERTY_FILE_NAME);
        properties.put("cacheDirectory", cacheDirectory.getAbsolutePath());
        bind(ApplicationConfiguration.class).toInstance(new ApplicationConfiguration(properties));

        Properties mimeProperties = PropertyUtils.readPropertiesFile(MIME_PROPERTY_FILE_NAME);
        bind(MimeTypeConfiguration.class).toInstance(new MimeTypeConfiguration(mimeProperties));
    }

    private void bindHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.setMaxTotal(20);

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout((int) MINUTES.toMillis(5))  // max wait for connection pool to lease us a client
                .setConnectTimeout((int) SECONDS.toMillis(30))  // max wait for a established TCP connection
                .setSocketTimeout((int) MINUTES.toMillis(5))  // max wait for socket reads
                .build();

        HttpClient client = HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();

        bind(HttpClient.class).toInstance(client);
    }
}
