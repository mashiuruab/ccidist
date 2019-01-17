package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "matching_rules")
@Cache(region = "com.cefalo.cci.model.MatchingRules", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MatchingRules extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private DesignToEpubMapper designToEpubMapper;
    private Publication publication;
    private int width;
    private int height;
    private String deviceName;
    private String os;
    private String osv;
    private String readerVersion;
    private Date created;
    private Date updated;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rules_seq_gen")
    @SequenceGenerator(name = "rules_seq_gen", sequenceName = "ccidist_seq_matching_rules")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_to_epub_mapper_id")
    public DesignToEpubMapper getDesignToEpubMapper() {
        return designToEpubMapper;
    }

    public void setDesignToEpubMapper(DesignToEpubMapper designToEpubMapper) {
        this.designToEpubMapper = designToEpubMapper;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsv() {
        return osv;
    }

    public void setOsv(String osv) {
        this.osv = osv;
    }

    @Column(name = "reader_version")
    public String getReaderVersion() {
        return readerVersion;
    }

    public void setReaderVersion(String readerVersion) {
        this.readerVersion = readerVersion;
    }

    @Column(name = "device_name")
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
}
