package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "rxml_zip_file")
@Cache(region = "com.cefalo.cci.model.RxmlZipFile", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RxmlZipFile extends Persistent implements Serializable, Identifier {
    private static final long serialVersionUID = 1L;

    private long id;
    private String fileName;
    private String designName;
    private String issueName;
    private Date issueDate;
    private Publication publication;
    private Date created;
    private Date updated;

    public RxmlZipFile() {

    }

    public RxmlZipFile(long id) {
        this.id = id;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rxml_seq_gen")
    @SequenceGenerator(name = "rxml_seq_gen", sequenceName = "ccidist_seq_rxml_zip_file")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "design_name")
    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    @Column(name = "issue_name")
    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    @Column(name = "issue_date")
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
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
