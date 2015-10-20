package org.hisp.dhis.sdk.java.user;

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
