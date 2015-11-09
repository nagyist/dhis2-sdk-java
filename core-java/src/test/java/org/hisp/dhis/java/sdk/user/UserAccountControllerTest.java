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

import org.hisp.dhis.java.sdk.models.user.UserAccount;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class UserAccountControllerTest {
    IUserApiClient userApiClientMock;
    IUserAccountStore userAccountStoreMock;
    IUserAccountController userAccountController;
    UserAccount userAccountMock;

    @Before
    public void setUp() {
        userApiClientMock = mock(IUserApiClient.class);
        userAccountStoreMock = mock(IUserAccountStore.class);
        userAccountMock = mock(UserAccount.class);

        when(userApiClientMock.getUserAccount()).thenReturn(userAccountMock);

        userAccountController = new UserAccountController(userApiClientMock, userAccountStoreMock);
    }

    @Test
    public void testShouldAskUserApiClientForTheUserAccount() {
        userAccountController.updateAccount();

        verify(userApiClientMock, times(1)).getUserAccount();
    }

    @Test
    public void testShouldCallSaveOnTheUserAccountStore() {
        userAccountController.updateAccount();

        verify(userAccountStoreMock, times(1)).save(userAccountMock);
    }

    @Test
    public void testUpdateAccountShouldReturnTheUserAccount() {
        assertSame(userAccountController.updateAccount(), userAccountMock);
    }
}
