package com.cefalo.cci.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Table(name = "rxml_binary_file")
public class RxmlBinaryFile extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Blob fileContent;
    private RxmlZipFile rxmlZipFile;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rxml_binary_seq_gen")
    @SequenceGenerator(name = "rxml_binary_seq_gen", sequenceName = "ccidist_seq_rxml_binary_file")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rxml_zip_file_id")
    public RxmlZipFile getRxmlZipFile() {
        return rxmlZipFile;
    }

    public void setRxmlZipFile(RxmlZipFile rxmlZipFile) {
        this.rxmlZipFile = rxmlZipFile;
    }

    @Lob
    @Column(name = "file_content")
    public Blob getFileContent() {
        return fileContent;
    }

    public void setFileContent(Blob fileContent) {
        this.fileContent = fileContent;
    }

}
