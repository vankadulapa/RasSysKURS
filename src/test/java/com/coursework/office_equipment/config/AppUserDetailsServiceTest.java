package com.coursework.office_equipment.config;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AppUserDetailsService service = new AppUserDetailsService(userRepository);

    @Test
    void loadsUserWithRoleAuthorityAndActiveFlag() {
        AppUser u = TestData.user(1, "anna", TestData.role(2, "MANAGER"));
        u.setPasswordHash("stored-hash");
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("anna");

        assertEquals("anna", details.getUsername());
        assertEquals("stored-hash", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }
}