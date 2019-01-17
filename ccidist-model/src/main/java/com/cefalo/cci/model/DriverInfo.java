package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "driver_info")
@Cache(region = "com.cefalo.cci.model.DriverInfo", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DriverInfo extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Publication publication;
    private DesignToEpubMapper designToEpubMapper;
    private boolean preGenerate;
    private String os;
    private String osVersion;
    private String reader;
    private String deviceName;
    private Date startDate;
    private Date endDate;
    private Date created;
    private Date updated;
    private boolean internal = false;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "driver_seq_gen")
    @SequenceGenerator(name = "driver_seq_gen", sequenceName = "ccidist_seq_driver_info")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    @JoinColumn(name = "design_to_epub_mapper_id")
    public DesignToEpubMapper getDesignToEpubMapper() {
        return designToEpubMapper;
    }

    public void setDesignToEpubMapper(DesignToEpubMapper designToEpubMapper) {
        this.designToEpubMapper = designToEpubMapper;
    }

    @Column(name = "pre_generate")
    public boolean isPreGenerate() {
        return preGenerate;
    }

    public void setPreGenerate(boolean preGenerate) {
        this.preGenerate = preGenerate;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Column(name = "os_version")
    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    @Column(name = "device_name")
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    public Date getStartDate() {
        return DateUtils.convertDateWithTZ(startDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    public Date getEndDate() {
        return DateUtils.convertDateWithTZ(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    @Column(name = "internal")
    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
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
}
