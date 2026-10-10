package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Role;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.RoleRepository;
import com.coursework.office_equipment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAdminControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserAdminController controller =
            new UserAdminController(userRepository, roleRepository, employeeRepository, encoder);

    private final RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    private final Role adminRole = TestData.role(1, "ADMIN");
    private final Role viewerRole = TestData.role(3, "VIEWER");

    private Authentication auth(String name) {
        return new UsernamePasswordAuthenticationToken(name, "pw",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void createRejectsShortPassword() {
        String view = controller.create("anna", "123", 3, null, ra);

        assertEquals("redirect:/admin/users", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createSavesUserWithEncodedPasswordRoleAndEmployee() {
        Employee emp = TestData.employee(5, "Анна", TestData.department(1, "IT"));
        when(roleRepository.findById(3)).thenReturn(Optional.of(viewerRole));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(emp));
        when(encoder.encode("secret1")).thenReturn("HASH");

        controller.create("anna", "secret1", 3, 5, ra);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser u = captor.getValue();
        assertEquals("anna", u.getUsername());
        assertEquals("HASH", u.getPasswordHash());
        assertSame(viewerRole, u.getRole());
        assertSame(emp, u.getEmployee());
        assertTrue(u.isActive());
    }

    @Test
    void toggleIsBlockedForOwnAccount() {
        AppUser me = TestData.user(1, "admin", adminRole);
        when(userRepository.findById(1)).thenReturn(Optional.of(me));

        controller.toggle(1, auth("admin"), ra);

        assertTrue(me.isActive());
        assertNotNull(ra.getFlashAttributes().get("error"));
    }

    @Test
    void changeEmployeeRejectsEmployeeOfAnotherUser() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        AppUser olga = TestData.user(3, "olga", viewerRole);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));
        when(userRepository.findByEmployeeId(5)).thenReturn(Optional.of(olga));

        controller.changeEmployee(2, 5, ra);

        assertTrue(((String) ra.getFlashAttributes().get("error")).contains("olga"));
        verify(userRepository, never()).save(any());
    }
}