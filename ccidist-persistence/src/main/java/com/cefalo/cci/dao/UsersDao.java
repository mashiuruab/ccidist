package com.cefalo.cci.dao;

import com.cefalo.cci.model.Role;
import com.cefalo.cci.model.Users;

import java.util.List;

public interface UsersDao {
    List<Users> getAllUsers();

    Users getUser(long id);

    void deleteUser(long id);

    void saveUser(Users users);

    List<Role> getAllRoles();

    Role getRole(long id);

    Users getUser(String loginName);
}
