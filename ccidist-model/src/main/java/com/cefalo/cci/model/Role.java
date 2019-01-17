package com.cefalo.cci.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "role")
@Cache(region = "com.cefalo.cci.model.Role", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Role extends Persistent implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "role_seq_gen")
    @SequenceGenerator(name = "role_seq_gen", sequenceName = "ccidist_seq_role")
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
}
