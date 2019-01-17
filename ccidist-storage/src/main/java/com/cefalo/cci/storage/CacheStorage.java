package com.cefalo.cci.storage;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Identifier;
import com.cefalo.cci.utils.FileUtils;
import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CacheStorage<T extends Identifier> implements Storage<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Storage<T> storage;
    private final ConcurrentMap<Long, Object> extractionLock;

    private final File cacheDirectory;
    private final File extractedCacheDirectory;

    @Inject
    public CacheStorage(
            @Named("internal") Storage<T> storage,
            @Named("zipDirName") String zipDirName,
            @Named("extractionLock") ConcurrentMap<Long, Object> extractionLock,
            ApplicationConfiguration config) {
        this.storage = storage;
        this.extractionLock = extractionLock;

        this.cacheDirectory = new File(config.getCacheDirectoryPath(), zipDirName);
        this.extractedCacheDirectory = new File(this.cacheDirectory, "ExTracted");
    }

    @Override
    public InputStream get(final T resource) throws IOException {
        checkNotNull(resource, "Resource may not be null");

        File resourceFile = cachedResourceFile(resource);
        if (resourceFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found cached {} for {} at: {}",
                        resource.getClass().getSimpleName(),
                        resource.getId(),
                        resourceFile.getAbsolutePath());
            }
            return new FileInputStream(resourceFile);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No cached {} for {}. Retrieving from DB.",
                        resource.getClass().getSimpleName(),
                        resource.getId());
            }

            try (InputStream data = storage.get(resource)) {
                if (data == null) {
                    return null;
                }

                FileUtils.writeToFile(data, resourceFile);
                return new FileInputStream(resourceFile);
            }
        }
    }

    @Override
    public boolean exists(T resource) throws IOException {
        File cachedResourceFile = cachedResourceFile(resource);
        if (cachedResourceFile.exists()) {
            // If the cached file does exist, we can return true. HOWEVER, we can't decide anything when the file is not
            // present.
            return true;
        }

        // The cache directory may not have the resource at the moment. So, we should always check with the internal
        // storage.
        return storage.exists(resource);
    }

    @Override
    public InputStream getFragment(final T resource, URI fragmentPath) throws IOException {
        checkNotNull(resource, "Resource may not be null");
        checkNotNull(fragmentPath, "Fragment Path may not be null");

        // This check is crucial. Otherwise we may actually read arbitrary files from the filesystem.
        checkState(!fragmentPath.isAbsolute(), "Fragment path must not be an absolute path.");

        File extractedCacheDir = extractedCachedResourceDir(resource);

        if (!extractedCacheDir.exists()) {
            Long resourceID = resource.getId();

            // This will be used to let others know that I am searching on something :)
            Object lockObject = new Object();

            // Checkout the javadoc for ConcurrentMap#putIfAbsent. If this returns null, it means that we are the first
            // one trying to do this search. So, we synchronize on this object. If this is not null, it means that
            // someone else already is searching. So, we synchronize & sleep on it.
            Object currentLock = extractionLock.putIfAbsent(resourceID, lockObject);

            // We need to do a try/catch here to remove the extractionLock in the finally clause.
            try {
                synchronized (currentLock != null ? currentLock : lockObject) {
                    if (!extractedCacheDir.exists()) {
                        InputStream data = get(resource);
                        if (data == null) {
                            throw new FileNotFoundException(String.format("No binary file for: %s", resource.getId()));
                        }
                        extractTo(data, extractedCacheDir.getAbsolutePath());
                    }
                }
            } finally {
                // Only remove if I put the extractionLock there.
                extractionLock.remove(resourceID, lockObject);
            }
        }

        File fragmentFile = new File(extractedCacheDir, fragmentPath.getPath());

        if (fragmentFile.exists()) {
            return new FileInputStream(fragmentFile);
        } else {
            throw new FileNotFoundException(
                    String.format("Unable to find file. %s: %s, fragment: %s",
                            resource.getClass().getSimpleName(),
                            resource.getId(),
                            fragmentPath));
        }
    }

    @Override
    public void create(T resource, InputStream inputStream) throws IOException {
        checkNotNull(resource, "Resource may not be null");
        checkNotNull(inputStream, "Binary stream may not be null");

        File cachedResourceFile = cachedResourceFile(resource);
        boolean exceptionHappened = false;
        try {
            FileUtils.writeToFile(inputStream, cachedResourceFile);
            inputStream = new FileInputStream(cachedResourceFile);
            storage.create(resource, inputStream);
        } catch (Exception e) {
            exceptionHappened = true;
            throw new RuntimeException(e);
        } finally {
            if (exceptionHappened) {
                Closeables.close(inputStream, true);
                FileUtils.deleteRecursive(cachedResourceFile);
            }
        }
    }

    @Override
    public void update(T modifiedData, InputStream inputStream) throws IOException {
        checkNotNull(modifiedData, "update object can not be null");

        File cachedResourceFile = cachedResourceFile(modifiedData);
        try {
            cleanUp(modifiedData);

            FileUtils.writeToFile(inputStream, cachedResourceFile); // This closes the inputStream
            inputStream = new FileInputStream(cachedResourceFile);

            storage.update(modifiedData, inputStream);
        } catch (IOException io) {
            Closeables.close(inputStream, true);
            FileUtils.deleteRecursive(cachedResourceFile);

            throw io;
        }
    }

    @Override
    public void delete(T resource) throws IOException {
        checkNotNull(resource, "Resource may not be null");
        Stopwatch timer = new Stopwatch().start();

        storage.delete(resource);
        cleanUp(resource);

        if (logger.isDebugEnabled()) {
            logger.debug("Time to clear cache of {} {}: {}",
                    resource.getClass().getSimpleName(),
                    resource.getId(),
                    timer.stop());
        }
    }

    private void extractTo(InputStream inputStream, String pathToExtract) throws IOException {
        checkNotNull(pathToExtract, "pathToExtract may not be null");

        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            String name;
            File file = new File(pathToExtract);
            if (!file.exists()) {
                Files.createParentDirs(file);
            }
            while ((entry = zipInputStream.getNextEntry()) != null) {
                name = entry.getName();
                file = new File(pathToExtract + "/" + name);
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                fileOutputStream = new FileOutputStream(file);
                ByteStreams.copy(zipInputStream, fileOutputStream);
            }
        } finally {
            Closeables.close(inputStream, false);
            Closeables.close(zipInputStream, false);
            Closeables.close(fileOutputStream, false);
        }
    }

    @Override
    public void cleanUp(T resource) {
        checkNotNull(resource, "Resource may not be null");

        FileUtils.deleteRecursive(cachedResourceFile(resource));
        FileUtils.deleteRecursive(extractedCachedResourceDir(resource));
    }

    private File cachedResourceFile(T resource) {
        return new File(this.cacheDirectory, String.valueOf(resource.getId()));
    }

    private File extractedCachedResourceDir(T resource) {
        return new File(this.extractedCacheDirectory, String.valueOf(resource.getId()));
    }
}
