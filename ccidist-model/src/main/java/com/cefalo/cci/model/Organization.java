package com.cefalo.cci.model;


import com.cefalo.cci.utils.DateUtils;
import org.hibernate.annotations.Cache;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "organization")
@Cache(region = "com.cefalo.cci.model.Organization", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Organization  extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Date created;
    private Date updated;
    private List<Publication> publications;

    public Organization() {}

    public Organization(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    @OrderBy("name asc")
    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
}
