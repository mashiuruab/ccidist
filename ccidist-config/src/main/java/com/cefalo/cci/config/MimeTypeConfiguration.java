package com.cefalo.cci.config;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.util.*;

public class MimeTypeConfiguration {
    private final Map<String, String> mimeTypeMap = new HashMap<>();

    public MimeTypeConfiguration(Properties mimeTypeProperties) {
        populateMap(mimeTypeProperties);
    }

    public String getMediaType(String fileName) {
        String extension = Files.getFileExtension(fileName);
        return mimeTypeMap.get(extension.toLowerCase());
    }

    private void populateMap(Properties properties) {
        for (String mimeType : properties.stringPropertyNames()) {
            String propertyValue = Strings.nullToEmpty(properties.getProperty(mimeType));
            if (isBlank(propertyValue)) {
                continue;
            }

            List<String> extensions = Lists.newArrayList(
                    Splitter.on(CharMatcher.WHITESPACE)
                            .trimResults()
                            .omitEmptyStrings()
                            .split(propertyValue));
            for (String extension : extensions) {
                mimeTypeMap.put(extension.toLowerCase(), mimeType);
            }
        }
    }
}
