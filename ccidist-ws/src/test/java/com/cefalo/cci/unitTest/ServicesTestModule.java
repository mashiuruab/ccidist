package com.cefalo.cci.unitTest;

import com.cefalo.cci.utils.XpathHelper;
import com.google.inject.AbstractModule;

public class ServicesTestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(XpathHelper.class);
    }

}
