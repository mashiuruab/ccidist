package com.cefalo.cci.action.admin;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.service.UsersService;

public class UserDeleteAction extends AdministrativeActionSupport {
    private static final long serialVersionUID = 8141596896552621893L;

    private final Logger logger = LoggerFactory.getLogger(UserDeleteAction.class);

    private UsersService usersService;

    private long userId = -1;

    @Inject
    public UserDeleteAction(ApplicationConfiguration config, UsersService usersService) {
        super(config);

        this.usersService = usersService;
    }

    /**
     * Invoked for deleting users.
     *
     * @return
     */
    public String deleteUser() {
        usersService.deleteUser(Long.valueOf(userId));

        return "deleted";
    }

    @Override
    public void validate() {
        super.validate();

        if (userId <= 0) {
            logger.error("User ID must be > 0. Provided: {}", userId);
            addFieldError("userId", getText("message.userId.number"));
        }
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
