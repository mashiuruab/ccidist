package com.cefalo.cci.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.model.Role;
import com.cefalo.cci.model.Users;
import com.google.inject.persist.Transactional;

public class UsersDaoImpl extends EntityManagerDao implements UsersDao {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @SuppressWarnings("unchecked")
    public List<Users> getAllUsers() {
        return getEntityManager().createQuery("From Users  u order by u.name").getResultList();
    }

    @Override
    public Users getUser(long id) {
        return getEntityManager().find(Users.class, id);
    }

    @Override
    public Users getUser(String loginName) {
        @SuppressWarnings("unchecked")
        List<Users> results = getEntityManager().createQuery("select u from Users u where u.loginName like :loginName")
                .setParameter("loginName", loginName)
                .setHint("org.hibernate.cacheable", true) // This is a heavily used method on "admin" webapp.
                .getResultList();

        // We expect a single result. For anything else, we return null
        if (results.isEmpty()) {
            return null;
        }

        if (results.size() > 1) {
            logger.error(
                    "It seems that there are duplicate users for the username '{}'. Your data is possibly corrupted.",
                    loginName);
            return null;
        }

        return results.get(0);
    }


    @Override
    @Transactional
    public void deleteUser(long userId) {
        Users users = getUser(userId);
        getEntityManager().remove(users);
    }

    @Override
    public void saveUser(Users users) {
        getEntityManager().persist(users);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Role> getAllRoles() {
        return getEntityManager().createQuery("From Role").getResultList();
    }

    @Override
    public Role getRole(long id) {
        return getEntityManager().find(Role.class, id);
    }
}
