package com.coursework.office_equipment.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EquipmentRepository;

class ReportControllerTest {

    private final EquipmentRepository repo = mock(EquipmentRepository.class);
    private final ReportController controller = new ReportController(repo);

    private final Department accounting = TestData.department(1, "Бухгалтерия");
    private final Department it = TestData.department(2, "IT");
    private final Employee anna = TestData.employee(1, "Анна", accounting);
    private final Employee oleg = TestData.employee(2, "Олег", it);
    private final EquipmentType pc = TestData.type(1, "ПК");
    private final EquipmentType laptop = TestData.type(2, "Ноутбук");
    private final EquipmentType printer = TestData.type(3, "Принтер");

    private Equipment item(int id, EquipmentType type, String status, Employee owner) {
        Equipment e = TestData.equipment(id, "INV-" + id, type, status);
        e.setOwner(owner);
        return e;
    }

    private void givenFleet() {
        when(repo.findAll()).thenReturn(List.of(
                item(1, pc, "IN_USE", anna),
                item(2, pc, "IN_REPAIR", anna),
                item(3, laptop, "IN_USE", oleg),
                item(4, printer, "IN_STOCK", null)));
    }

    @SuppressWarnings("unchecked")
    private List<ReportController.Row> rows(Model model) {
        return (List<ReportController.Row>) model.asMap().get("rows");
    }

    @SuppressWarnings("unchecked")
    private List<Equipment> items(Model model) {
        return (List<Equipment>) model.asMap().get("items");
    }

    private ReportController.Row find(List<ReportController.Row> rows, String name) {
        return rows.stream().filter(r -> r.name.equals(name)).findFirst().orElseThrow();
    }

    @Test
    void departmentsReportGroupsByOwnerDepartment() {
        givenFleet();
        Model model = new ExtendedModelMap();

        String view = controller.reports("departments", 5, model);

        assertEquals("reports", view);
        List<ReportController.Row> rows = rows(model);
        ReportController.Row acc = find(rows, "Бухгалтерия");
        assertEquals(2, acc.total);
        assertEquals(1, acc.inUse);
        assertEquals(1, acc.inRepair);
        assertEquals(1, find(rows, "IT").total);
        assertEquals(1, find(rows, "Без владельца").inStock);
    }

    @Test
    void totalRowIsLastAndSumsEverything() {
        givenFleet();
        Model model = new ExtendedModelMap();

        controller.reports("departments", 5, model);

        List<ReportController.Row> rows = rows(model);
        ReportController.Row total = rows.get(rows.size() - 1);
        assertEquals("Итого", total.name);
        assertEquals(4, total.total);
        assertEquals(2, total.inUse);
        assertEquals(1, total.inStock);
        assertEquals(1, total.inRepair);
        assertEquals(0, total.writtenOff);
    }

    @Test
    void typesReportGroupsByType() {
        givenFleet();
        Model model = new ExtendedModelMap();

        controller.reports("types", 5, model);

        List<ReportController.Row> rows = rows(model);
        assertEquals(2, find(rows, "ПК").total);
        assertEquals(1, find(rows, "Ноутбук").total);
        assertEquals(1, find(rows, "Принтер").total);
        assertEquals("types", model.asMap().get("type"));
    }

    @Test
    void unknownTypeFallsBackToDepartments() {
        givenFleet();
        Model model = new ExtendedModelMap();

        controller.reports("zzz", 5, model);

        assertEquals("departments", model.asMap().get("type"));
        assertEquals("Техника по подразделениям", model.asMap().get("title"));
        assertNotNull(model.asMap().get("today"));
    }

    @Test
    void networkReportSortsByIpNumerically() {
        Equipment a = item(1, pc, "IN_USE", null);
        a.setIpAddress("192.168.1.11");
        Equipment b = item(2, pc, "IN_USE", null);
        b.setIpAddress("192.168.1.2");
        Equipment macOnly = item(3, pc, "IN_USE", null);
        macOnly.setMacAddress("AA:BB:CC:00:00:03");
        Equipment noNetwork = item(4, pc, "IN_USE", null);
        when(repo.findAll()).thenReturn(List.of(a, b, macOnly, noNetwork));
        Model model = new ExtendedModelMap();

        controller.reports("network", 5, model);

        assertEquals(List.of(2, 1, 3), items(model).stream().map(Equipment::getId).toList());
        assertTrue(((String) model.asMap().get("title")).contains("Сетевой реестр"));
    }

    @Test
    void unassignedReportContainsOnlyEquipmentWithoutOwner() {
        givenFleet();
        Model model = new ExtendedModelMap();

        controller.reports("unassigned", 5, model);

        assertEquals(List.of(4), items(model).stream().map(Equipment::getId).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void oldReportFiltersByAgeAndSkipsWrittenOff() {
        LocalDate today = LocalDate.now();
        Equipment veryOld = item(1, pc, "IN_USE", anna);
        veryOld.setPurchaseDate(today.minusYears(10));
        Equipment old = item(2, pc, "IN_STOCK", null);
        old.setPurchaseDate(today.minusYears(6));
        Equipment fresh = item(3, pc, "IN_USE", anna);
        fresh.setPurchaseDate(today.minusYears(2));
        Equipment writtenOff = item(4, pc, "WRITTEN_OFF", null);
        writtenOff.setPurchaseDate(today.minusYears(12));
        Equipment noDate = item(5, pc, "IN_USE", null);
        when(repo.findAll()).thenReturn(List.of(fresh, old, writtenOff, noDate, veryOld));
        Model model = new ExtendedModelMap();

        controller.reports("old", 5, model);

        assertEquals(List.of(1, 2), items(model).stream().map(Equipment::getId).toList());
        Map<Integer, Long> ages = (Map<Integer, Long>) model.asMap().get("ages");
        assertEquals(10L, ages.get(1));
        assertEquals(6L, ages.get(2));
        assertEquals("Техника старше 5 лет", model.asMap().get("title"));
    }

    @Test
    void negativeYearsAreTreatedAsZero() {
        Model model = new ExtendedModelMap();

        controller.reports("old", -3, model);

        assertEquals(0, model.asMap().get("years"));
        assertEquals("Техника старше 0 лет", model.asMap().get("title"));
    }
}