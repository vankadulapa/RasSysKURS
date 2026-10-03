package com.coursework.office_equipment.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.EquipmentTypeRepository;

class EquipmentControllerTest {

    private final EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
    private final EquipmentTypeRepository typeRepository = mock(EquipmentTypeRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final EquipmentController controller =
            new EquipmentController(equipmentRepository, typeRepository, employeeRepository);

    private final EquipmentType pc = TestData.type(1, "ПК");

    private Equipment saved() {
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void listCountsEquipmentByStatus() {
        when(equipmentRepository.findAll()).thenReturn(List.of(
                TestData.equipment(1, "INV-1", pc, "IN_USE"),
                TestData.equipment(2, "INV-2", pc, "IN_USE"),
                TestData.equipment(3, "INV-3", pc, "IN_STOCK"),
                TestData.equipment(4, "INV-4", pc, "IN_REPAIR")));
        Model model = new ExtendedModelMap();

        String view = controller.list("", "", model);

        assertEquals("equipment/list", view);
        assertEquals(4, model.asMap().get("statTotal"));
        assertEquals(2L, model.asMap().get("statInUse"));
        assertEquals(1L, model.asMap().get("statInStock"));
        assertEquals(1L, model.asMap().get("statInRepair"));
    }

    @Test
    void listTrimsQueryBeforeSearch() {
        Model model = new ExtendedModelMap();

        controller.list("  dell ", "IN_USE", model);

        verify(equipmentRepository).search("dell", "IN_USE");
        assertEquals("  dell ", model.asMap().get("q"));
        assertEquals("IN_USE", model.asMap().get("status"));
    }

    @Test
    void listPassesAllStatusOptions() {
        Model model = new ExtendedModelMap();

        controller.list("", "", model);

        assertEquals(List.of("IN_STOCK", "IN_USE", "IN_REPAIR", "WRITTEN_OFF"),
                model.asMap().get("statuses"));
    }

    @Test
    void createFormPrefillsDefaults() {
        Model model = new ExtendedModelMap();

        String view = controller.createForm(model);

        assertEquals("equipment/form", view);
        Equipment e = (Equipment) model.asMap().get("equipment");
        assertEquals("IN_STOCK", e.getStatus());
        assertNotNull(e.getType());
        assertNotNull(e.getOwner());
        assertNull(e.getId());
    }

    @Test
    void editFormLoadsEquipment() {
        Equipment e = TestData.equipment(5, "INV-5", pc, "IN_USE");
        e.setOwner(TestData.employee(1, "Анна", TestData.department(1, "IT")));
        when(equipmentRepository.findById(5)).thenReturn(Optional.of(e));
        Model model = new ExtendedModelMap();

        String view = controller.editForm(5, model);

        assertEquals("equipment/form", view);
        assertSame(e, model.asMap().get("equipment"));
    }

    @Test
    void editFormAddsEmptyOwnerWhenNone() {
        Equipment e = TestData.equipment(5, "INV-5", pc, "IN_STOCK");
        when(equipmentRepository.findById(5)).thenReturn(Optional.of(e));

        controller.editForm(5, new ExtendedModelMap());

        assertNotNull(e.getOwner());
        assertNull(e.getOwner().getId());
    }

    @Test
    void editFormFailsForUnknownId() {
        assertThrows(NoSuchElementException.class,
                () -> controller.editForm(99, new ExtendedModelMap()));
    }

    @Test
    void saveTurnsBlankFieldsIntoNull() {
        Equipment e = TestData.equipment(1, "INV-1", pc, "IN_USE");
        e.setIpAddress("   ");
        e.setMacAddress("");
        e.setSerialNumber(" SN-1 ");
        e.setLocation("  Каб. 101 ");
        e.setSpecs(null);

        String view = controller.save(e);

        Equipment s = saved();
        assertNull(s.getIpAddress());
        assertNull(s.getMacAddress());
        assertNull(s.getSpecs());
        assertEquals("SN-1", s.getSerialNumber());
        assertEquals("Каб. 101", s.getLocation());
        assertEquals("redirect:/equipment", view);
    }

    @Test
    void saveClearsOwnerWithoutId() {
        Equipment e = TestData.equipment(1, "INV-1", pc, "IN_STOCK");
        e.setOwner(new Employee());

        controller.save(e);

        assertNull(saved().getOwner());
    }

    @Test
    void saveKeepsSelectedOwner() {
        Employee owner = TestData.employee(3, "Олег", TestData.department(1, "IT"));
        Equipment e = TestData.equipment(1, "INV-1", pc, "IN_USE");
        e.setOwner(owner);

        controller.save(e);

        assertSame(owner, saved().getOwner());
    }

    @Test
    void deleteRemovesAndRedirects() {
        String view = controller.delete(7);

        verify(equipmentRepository).deleteById(7);
        assertEquals("redirect:/equipment", view);
    }
}