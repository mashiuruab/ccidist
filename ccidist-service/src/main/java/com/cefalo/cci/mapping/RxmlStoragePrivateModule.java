package com.cefalo.cci.mapping;

import com.cefalo.cci.model.RxmlZipFile;
import com.cefalo.cci.storage.*;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RxmlStoragePrivateModule extends PrivateModule {
    @Override
    protected void configure() {
        TypeLiteral<Storage<RxmlZipFile>> typeLiteral = new TypeLiteral<Storage<RxmlZipFile>>() {
        };

        TypeLiteral<CacheStorage<RxmlZipFile>> cacheStorageType = new TypeLiteral<CacheStorage<RxmlZipFile>>() {
        };

        bind(typeLiteral).to(cacheStorageType);
        bind(typeLiteral).annotatedWith(Names.named("internal")).to(RxmlDbStorage.class);
        bindConstant().annotatedWith(Names.named("zipDirName")).to("RXMLS");
        bind(new TypeLiteral<ConcurrentMap<Long, Object>>() {
        }).annotatedWith(Names.named("extractionLock")).toInstance(new ConcurrentHashMap<Long, Object>());

        expose(typeLiteral);
    }
}
