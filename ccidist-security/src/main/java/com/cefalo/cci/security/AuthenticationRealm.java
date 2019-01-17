package com.cefalo.cci.security;

import com.cefalo.cci.model.Users;
import com.cefalo.cci.service.UsersService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

import static com.cefalo.cci.model.Users.SUPER_USER_NAME;
import static com.cefalo.cci.utils.StringUtils.isBlank;

public class AuthenticationRealm extends AuthorizingRealm {
    private Provider<UsersService> usersServiceProvider;

    @Inject
    public AuthenticationRealm(Provider<UsersService> usersServiceProvider) {
        this.usersServiceProvider = usersServiceProvider;
    }

    private UsersService getUserService() {
        return usersServiceProvider.get();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String userName = upToken.getUsername();
        if (isBlank(userName)) {
            throw new AccountException("Blank username is not allowed by this realm.");
        }

        String password = String.valueOf(upToken.getPassword());
        if (isBlank(password)) {
            throw new AccountException("Blank password is not allowed by this realm.");
        }

        Users user = getUserService().authenticateAndGetUser(userName, password);

        if (user == null) {
            throw new AccountException("User is unauthorized!");
        }

        return new SimpleAuthenticationInfo(user, upToken.getCredentials(), this.getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }
        Users user = principals.oneByType(Users.class);
        Set<String> roleNames = ImmutableSet.of();

        if (SUPER_USER_NAME.equals(user.getLoginName())) {
            roleNames = ImmutableSet.of(SUPER_USER_NAME);
        } else {
            roleNames = ImmutableSet.of(user.getUserPrivilege().getRole().getName());
        }
        return new SimpleAuthorizationInfo(roleNames);
    }

    @Override
    public String getName() {
        return "CCI Distribution Service Administration Area";
    }
}
