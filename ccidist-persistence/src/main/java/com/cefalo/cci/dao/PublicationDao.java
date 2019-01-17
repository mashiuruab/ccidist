package com.cefalo.cci.dao;

import com.cefalo.cci.model.Publication;

import java.util.List;

public interface PublicationDao {
    Publication getPublication(String id);

    void saveOrUpdate(Publication publication);

    void delete(Publication publication);

    List<Publication> allPublicationList();
}
