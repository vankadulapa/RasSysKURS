package com.coursework.office_equipment.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("username")
    public String username(Authentication auth) {
        return auth == null ? null : auth.getName();
    }

    @ModelAttribute("canEdit")
    public boolean canEdit(Authentication auth) {
        return hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_MANAGER");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication auth) {
        return hasRole(auth, "ROLE_ADMIN");
    }

    @ModelAttribute("statusLabels")
    public Map<String, String> statusLabels() {
        return Map.of(
                "IN_STOCK", "На складе",
                "IN_USE", "Используется",
                "IN_REPAIR", "В ремонте",
                "WRITTEN_OFF", "Списано");
    }

    private static boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}