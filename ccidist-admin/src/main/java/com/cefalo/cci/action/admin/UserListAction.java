package com.cefalo.cci.action.admin;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Users;
import com.cefalo.cci.service.UsersService;
import java.util.List;

import javax.inject.Inject;

public class UserListAction extends AdministrativeActionSupport {
    private static final long serialVersionUID = 835406112161442622L;

    private final UsersService usersService;
    private List<Users> userList;

    @Inject
    public UserListAction(ApplicationConfiguration config, UsersService usersService) {
        super(config);

        this.usersService = usersService;
    }

    public String loadUserList() {
        userList = usersService.getAllUsers();

        setPagetTitle(getText("message.users"));

        return "done";
    }

    public List<Users> getUserList() {
        return userList;
    }
}
