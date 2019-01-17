package com.cefalo.cci.dao;

import com.cefalo.cci.model.Publication;
import com.google.inject.persist.Transactional;

import java.util.List;

public class PublicationDaoImpl extends EntityManagerDao implements PublicationDao {

    @Override
    public Publication getPublication(String id) {
        return getEntityManager().find(Publication.class, id);
    }

    @Override
    @Transactional
    public void saveOrUpdate(Publication publication) {
        getEntityManager().persist(publication);
    }

    @Override
    @Transactional
    public void delete(Publication publication) {
        getEntityManager().remove(publication);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Publication> allPublicationList() {
        return getEntityManager().createQuery("Select p from Publication p ").getResultList();
    }

}

