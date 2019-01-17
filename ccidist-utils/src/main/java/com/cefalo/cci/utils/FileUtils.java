package com.cefalo.cci.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

public abstract class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class.getName());

    private FileUtils() {

    }

    public static void writeToFile(final InputStream inputStream, final File targetFile) throws IOException {
        FileOutputStream tmpFileOutputStream = null;
        try {
            com.google.common.io.Files.createParentDirs(targetFile);
            tmpFileOutputStream = new FileOutputStream(targetFile);

            ByteStreams.copy(inputStream, tmpFileOutputStream);
            if (logger.isInfoEnabled()) {
                logger.info("File written. Path: {}", targetFile.getAbsolutePath());
            }
        } finally {
            Closeables.close(tmpFileOutputStream, true);
            Closeables.close(inputStream, true);
        }
    }

    public static void deleteRecursive(File file) {
        if (file == null || !file.exists()) {
            // If the file does not exist, we are done :-)
            return;
        }

        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static InputStream getExtractedContent(File zipFile, String entryName) throws IOException {
        ZipInputStream zipInputStream = null;
        boolean exception = false;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                if (entryName.equals(ze.getName())) {
                    return zipInputStream;
                }
            }

            // FIXME: This return is never handled????
            return null;
        } catch (IOException io) {
            exception = true;
            throw io;
        } finally {
            if (exception) {
                Closeables.close(zipInputStream, true);
            }
        }
    }

    public static ByteSource toByteSource(final Path contentPath) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return Files.newInputStream(contentPath);
            }
        };
    }

    /**
     * Shamelessly copied over from stack overflow. For the life in me, I could
     * not remember the log formula.
     * 
     * @param file
     * @return
     */
    public static String getHumanReadableFileSize(final File file) {
        int unit = 1024;
        long fileLength = file.length();

        if (fileLength < unit) {
            return fileLength + " B";
        }

        int exp = (int) (Math.log(fileLength) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", fileLength / Math.pow(unit, exp), pre);
    }
}
