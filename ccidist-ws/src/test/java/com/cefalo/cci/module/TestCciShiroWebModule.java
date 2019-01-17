package com.cefalo.cci.module;

import com.cefalo.cci.security.AuthenticationRealm;
import org.apache.shiro.guice.web.ShiroWebModule;

import javax.servlet.ServletContext;
import java.util.Arrays;

public class TestCciShiroWebModule extends ShiroWebModule {
    public TestCciShiroWebModule(ServletContext sc) {
        super(sc);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configureShiroWeb() {
        try {
            bindRealm().to(AuthenticationRealm.class);
        } catch (Exception e) {
            addError(e);
        }

        // Lets not add authentication to static files :-)
        // FIXME: Maybe we can take this list of extensions from config??
        for (String extension : Arrays.asList("css", "js", "png", "gif", "jpeg", "jpg")) {
            addFilterChain("/**/*.".concat(extension), ANON);
        }

        // The NO_SESSION_CREATION filter makes sure that no session is ever created. We'll get an Exception if that
        // happens. Fail fast FTW :-)
        addFilterChain("/edit/**", NO_SESSION_CREATION, AUTHC_BASIC);
        addFilterChain("/**/accesstoken", NO_SESSION_CREATION, AUTHC_BASIC);
    }

}
