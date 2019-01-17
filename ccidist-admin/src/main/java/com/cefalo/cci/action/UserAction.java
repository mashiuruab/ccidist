package com.cefalo.cci.action;

import static com.cefalo.cci.model.Users.SUPER_USER_NAME;
import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.CharMatcher;
import org.apache.shiro.authz.UnauthorizedException;

import com.cefalo.cci.config.ApplicationConfiguration;
import com.cefalo.cci.model.Organization;
import com.cefalo.cci.model.Role;
import com.cefalo.cci.model.UserPrivilege;
import com.cefalo.cci.model.Users;
import com.cefalo.cci.service.OrganizationService;
import com.cefalo.cci.service.UsersService;
import com.google.common.base.Objects;

/**
 * Handle Create, Read and Edit of users.
 *
 * @author partha
 *
 */
public class UserAction extends AuthenticatedActionSupport {
    private static final long serialVersionUID = 8141596896552621893L;

    private OrganizationService organizationService;
    private UsersService usersService;

    private long userId = -1;
    private String userName;
    private String loginName;
    private String password;
    private String retypePassword;
    private String organizationId;
    private long roleId;

    private boolean createForm;

    private List<Organization> organizationList;
    private List<Role> roleList;

    @Inject
    public UserAction(ApplicationConfiguration config, OrganizationService organizationService,
            UsersService usersService) {
        super(config);

        this.organizationService = organizationService;
        this.usersService = usersService;
    }

    /**
     * Entry method for read/create/edit user.
     *
     * @return
     */
    public String populateBeans() {
        createForm = true;
        if (userId > 0) {
            createForm = false;

            Users user = usersService.getUser(Long.valueOf(userId));
            setLoginName(user.getLoginName());
            setUserName(user.getName());
            if (!SUPER_USER_NAME.equals(user.getLoginName())) {
                setOrganizationId(user.getUserPrivilege().getOrganization().getId());
                setRoleId(user.getUserPrivilege().getRole().getId());
            }
        }

        setPagetTitle(getText(createForm ? "message.createUser" : "message.editUser"));

        return "done";
    }

    public String saveOrUpdate() {
        if (userId <= 0) {
            // Creating a new user here. Only admin does this.
            usersService.createUser(loginName, userName, password, organizationId, roleId);
            return "userList";
        }

        // First load the user
        Users user = usersService.getUser(userId);
        user.setName(userName);

        if (!isBlank(password)) {
            // Set the encrypted password
            user.setPassword(usersService.getEncryptedPassword(loginName, password));
        }

        if (!isBlank(organizationId) && roleId > 0) {
            UserPrivilege privilege = user.getUserPrivilege();
            privilege.setOrganization(organizationService.getOrganization(organizationId));
            privilege.setRole(findRole());
        }

        usersService.updateUser(user);

        return isSuperUser() ? "userList" : "orgList";
    }

    private Role findRole() {
        for (Role role : roleList) {
            if (role.getId() == roleId) {
                return role;
            }
        }

        throw new RuntimeException(String.format("Unable to find role with id: %s", roleId));
    }

    private void loadCommonList() {
        organizationList = organizationService.getAllOrganizations();
        roleList = usersService.getAllRoles();
    }

    @Override
    public void validate() {
        super.validate();

        // FIXME: Probably not the right place to do this.
        loadCommonList();

        setPagetTitle(getText(userId <= 0 ? "message.createUser" : "message.editUser"));
        createForm = userId <= 0;

        Users loggedInUser = getLoggedInUser();
        String httpMethod = getRequestMethod();
        if ("GET".equals(httpMethod)) {
            // Create form is only allowed by the admin user
            if (userId <= 0 && !isSuperUser()) {
                addActionError("User creation form is only allowed for super user");
            } else if (userId > 0 && !isSuperUser() && userId != loggedInUser.getId()) {
                addActionError("You can only read your own information (unless you are super user)");
            }

            return;
        }

        if (!"POST".equals(httpMethod)) {
            throw new IllegalArgumentException("Only GET & POST is allowed");
        }

        // We are going to create/edit a new user via a POST request
        if (isBlank(loginName)) {
            addFieldError("loginName", getText("message.required"));
        } else if (!getLoginName().matches("^([A-Za-z]|[0-9]|-|_)+$")) {
            addFieldError("loginName", getText("message.name.alphanumeric", new String[] {"Login Name"}));
        }

        if (isBlank(userName)) {
            addFieldError("userName", getText("message.required"));
        }

        if (createForm) {
            // This is a create user request. Only super user is allowed to do this.
            if (!isSuperUser()) {
                throw new UnauthorizedException("Only super user can create new users.");
            }

            if (!isAvailableLoginName()) {
                addFieldError("loginName", getText("message.login.not.available"));
            }

            if (isBlank(password)) {
                addFieldError("password", getText("message.required"));
            } else if (!CharMatcher.ASCII.matchesAllOf(getPassword())) {
                addFieldError("password", getText("message.password.ascii"));
            }

            if (isBlank(retypePassword)) {
                addFieldError("retypePassword", getText("message.required"));
            }
        } else {
            // This is an update request for an user.
            if (!isSuperUser() && loggedInUser.getId() != userId) {
                throw new UnauthorizedException("You can only change your own password (unless you are super user).");
            }
            if (!isBlank(password) && !CharMatcher.ASCII.matchesAllOf(getPassword())) {
                addFieldError("password", getText("message.password.ascii"));
            }
        }

        if (!Objects.equal(password, retypePassword)) {
            addFieldError("retypePassword", getText("message.retype.password.not.match"));
        }

        if (createForm || (isSuperUser() && !SUPER_USER_NAME.equals(loginName))) {
            // For create and edit by admin for other users, we need the org/role thing
            if (isBlankSelectValue(organizationId)) {
                addFieldError("organizationId", getText("message.required"));
            }

            if (organizationService.getOrganization(organizationId) == null) {
                addFieldError("organizationId", getText("message.organization.not.found"));
            }

            if (roleId <= 0) {
                addFieldError("roleId", getText("message.required"));
            }
        } else if (!isSuperUser() || (isSuperUser() && SUPER_USER_NAME.equals(loginName))) {
            if (!isBlankSelectValue(organizationId)) {
                addFieldError("organizationId", "Organization ID can not be changed.");
            }
            if (roleId > 0) {
                addFieldError("roleId", "Role id can not be changed.");
            }
        }
    }

    private boolean isBlankSelectValue(String selctValue) {
        return isBlank(selctValue) || "-1".equals(selctValue);
    }

    private boolean isAvailableLoginName() {
        return usersService.getUserByLoginName(loginName) == null;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public List<Organization> getOrganizationList() {
        return organizationList;
    }

    public String getPassword() {
        return password;
    }

    public String getRetypePassword() {
        return retypePassword;
    }

    public long getRoleId() {
        return roleId;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRetypePassword(String retypePassword) {
        this.retypePassword = retypePassword;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isCreateForm() {
        return createForm;
    }
}
