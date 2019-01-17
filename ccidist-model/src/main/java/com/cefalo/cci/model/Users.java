package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;
import com.google.common.base.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "users")
@Cache(region = "com.cefalo.cci.model.Users", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Users extends Persistent implements Serializable {
    private static final long serialVersionUID = -933573954805282645L;

    public static final String SUPER_USER_NAME="admin";

    private long id;
    private String name;
    private String loginName;
    private String password;
    private Date created;
    private Date updated;
    private UserPrivilege userPrivilege;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "ccidist_seq_users")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "login_name")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreated() {
        return DateUtils.convertDateWithTZ(created);
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getUpdated() {
        return DateUtils.convertDateWithTZ(updated);
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @PrePersist
    protected void onCreate() {
        setCreated(DateUtils.convertDateWithTZ(new Date()));
        setUpdated(DateUtils.convertDateWithTZ(new Date()));
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdated(DateUtils.convertDateWithTZ(new Date()));
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_privilege_id")
    public UserPrivilege getUserPrivilege() {
        return userPrivilege;
    }

    public void setUserPrivilege(UserPrivilege userPrivilege) {
        this.userPrivilege = userPrivilege;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("login", getLoginName()).add("id", getId()).toString();
    }
}
