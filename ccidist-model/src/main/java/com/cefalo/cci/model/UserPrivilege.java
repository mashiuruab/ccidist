package com.cefalo.cci.model;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_privilege")
@Cache(region = "com.cefalo.cci.model.UserPrivilege", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserPrivilege extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Organization organization;
    private Role role;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_privilege_seq_gen")
    @SequenceGenerator(name = "user_privilege_seq_gen", sequenceName = "ccidist_seq_user_privilege")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
