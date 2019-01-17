package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "issue")
@Cache(region = "com.cefalo.cci.model.Issue", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Issue extends Persistent implements Serializable, Identifier {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private Publication publication;
    private Date created;
    private Date updated;
    private int status;
    /**
     * Stale basically means that the issue binary has to be re-generated. This will have to be done by using the
     * associated {@link RxmlZipFile} & {@link DriverInfo}.
     */
    private boolean stale;
    private DriverInfo driverInfo;
    private RxmlZipFile rxmlZipFile;

    @Override
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "issue_seq_gen")
    @SequenceGenerator(name = "issue_seq_gen", sequenceName = "ccidist_seq_issue")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_info_id")
    public DriverInfo getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(DriverInfo driverInfo) {
        this.driverInfo = driverInfo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zip_file_id")
    public RxmlZipFile getRxmlZipFile() {
        return rxmlZipFile;
    }

    public void setRxmlZipFile(RxmlZipFile rxmlZipFile) {
        this.rxmlZipFile = rxmlZipFile;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "stale")
    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
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

    @PreUpdate
    protected void onUpdate() {
        setUpdated(DateUtils.convertDateWithTZ(new Date()));
    }
}
