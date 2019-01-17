package com.cefalo.cci.unitTest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ ServicesTestModule.class })
public class ServiceUnitTest {

    @Test
    public void getAllFileNamesInDirectoryTest() {
        checkInvalidDirectoryPath(null, "", "   ");

        Path directoryPath = Paths.get("src", "test", "resources", "epubs");
        List<String> listFileNames = getAllFileNamesInDirectory(directoryPath.toAbsolutePath().toString());
        assertEquals(1, listFileNames.size());
        assertTrue(listFileNames.contains("fileTest.txt"));
    }

    private void checkInvalidDirectoryPath(final String... paths) {
        for (String path : paths) {
            try {
                getAllFileNamesInDirectory(path);
                fail("Empty directory should be an invalid parameter");
            } catch (IllegalArgumentException ex) {
                // This is expected
            }
        }
    }

    @Test
    public void areSetsEquals() {
        Set<String> from = Sets.newSet("ipad", "iphone");
        Set<String> to = Sets.newSet("iphone", "ipad");
        assertEquals(true, areSetsEquals(from, to));

        from = Sets.newSet("ipad", "iphone", "mini-ipad");
        to = Sets.newSet("ipad", "iphone");
        assertEquals(false, areSetsEquals(from, to));

        from = Sets.newSet("ipad", "iphone");
        to = Sets.newSet("ipad", "iphone", "mini-ipad");
        assertEquals(false, areSetsEquals(from, to));

        from = Sets.newSet("iipad", "iphone");
        to = Sets.newSet("ipad", "iphone");
        assertEquals(false, areSetsEquals(from, to));

        from = Sets.newSet("  ipad", "  iphone");
        to = Sets.newSet("ipad", "iphone");
        assertEquals(false, areSetsEquals(from, to));

        from = com.google.common.collect.Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
                .split("ipad,    iphone,   mini-ipad"));
        to = com.google.common.collect.Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
                .split("ipad,iphone, mini-ipad"));
        assertEquals(true, areSetsEquals(from, to));

        from = com.google.common.collect.Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
                .split("ipad"));
        to = com.google.common.collect.Sets.newHashSet(Splitter.on(",").omitEmptyStrings().trimResults()
                .split("ipad,     iphone"));
        assertEquals(false, areSetsEquals(from, to));

    }

    public List<String> getAllFileNamesInDirectory(final String directory) {
        String dir = Strings.nullToEmpty(directory);
        Preconditions.checkArgument(dir.trim().length() > 0, "Directory path may not be empty or null.");

        final List<String> epubFileNames = new ArrayList<String>();
        try {
            final Path directoryPath = Paths.get(directory);
            Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    epubFileNames.add(file.getFileName().toString());
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.equals(directoryPath)) {
                        return super.preVisitDirectory(dir, attrs);
                    }

                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return epubFileNames;
    }

    public boolean areSetsEquals(Set<String> from, Set<String> to) {
        return com.google.common.collect.Sets.symmetricDifference(from, to).isEmpty();
    }
}
