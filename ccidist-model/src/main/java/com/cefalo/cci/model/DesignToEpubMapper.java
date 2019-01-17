package com.cefalo.cci.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "design_to_epub_mapper")
@Cache(region = "com.cefalo.cci.model.DesignToEpubMapper", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DesignToEpubMapper  extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String designName;
    private String epubName;

    public DesignToEpubMapper() {

    }

    public DesignToEpubMapper(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "dem_seq_gen")
    @SequenceGenerator(name = "dem_seq_gen", sequenceName = "ccidist_seq_epub_mapper")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "design_name")
    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    @Column(name = "epub_name")
    public String getEpubName() {
        return epubName;
    }

    public void setEpubName(String epubName) {
        this.epubName = epubName;
    }
}
