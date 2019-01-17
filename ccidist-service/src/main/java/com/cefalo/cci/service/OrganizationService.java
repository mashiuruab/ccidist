/*
 * $Header$
 *
 * Copyright (C) 2013 Escenic AS.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Escenic AS.  This notice must be included on all
 * copies, modifications and derivatives of this work.
 */
package com.cefalo.cci.service;

import com.cefalo.cci.model.Organization;

import java.util.List;

public interface OrganizationService {
    List<Organization> getAllOrganizations();

    Organization getOrganization(String id);

    void delete(String organizationId);

    void createOrganization(Organization organization);

    void updateOrganization(Organization organization);
}
