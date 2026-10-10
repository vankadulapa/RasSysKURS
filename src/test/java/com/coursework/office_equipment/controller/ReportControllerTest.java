package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private final EquipmentRepository repo = mock(EquipmentRepository.class);
    private final ReportController controller = new ReportController(repo);

    private final Department accounting = TestData.department(1, "Бухгалтерия");
    private final Employee anna = TestData.employee(1, "Анна", accounting);
    private final EquipmentType pc = TestData.type(1, "ПК");

    private Equipment item(int id, String status, Employee owner) {
        Equipment e = TestData.equipment(id, "INV-" + id, pc, status);
        e.setOwner(owner);
        return e;
    }

    @SuppressWarnings("unchecked")
    private List<Equipment> items(Model model) {
        return (List<Equipment>) model.asMap().get("items");
    }

    @Test
    @SuppressWarnings("unchecked")
    void departmentsReportGroupsByOwnerDepartmentAndAddsTotal() {
        when(repo.findAll()).thenReturn(List.of(
                item(1, "IN_USE", anna),
                item(2, "IN_REPAIR", anna),
                item(3, "IN_STOCK", null)));
        Model model = new ExtendedModelMap();

        controller.reports("departments", 5, model);

        List<ReportController.Row> rows = (List<ReportController.Row>) model.asMap().get("rows");
        ReportController.Row acc = rows.stream()
                .filter(r -> r.name.equals("Бухгалтерия")).findFirst().orElseThrow();
        assertEquals(2, acc.total);
        assertEquals(1, acc.inUse);
        assertEquals(1, acc.inRepair);
        ReportController.Row total = rows.get(rows.size() - 1);
        assertEquals("Итого", total.name);
        assertEquals(3, total.total);
    }

    @Test
    void networkReportSortsByIpNumerically() {
        Equipment a = item(1, "IN_USE", null);
        a.setIpAddress("192.168.1.11");
        Equipment b = item(2, "IN_USE", null);
        b.setIpAddress("192.168.1.2");
        Equipment noNetwork = item(3, "IN_USE", null);
        when(repo.findAll()).thenReturn(List.of(a, b, noNetwork));
        Model model = new ExtendedModelMap();

        controller.reports("network", 5, model);

        assertEquals(List.of(2, 1), items(model).stream().map(Equipment::getId).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void oldReportFiltersByAgeAndSkipsWrittenOff() {
        LocalDate today = LocalDate.now();
        Equipment veryOld = item(1, "IN_USE", anna);
        veryOld.setPurchaseDate(today.minusYears(10));
        Equipment fresh = item(2, "IN_USE", anna);
        fresh.setPurchaseDate(today.minusYears(2));
        Equipment writtenOff = item(3, "WRITTEN_OFF", null);
        writtenOff.setPurchaseDate(today.minusYears(12));
        when(repo.findAll()).thenReturn(List.of(fresh, writtenOff, veryOld));
        Model model = new ExtendedModelMap();

        controller.reports("old", 5, model);

        assertEquals(List.of(1), items(model).stream().map(Equipment::getId).toList());
        Map<Integer, Long> ages = (Map<Integer, Long>) model.asMap().get("ages");
        assertEquals(10L, ages.get(1));
    }
}