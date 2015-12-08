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

package org.hisp.dhis.java.sdk.user;

import org.hisp.dhis.java.sdk.common.IModelsStore;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.java.sdk.models.program.Program;
import org.hisp.dhis.java.sdk.utils.Preconditions;
import org.hisp.dhis.java.sdk.models.user.*;

import java.util.List;

public class UserAccountService implements IUserAccountService {
    private final IUserAccountStore userAccountStore;
    private final IModelsStore modelsStore;

    public UserAccountService(IUserAccountStore userAccountStore, IModelsStore modelsStore) {
        this.userAccountStore = userAccountStore;
        this.modelsStore = modelsStore;
    }

    @Override
    public UserAccount getCurrentUserAccount() {
        List<UserAccount> userAccounts = userAccountStore.queryAll();

        if (userAccounts != null && !userAccounts.isEmpty()) {
            return userAccounts.get(0);
        }
        return null;
    }

    @Override
    public User toUser(UserAccount userAccount) {
        Preconditions.isNull(userAccount, "userAccount must not be null");

        User user = new User();
        user.setUId(userAccount.getUId());
        user.setAccess(userAccount.getAccess());
        user.setCreated(userAccount.getCreated());
        user.setLastUpdated(userAccount.getLastUpdated());
        user.setName(userAccount.getName());
        user.setDisplayName(userAccount.getDisplayName());
        return user;
    }

    @Override
    public void logOut() {
        // removing all existing data
        modelsStore.deleteAllTables();
    }

    @Override
    public List<Program> listAssignedPrograms(OrganisationUnit organisationUnit) {
        return null;
    }

    @Override
    public List<OrganisationUnit> listAssignedOrganisationUnits() {
        return null;
    }
}
