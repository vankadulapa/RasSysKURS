package com.coursework.office_equipment.model;

import com.coursework.office_equipment.TestData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void newUserIsActiveByDefault() {
        assertTrue(new AppUser().isActive());
    }

    @Test
    void equipmentKeepsAssignedValues() {
        Equipment e = TestData.equipment(1, "INV-1", TestData.type(1, "ПК"), "IN_USE");
        e.setIpAddress("192.168.1.10");
        e.setMacAddress("AA:BB:CC:DD:EE:FF");
        e.setPurchaseDate(LocalDate.of(2024, 1, 20));
        e.setLocation("Каб. 101");

        assertEquals("INV-1", e.getInventoryNumber());
        assertEquals("ПК", e.getType().getName());
        assertEquals("192.168.1.10", e.getIpAddress());
        assertEquals("AA:BB:CC:DD:EE:FF", e.getMacAddress());
        assertEquals(LocalDate.of(2024, 1, 20), e.getPurchaseDate());
        assertEquals("Каб. 101", e.getLocation());
    }

    @Test
    void equipmentStatusHasFourValues() {
        assertEquals(4, EquipmentStatus.values().length);
        assertEquals(EquipmentStatus.IN_USE, EquipmentStatus.valueOf("IN_USE"));
    }
}