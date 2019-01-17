package com.cefalo.cci.model;

import com.cefalo.cci.utils.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "events")
@Cache(region = "com.cefalo.cci.model.Events", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Events extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long issueId;
    private String path;
    private int category;
    private Date created;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "events_seq_gen")
    @SequenceGenerator(name = "events_seq_gen", sequenceName = "ccidist_seq_events")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "issue_id")
    public long getIssueId() {
        return issueId;
    }

    public void setIssueId(long issueId) {
        this.issueId = issueId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreated() {
        return DateUtils.convertDateWithTZ(created);
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @PrePersist
    protected void onCreate() {
        setCreated(DateUtils.convertDateWithTZ(new Date()));
    }
}
