package com.cefalo.cci.service;

import com.cefalo.cci.model.Role;
import com.cefalo.cci.model.Users;

import java.util.List;

public interface UsersService {
    List<Users> getAllUsers();

    Users getUser(long id);

    Users getUserByLoginName(String loginName);

    void deleteUser(long id);

    List<Role> getAllRoles();

    void createUser(String loginName,String userName, String password, String organizationId, long roleId);

    void updateUser(Users user);

    Users authenticateAndGetUser(String loginName, String password);

    String getEncryptedPassword(String loginName, String password);

    boolean hasRole(String publicationId, List<String> allowedRoles);
}
