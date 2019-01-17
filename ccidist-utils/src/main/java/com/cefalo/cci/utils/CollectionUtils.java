package com.cefalo.cci.utils;

import java.util.Collection;

public abstract class CollectionUtils {
    private CollectionUtils() {
        // To make it a proper util class.
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
