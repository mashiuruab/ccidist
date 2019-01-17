package com.cefalo.cci.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.shiro.SecurityUtils;

import com.cefalo.cci.dao.UsersDao;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.model.Role;
import com.cefalo.cci.model.UserPrivilege;
import com.cefalo.cci.model.Users;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UsersServiceImpl implements UsersService {
    @Inject
    private UsersDao usersDao;

    @Inject
    private OrganizationService organizationService;

    @Override
    public List<Users> getAllUsers() {
        return usersDao.getAllUsers();
    }

    @Override
    public Users getUser(long id) {
        return usersDao.getUser(id);
    }

    @Override
    public Users getUserByLoginName(String loginName) {
        return usersDao.getUser(loginName);
    }

    @Override
    public void deleteUser(long userId) {
        usersDao.deleteUser(userId);
    }

    @Override
    public List<Role> getAllRoles() {
        return usersDao.getAllRoles();
    }

    @Override
    @Transactional
    public void createUser(String loginName, String userName, String password, String organizationId, long roleId) {
        Users users = new Users();

        users.setName(userName);
        users.setLoginName(loginName);
        users.setPassword(getEncryptedPassword(loginName, password));

        UserPrivilege userPrivilege = new UserPrivilege();
        Organization organization = organizationService.getOrganization(organizationId);
        Role role = usersDao.getRole(roleId);
        userPrivilege.setOrganization(organization);
        userPrivilege.setRole(role);
        users.setUserPrivilege(userPrivilege);

        usersDao.saveUser(users);
    }

    @Override
    @Transactional
    public void updateUser(Users user) {
        usersDao.saveUser(user);
    }

    @Override
    public String getEncryptedPassword(String loginName, String password) {
        String hashedPassword = getMD5Hash(password);
        String hashedUsername = getMD5Hash(loginName);
        return getMD5Hash(hashedUsername.concat(hashedPassword));
    }

    private String getMD5Hash(String str) {
        try {
            return Hashing.md5().newHasher().putBytes(str.getBytes("UTF-8")).hash().toString();
        } catch (UnsupportedEncodingException e) {
            throw  new RuntimeException(e);
        }
    }

    @Override
    public Users authenticateAndGetUser(String loginName, String password) {
        loginName = Strings.nullToEmpty(loginName);
        password = Strings.nullToEmpty(password);

        Users user = usersDao.getUser(loginName);

        if (user != null && getEncryptedPassword(loginName, password).equals(user.getPassword())) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasRole(String publicationId, List<String> allowedRoles) {
        Users user = (Users) SecurityUtils.getSubject().getPrincipal();
        return Users.SUPER_USER_NAME.equals(user.getLoginName())
                || (hasPublication(user.getUserPrivilege().getOrganization().getPublications(), publicationId)
                    && allowedRoles.contains(user.getUserPrivilege().getRole().getName()));
    }

    private boolean hasPublication(List<Publication> publicationList, String publicationId) {
        if (publicationList != null) {
            for (Publication publication : publicationList) {
                if (publicationId.toLowerCase().equals(publication.getId().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
