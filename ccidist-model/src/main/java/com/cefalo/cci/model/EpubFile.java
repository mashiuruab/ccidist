package com.cefalo.cci.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Table(name = "epub_file")
// This entity contains large byte[]. So this is not cached. Plus the CachedStorage caches parts of this in FS anyway.
public class EpubFile extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Blob fileContent;
    private Issue issue;

    public EpubFile() {

    }

    public EpubFile(long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "epub_seq_gen")
    @SequenceGenerator(name = "epub_seq_gen", sequenceName = "ccidist_seq_epub_file")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Lob
    @Column(name = "file_content")
    public Blob getFileContent() {
        return fileContent;
    }

    public void setFileContent(Blob fileContent) {
        this.fileContent = fileContent;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }
}
