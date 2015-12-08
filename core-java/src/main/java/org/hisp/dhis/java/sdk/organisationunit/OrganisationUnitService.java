/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.java.sdk.organisationunit;

import org.hisp.dhis.java.sdk.common.IStateStore;
import org.hisp.dhis.java.sdk.models.common.state.Action;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.utils.Preconditions;

import java.util.List;

public class OrganisationUnitService implements IOrganisationUnitService {

    private final IOrganisationUnitStore organisationUnitStore;
    private final IStateStore stateStore;

    public OrganisationUnitService(IOrganisationUnitStore organisationUnitStore, IStateStore stateStore) {
        this.organisationUnitStore = organisationUnitStore;
        this.stateStore = stateStore;
    }

    @Override
    public boolean save(OrganisationUnit object) {
        Preconditions.isNull(object, "Dashboard object must not be null");

        Action action = stateStore.queryActionForModel(object);
        if (action == null) {
            boolean status = organisationUnitStore.save(object);

            if (status) {
                status = stateStore.saveActionForModel(object, Action.TO_POST);
            }

            return status;
        }

        boolean status = false;
        switch (action) {
            case TO_POST:
            case TO_UPDATE: {
                status = organisationUnitStore.save(object);
                break;
            }
            case SYNCED: {
                status = organisationUnitStore.save(object);

                if (status) {
                    status = stateStore.saveActionForModel(object, Action.TO_UPDATE);
                }
                break;
            }
            case TO_DELETE: {
                status = false;
                break;
            }

        }

        return status;
    }

    @Override
    public boolean remove(OrganisationUnit object) {
        Preconditions.isNull(object, "Dashboard object must not be null");

        Action action = stateStore.queryActionForModel(object);
        if (action == null) {
            return false;
        }

        boolean status = false;
        switch (action) {
            case SYNCED:
            case TO_UPDATE: {
                status = stateStore.saveActionForModel(object, Action.TO_DELETE);
                break;
            }
            case TO_POST: {
                status = organisationUnitStore.delete(object);
                break;
            }
            case TO_DELETE: {
                status = false;
                break;
            }
        }

        return status;
    }

    @Override
    public OrganisationUnit get(long id) {
        OrganisationUnit organisationUnit = organisationUnitStore.queryById(id);

        if (organisationUnit != null) {
            Action action = stateStore.queryActionForModel(organisationUnit);

            if (!Action.TO_DELETE.equals(action)) {
                return organisationUnit;
            }
        }
        return null;
    }

    @Override
    public OrganisationUnit get(String uid) {
        OrganisationUnit organisationUnit = organisationUnitStore.queryByUid(uid);

        if (organisationUnit != null) {
            Action action = stateStore.queryActionForModel(organisationUnit);

            if (!Action.TO_DELETE.equals(action)) {
                return organisationUnit;
            }
        }
        return null;
    }

    @Override
    public List<OrganisationUnit> list() {
        return stateStore.queryModelsWithActions(OrganisationUnit.class,
                Action.SYNCED, Action.TO_POST, Action.TO_UPDATE);
    }
}
