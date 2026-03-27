package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.repositories.UserLoginRegisterRepository;
import net.geant.nmaas.portal.persistence.results.UserLoginDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserLoginRegisterServiceImplTest {

    private final UserLoginRegisterRepository repository = mock(UserLoginRegisterRepository.class);
    private final UserLoginRegisterServiceImpl service = new UserLoginRegisterServiceImpl(repository);

    @Test
    void registerNewSuccessfulLoginShouldSaveSuccessEntry() {
        User user = new User("user", true);
        user.setId(11L);

        when(repository.save(org.mockito.ArgumentMatchers.any(UserLoginRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserLoginRegister.class));

        UserLoginRegister result = service.registerNewSuccessfulLogin(user, "host", "agent", "127.0.0.1");

        ArgumentCaptor<UserLoginRegister> captor = ArgumentCaptor.forClass(UserLoginRegister.class);
        verify(repository).save(captor.capture());
        UserLoginRegister saved = captor.getValue();

        assertSame(saved, result);
        assertEquals(11L, saved.getUserId());
        assertSame(user, saved.getUser());
        assertEquals(UserLoginRegisterType.SUCCESS, saved.getType());
        assertEquals("127.0.0.1", saved.getRemoteAddress());
        assertEquals("host", saved.getHost());
        assertEquals("agent", saved.getUserAgent());
        assertNotNull(saved.getDate());
    }

    @Test
    void registerNewFailedLoginShouldSaveFailureEntry() {
        User user = new User("user", true);
        user.setId(12L);

        when(repository.save(org.mockito.ArgumentMatchers.any(UserLoginRegister.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserLoginRegister.class));

        UserLoginRegister result = service.registerNewFailedLogin(user, "host2", "agent2", "10.0.0.1");

        ArgumentCaptor<UserLoginRegister> captor = ArgumentCaptor.forClass(UserLoginRegister.class);
        verify(repository).save(captor.capture());
        UserLoginRegister saved = captor.getValue();

        assertSame(saved, result);
        assertEquals(12L, saved.getUserId());
        assertSame(user, saved.getUser());
        assertEquals(UserLoginRegisterType.FAILURE, saved.getType());
        assertEquals("10.0.0.1", saved.getRemoteAddress());
        assertEquals("host2", saved.getHost());
        assertEquals("agent2", saved.getUserAgent());
        assertNotNull(saved.getDate());
    }

    @Test
    void getLastLoginShouldDelegateToRepository() {
        User user = new User("u1", true);
        Optional<UserLoginRegister> expected = Optional.of(mock(UserLoginRegister.class));
        when(repository.findFirstByUserOrderByDateDesc(user)).thenReturn(expected);

        Optional<UserLoginRegister> result = service.getLastLogin(user);

        assertSame(expected, result);
        verify(repository).findFirstByUserOrderByDateDesc(user);
    }

    @Test
    void getLastSuccessfulLoginShouldDelegateToRepository() {
        User user = new User("u2", true);
        Optional<UserLoginRegister> expected = Optional.of(mock(UserLoginRegister.class));
        when(repository.findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.SUCCESS)).thenReturn(expected);

        Optional<UserLoginRegister> result = service.getLastSuccessfulLogin(user);

        assertSame(expected, result);
        verify(repository).findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.SUCCESS);
    }

    @Test
    void getLastFailedLoginShouldDelegateToRepository() {
        User user = new User("u3", true);
        Optional<UserLoginRegister> expected = Optional.of(mock(UserLoginRegister.class));
        when(repository.findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.FAILURE)).thenReturn(expected);

        Optional<UserLoginRegister> result = service.getLastFailedLogin(user);

        assertSame(expected, result);
        verify(repository).findFirstByUserAndTypeOrderByDateDesc(user, UserLoginRegisterType.FAILURE);
    }

    @Test
    void getFirsLoginShouldDelegateToRepository() {
        User user = new User("u4", true);
        Optional<UserLoginRegister> expected = Optional.of(mock(UserLoginRegister.class));
        when(repository.findFirstByUserOrderByDateAsc(user)).thenReturn(expected);

        Optional<UserLoginRegister> result = service.getFirsLogin(user);

        assertSame(expected, result);
        verify(repository).findFirstByUserOrderByDateAsc(user);
    }

    @Test
    void getAllLoginDetailsShouldDelegateToRepository() {
        List<UserLoginRegister> expected = List.of(mock(UserLoginRegister.class));
        when(repository.findAll()).thenReturn(expected);

        List<UserLoginRegister> result = service.getAllLoginDetails();

        assertSame(expected, result);
        verify(repository).findAll();
    }

    @Test
    void getAllLoginDetailsForUserShouldDelegateToRepository() {
        User user = new User("u5", true);
        List<UserLoginRegister> expected = List.of(mock(UserLoginRegister.class));
        when(repository.findAllByUserOrderByDateDesc(user)).thenReturn(expected);

        List<UserLoginRegister> result = service.getAllLoginDetails(user);

        assertSame(expected, result);
        verify(repository).findAllByUserOrderByDateDesc(user);
    }

    @Test
    void getUserFirstAndLastSuccessfulLoginDateShouldUseUserIdAndSuccessType() {
        User user = new User("u6", true);
        user.setId(66L);
        Optional<UserLoginDate> expected = Optional.of(new UserLoginDate(66L, OffsetDateTime.now().minusDays(2), OffsetDateTime.now()));
        when(repository.findFirstAndLastLoginByUserAndType(66L, UserLoginRegisterType.SUCCESS)).thenReturn(expected);

        Optional<UserLoginDate> result = service.getUserFirstAndLastSuccessfulLoginDate(user);

        assertSame(expected, result);
        verify(repository).findFirstAndLastLoginByUserAndType(66L, UserLoginRegisterType.SUCCESS);
    }

    @Test
    void getAllFirstAndLastSuccessfulLoginDateShouldUseSuccessType() {
        List<UserLoginDate> expected = List.of(new UserLoginDate(1L, OffsetDateTime.now().minusDays(1), OffsetDateTime.now()));
        when(repository.findAllFirstAndLastLoginByType(UserLoginRegisterType.SUCCESS)).thenReturn(expected);

        List<UserLoginDate> result = service.getAllFirstAndLastSuccessfulLoginDate();

        assertSame(expected, result);
        verify(repository).findAllFirstAndLastLoginByType(UserLoginRegisterType.SUCCESS);
    }
}
