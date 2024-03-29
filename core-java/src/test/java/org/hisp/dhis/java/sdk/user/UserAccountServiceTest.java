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

import org.hisp.dhis.java.sdk.models.user.User;
import org.hisp.dhis.java.sdk.models.user.UserAccount;
import org.hisp.dhis.java.sdk.common.IModelsStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserAccountServiceTest {
    private UserAccount userAccountMock;
    private IUserAccountService userAccountService;
    private IUserAccountStore userAccountStoreMock;
    private IModelsStore modelsStoreMock;

    @Before
    public void setUp() {
        modelsStoreMock = mock(IModelsStore.class);
        userAccountMock = mock(UserAccount.class);
        userAccountStoreMock = mock(IUserAccountStore.class);
        userAccountStoreMock.insert(userAccountMock);
        userAccountService = new UserAccountService(userAccountStoreMock, modelsStoreMock);

        List<UserAccount> userAccounts = new ArrayList<>();
        userAccounts.add(userAccountMock);
        when(userAccountStoreMock.queryAll()).thenReturn(userAccounts);
        when(userAccountMock.getUId()).thenReturn("2xPJ8ysSeqs");
    }

    @Test
    public void testToUser() {
        User user = userAccountService.toUser(userAccountMock);
        assertEquals(user.getUId(), userAccountMock.getUId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentUserAccountToUser() {
        userAccountService.toUser(null);
    }

    @Test
    public void testGetCurrentUser() {
        UserAccount userAccount = userAccountService.getCurrentUserAccount();
        assertEquals(userAccount, userAccountMock);
    }

    @Test
    public void testGetCurrentUserShouldReturnNullWhenListUserAccountStoreHasNoAccounts() {
        when(userAccountStoreMock.queryAll()).thenReturn(new ArrayList<UserAccount>());

        UserAccount userAccount = userAccountService.getCurrentUserAccount();
        assertNull(userAccount);
    }

    @Test
    public void testGetCurrentUserShouldReturnNullWhenListUserAccountStoreReturnsNull() {
        when(userAccountStoreMock.queryAll()).thenReturn(null);

        UserAccount userAccount = userAccountService.getCurrentUserAccount();
        assertNull(userAccount);
    }

    @Test
    public void testLogOut() {
        userAccountService.logOut();
        verify(modelsStoreMock, atLeastOnce()).deleteAllTables();
    }
}
