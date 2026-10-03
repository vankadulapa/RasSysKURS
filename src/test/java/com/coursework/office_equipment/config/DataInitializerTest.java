package com.coursework.office_equipment.config;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.repository.RoleRepository;
import com.coursework.office_equipment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final DataInitializer initializer =
            new DataInitializer(userRepository, roleRepository, encoder);

    private void givenRolesExist() {
        when(roleRepository.findByName(anyString()))
                .thenAnswer(inv -> Optional.of(TestData.role(1, inv.getArgument(0))));
        when(encoder.encode(anyString())).thenAnswer(inv -> "enc:" + inv.getArgument(0));
    }

    private List<AppUser> savedUsers(int times) {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository, times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void createsThreeDefaultUsersWhenMissing() {
        givenRolesExist();

        initializer.run();

        List<AppUser> saved = savedUsers(3);
        assertEquals(List.of("admin", "manager", "viewer"),
                saved.stream().map(AppUser::getUsername).toList());
        assertEquals(List.of("ADMIN", "MANAGER", "VIEWER"),
                saved.stream().map(u -> u.getRole().getName()).toList());
        assertTrue(saved.stream().allMatch(AppUser::isActive));
    }

    @Test
    void passwordsAreStoredOnlyAsHashes() {
        givenRolesExist();

        initializer.run();

        List<AppUser> saved = savedUsers(3);
        assertEquals("enc:admin123", saved.get(0).getPasswordHash());
        assertNotEquals("admin123", saved.get(0).getPasswordHash());
    }

    @Test
    void existingUsersAreNotCreatedTwice() {
        givenRolesExist();
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(TestData.user(1, "admin", TestData.role(1, "ADMIN"))));

        initializer.run();

        List<AppUser> saved = savedUsers(2);
        assertEquals(List.of("manager", "viewer"),
                saved.stream().map(AppUser::getUsername).toList());
    }

    @Test
    void failsWhenRolesAreMissingInDatabase() {
        assertThrows(NoSuchElementException.class, () -> initializer.run());
    }
}