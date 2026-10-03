package com.coursework.office_equipment.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginControllerTest {

    @Test
    void returnsLoginView() {
        assertEquals("login", new LoginController().login());
    }
}