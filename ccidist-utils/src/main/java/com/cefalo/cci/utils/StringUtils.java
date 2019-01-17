package com.cefalo.cci.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public abstract class StringUtils {
    private StringUtils() {

    }

    public static boolean isBlank(final String str) {
        return Strings.isNullOrEmpty(str) || CharMatcher.WHITESPACE.matchesAllOf(str);
    }

    public static String createCsv(List<?> list) {
        if (list == null) {
            return "";
        }

        return Joiner.on(",").skipNulls().join(list);
    }

    public static List<String> convertToStringList(final List<?> objList) {
        List<String> stringList = new ArrayList<>();
        if (objList != null) {
            for (Object obj : objList) {
                stringList.add(String.valueOf(obj));
            }
        }

        return stringList;
    }

    public static String createETagHeaderValue(final long value) {
        return String.format("\"%s\"", value);
    }

    public static String urlDecode(String text) {
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public static String urlEncode(String text) {
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return text;
    }
}
