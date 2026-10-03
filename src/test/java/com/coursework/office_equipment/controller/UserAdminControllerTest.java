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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.NoSuchElementException;
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

    private String error() {
        return (String) ra.getFlashAttributes().get("error");
    }

    private String message() {
        return (String) ra.getFlashAttributes().get("message");
    }

    private AppUser savedUser() {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void listAddsUsersRolesEmployeesAndCurrentUser() {
        Model model = new ExtendedModelMap();

        String view = controller.list(model, auth("admin"));

        assertEquals("admin/users", view);
        assertEquals("admin", model.asMap().get("currentUser"));
        assertNotNull(model.asMap().get("users"));
        assertNotNull(model.asMap().get("roles"));
        assertNotNull(model.asMap().get("employees"));
    }

    @Test
    void createRejectsShortPassword() {
        String view = controller.create("anna", "123", 1, null, ra);

        assertEquals("redirect:/admin/users", view);
        assertNotNull(error());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsEmptyLogin() {
        controller.create("   ", "secret1", 1, null, ra);

        assertNotNull(error());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateLogin() {
        when(userRepository.findByUsername("anna"))
                .thenReturn(Optional.of(TestData.user(1, "anna", viewerRole)));

        controller.create("anna", "secret1", 3, null, ra);

        assertTrue(error().contains("уже существует"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsEmployeeLinkedToAnotherUser() {
        when(userRepository.findByEmployeeId(5))
                .thenReturn(Optional.of(TestData.user(2, "olga", viewerRole)));

        controller.create("anna", "secret1", 3, 5, ra);

        assertTrue(error().contains("уже привязан"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createSavesUserWithEncodedPasswordRoleAndEmployee() {
        Employee emp = TestData.employee(5, "Анна", TestData.department(1, "IT"));
        when(roleRepository.findById(3)).thenReturn(Optional.of(viewerRole));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(emp));
        when(encoder.encode("secret1")).thenReturn("HASH");

        controller.create("anna", "secret1", 3, 5, ra);

        AppUser u = savedUser();
        assertEquals("anna", u.getUsername());
        assertEquals("HASH", u.getPasswordHash());
        assertSame(viewerRole, u.getRole());
        assertSame(emp, u.getEmployee());
        assertTrue(u.isActive());
        assertNotNull(message());
    }

    @Test
    void createTrimsLogin() {
        when(roleRepository.findById(3)).thenReturn(Optional.of(viewerRole));
        when(encoder.encode("secret1")).thenReturn("HASH");

        controller.create("  anna ", "secret1", 3, null, ra);

        assertEquals("anna", savedUser().getUsername());
    }

    @Test
    void createWithoutEmployeeLeavesLinkEmpty() {
        when(roleRepository.findById(3)).thenReturn(Optional.of(viewerRole));
        when(encoder.encode("secret1")).thenReturn("HASH");

        controller.create("anna", "secret1", 3, null, ra);

        assertNull(savedUser().getEmployee());
    }

    @Test
    void changeRoleIsBlockedForOwnAccount() {
        when(userRepository.findById(1)).thenReturn(Optional.of(TestData.user(1, "admin", adminRole)));

        controller.changeRole(1, 3, auth("admin"), ra);

        assertNotNull(error());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeRoleUpdatesAnotherUser() {
        AppUser anna = TestData.user(2, "anna", adminRole);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));
        when(roleRepository.findById(3)).thenReturn(Optional.of(viewerRole));

        controller.changeRole(2, 3, auth("admin"), ra);

        assertSame(viewerRole, anna.getRole());
        verify(userRepository).save(anna);
    }

    @Test
    void toggleIsBlockedForOwnAccount() {
        AppUser me = TestData.user(1, "admin", adminRole);
        when(userRepository.findById(1)).thenReturn(Optional.of(me));

        controller.toggle(1, auth("admin"), ra);

        assertTrue(me.isActive());
        assertNotNull(error());
    }

    @Test
    void toggleDisablesActiveUser() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));

        controller.toggle(2, auth("admin"), ra);

        assertFalse(anna.isActive());
        verify(userRepository).save(anna);
    }

    @Test
    void toggleEnablesDisabledUser() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        anna.setActive(false);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));

        controller.toggle(2, auth("admin"), ra);

        assertTrue(anna.isActive());
    }

    @Test
    void deleteIsBlockedForOwnAccount() {
        when(userRepository.findById(1)).thenReturn(Optional.of(TestData.user(1, "admin", adminRole)));

        controller.delete(1, auth("admin"), ra);

        assertNotNull(error());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesAnotherUser() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));

        controller.delete(2, auth("admin"), ra);

        verify(userRepository).delete(anna);
        assertNotNull(message());
    }

    @Test
    void changeEmployeeWithEmptyValueUnlinks() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        anna.setEmployee(TestData.employee(5, "Анна", TestData.department(1, "IT")));
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));

        controller.changeEmployee(2, null, ra);

        assertNull(anna.getEmployee());
        verify(userRepository).save(anna);
    }

    @Test
    void changeEmployeeLinksFreeEmployee() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        Employee emp = TestData.employee(5, "Анна", TestData.department(1, "IT"));
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(emp));

        controller.changeEmployee(2, 5, ra);

        assertSame(emp, anna.getEmployee());
        verify(userRepository).save(anna);
    }

    @Test
    void changeEmployeeRejectsEmployeeOfAnotherUser() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        AppUser olga = TestData.user(3, "olga", viewerRole);
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));
        when(userRepository.findByEmployeeId(5)).thenReturn(Optional.of(olga));

        controller.changeEmployee(2, 5, ra);

        assertTrue(error().contains("olga"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmployeeAllowsKeepingTheSameLink() {
        AppUser anna = TestData.user(2, "anna", viewerRole);
        Employee emp = TestData.employee(5, "Анна", TestData.department(1, "IT"));
        when(userRepository.findById(2)).thenReturn(Optional.of(anna));
        when(userRepository.findByEmployeeId(5)).thenReturn(Optional.of(anna));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(emp));

        controller.changeEmployee(2, 5, ra);

        assertNull(error());
        verify(userRepository).save(anna);
    }

    @Test
    void unknownUserCausesNotFound() {
        assertThrows(NoSuchElementException.class, () -> controller.toggle(99, auth("admin"), ra));
    }
}