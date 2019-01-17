package com.cefalo.cci.mapping;

import com.cefalo.cci.model.Issue;
import com.cefalo.cci.storage.CacheStorage;
import com.cefalo.cci.storage.EpubDbStorage;
import com.cefalo.cci.storage.Storage;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EpubStoragePrivateModule extends PrivateModule {
    @Override
    protected void configure() {
        TypeLiteral<Storage<Issue>> typeLiteral = new TypeLiteral<Storage<Issue>>() {
        };

        TypeLiteral<CacheStorage<Issue>> cacheStorageType = new TypeLiteral<CacheStorage<Issue>>() {
        };

        bind(typeLiteral).to(cacheStorageType);
        bind(typeLiteral).annotatedWith(Names.named("internal")).to(EpubDbStorage.class);
        bindConstant().annotatedWith(Names.named("zipDirName")).to("EPUBS");
        bind(new TypeLiteral<ConcurrentMap<Long, Object>>() {
        }).annotatedWith(Names.named("extractionLock")).toInstance(new ConcurrentHashMap<Long, Object>());

        expose(typeLiteral);
    }
}
