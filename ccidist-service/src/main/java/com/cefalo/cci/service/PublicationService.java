package com.cefalo.cci.service;

import com.cefalo.cci.model.Publication;

public interface PublicationService {
    Publication getPublication(String publicationId);

    void delete(Publication publication);

    void createPublication(Publication publication);

    void updatePublication(Publication publication);
}
