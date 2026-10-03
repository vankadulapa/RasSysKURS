package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.model.EquipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalModelAdviceTest {

    private final GlobalModelAdvice advice = new GlobalModelAdvice();

    private Authentication auth(String name, String... roles) {
        return new UsernamePasswordAuthenticationToken(name, "pw",
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    void usernameIsNullForAnonymous() {
        assertNull(advice.username(null));
    }

    @Test
    void usernameComesFromAuthentication() {
        assertEquals("anna", advice.username(auth("anna", "ROLE_VIEWER")));
    }

    @Test
    void adminAndManagerCanEdit() {
        assertTrue(advice.canEdit(auth("a", "ROLE_ADMIN")));
        assertTrue(advice.canEdit(auth("m", "ROLE_MANAGER")));
    }

    @Test
    void viewerCannotEdit() {
        assertFalse(advice.canEdit(auth("v", "ROLE_VIEWER")));
    }

    @Test
    void anonymousCannotEdit() {
        assertFalse(advice.canEdit(null));
        assertFalse(advice.isAdmin(null));
    }

    @Test
    void onlyAdminIsAdmin() {
        assertTrue(advice.isAdmin(auth("a", "ROLE_ADMIN")));
        assertFalse(advice.isAdmin(auth("m", "ROLE_MANAGER")));
        assertFalse(advice.isAdmin(auth("v", "ROLE_VIEWER")));
    }

    @Test
    void statusLabelsAreInRussian() {
        Map<String, String> labels = advice.statusLabels();

        assertEquals("На складе", labels.get("IN_STOCK"));
        assertEquals("Используется", labels.get("IN_USE"));
        assertEquals("В ремонте", labels.get("IN_REPAIR"));
        assertEquals("Списано", labels.get("WRITTEN_OFF"));
    }

    @Test
    void everyStatusHasLabel() {
        Map<String, String> labels = advice.statusLabels();

        for (EquipmentStatus s : EquipmentStatus.values()) {
            assertTrue(labels.containsKey(s.name()), "Нет подписи для " + s);
        }
    }
}