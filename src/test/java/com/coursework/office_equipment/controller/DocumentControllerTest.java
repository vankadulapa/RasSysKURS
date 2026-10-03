package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    private final EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DocumentController controller =
            new DocumentController(equipmentRepository, employeeRepository, userRepository);

    private final EquipmentType pc = TestData.type(1, "ПК");

    private Authentication auth(String name) {
        return new UsernamePasswordAuthenticationToken(name, "pw",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    private Equipment item(int id, String inventoryNumber, String status) {
        return TestData.equipment(id, inventoryNumber, pc, status);
    }

    private String printDocument(String type, List<Integer> ids, boolean apply, Integer toId,
                                 Model model, RedirectAttributes ra) {
        return controller.print(type, ids, "", null, "", "", null, toId, null, null, null,
                apply, model, ra);
    }

    @SuppressWarnings("unchecked")
    private List<Equipment> items(Model model) {
        return (List<Equipment>) model.asMap().get("items");
    }

    @Test
    void pageHidesWrittenOffEquipmentAndSortsByInventoryNumber() {
        when(equipmentRepository.findAll()).thenReturn(List.of(
                item(2, "INV-2", "IN_USE"),
                item(3, "INV-3", "WRITTEN_OFF"),
                item(1, "INV-1", "IN_STOCK")));
        Model model = new ExtendedModelMap();

        String view = controller.page("waybill", model, auth("manager"));

        assertEquals("documents", view);
        assertEquals(List.of("INV-1", "INV-2"),
                items(model).stream().map(Equipment::getInventoryNumber).toList());
    }

    @Test
    void pageDefaultsUnknownTypeToWaybill() {
        Model unknown = new ExtendedModelMap();
        Model writeoff = new ExtendedModelMap();

        controller.page("abc", unknown, auth("manager"));
        controller.page("writeoff", writeoff, auth("manager"));

        assertEquals("waybill", unknown.asMap().get("type"));
        assertEquals("writeoff", writeoff.asMap().get("type"));
    }

    @Test
    void pageSuppliesEmployeeOfCurrentUser() {
        AppUser u = TestData.user(1, "manager", TestData.role(2, "MANAGER"));
        u.setEmployee(TestData.employee(5, "Анна", TestData.department(1, "IT")));
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(u));
        Model model = new ExtendedModelMap();

        controller.page("waybill", model, auth("manager"));

        assertEquals(5, model.asMap().get("meEmployeeId"));
    }

    @Test
    void pageWithoutLinkedEmployeeHasNoDefaultSender() {
        AppUser u = TestData.user(1, "manager", TestData.role(2, "MANAGER"));
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(u));
        Model model = new ExtendedModelMap();

        controller.page("waybill", model, auth("manager"));

        assertNull(model.asMap().get("meEmployeeId"));
    }

    @Test
    void printWithoutIdsRedirectsBackWithError() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = printDocument("waybill", null, false, null, new ExtendedModelMap(), ra);

        assertEquals("redirect:/documents?type=waybill", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
        verifyNoInteractions(equipmentRepository);
    }

    @Test
    void printWithEmptyIdsRedirectsBackForWriteOff() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = printDocument("writeoff", List.of(), false, null, new ExtendedModelMap(), ra);

        assertEquals("redirect:/documents?type=writeoff", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
    }

    @Test
    void waybillWithApplyAssignsOwnerAndMovesStockItemsToInUse() {
        Equipment stock = item(1, "INV-1", "IN_STOCK");
        Equipment repair = item(2, "INV-2", "IN_REPAIR");
        Employee receiver = TestData.employee(10, "Пётр", TestData.department(1, "IT"));
        when(equipmentRepository.findAllById(List.of(1, 2))).thenReturn(List.of(stock, repair));
        when(employeeRepository.findById(10)).thenReturn(Optional.of(receiver));
        Model model = new ExtendedModelMap();

        String view = printDocument("waybill", List.of(1, 2), true, 10, model,
                new RedirectAttributesModelMap());

        assertEquals("document-print", view);
        assertSame(receiver, stock.getOwner());
        assertEquals("IN_USE", stock.getStatus());
        assertSame(receiver, repair.getOwner());
        assertEquals("IN_REPAIR", repair.getStatus());
        verify(equipmentRepository).saveAll(any());
        assertEquals(Boolean.TRUE, model.asMap().get("applied"));
    }

    @Test
    void waybillWithoutApplyDoesNotChangeData() {
        Equipment stock = item(1, "INV-1", "IN_STOCK");
        Employee receiver = TestData.employee(10, "Пётр", TestData.department(1, "IT"));
        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(stock));
        when(employeeRepository.findById(10)).thenReturn(Optional.of(receiver));
        Model model = new ExtendedModelMap();

        printDocument("waybill", List.of(1), false, 10, model, new RedirectAttributesModelMap());

        assertNull(stock.getOwner());
        assertEquals("IN_STOCK", stock.getStatus());
        verify(equipmentRepository, never()).saveAll(any());
        assertEquals(Boolean.FALSE, model.asMap().get("applied"));
    }

    @Test
    void waybillApplyWithoutRecipientDoesNothing() {
        Equipment stock = item(1, "INV-1", "IN_STOCK");
        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(stock));
        Model model = new ExtendedModelMap();

        printDocument("waybill", List.of(1), true, null, model, new RedirectAttributesModelMap());

        verify(equipmentRepository, never()).saveAll(any());
        assertEquals(Boolean.FALSE, model.asMap().get("applied"));
    }

    @Test
    void writeOffWithApplyMarksEquipmentWrittenOff() {
        Equipment a = item(1, "INV-1", "IN_REPAIR");
        Equipment b = item(2, "INV-2", "IN_USE");
        when(equipmentRepository.findAllById(List.of(1, 2))).thenReturn(List.of(a, b));
        Model model = new ExtendedModelMap();

        printDocument("writeoff", List.of(1, 2), true, null, model, new RedirectAttributesModelMap());

        assertEquals("WRITTEN_OFF", a.getStatus());
        assertEquals("WRITTEN_OFF", b.getStatus());
        verify(equipmentRepository).saveAll(any());
        assertEquals(Boolean.TRUE, model.asMap().get("applied"));
    }

    @Test
    void writeOffWithoutApplyKeepsStatus() {
        Equipment a = item(1, "INV-1", "IN_REPAIR");
        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(a));

        printDocument("writeoff", List.of(1), false, null, new ExtendedModelMap(),
                new RedirectAttributesModelMap());

        assertEquals("IN_REPAIR", a.getStatus());
        verify(equipmentRepository, never()).saveAll(any());
    }

    @Test
    void autoNumberUsesDocumentSpecificPrefix() {
        Model waybill = new ExtendedModelMap();
        Model writeoff = new ExtendedModelMap();

        printDocument("waybill", List.of(1), false, null, waybill, new RedirectAttributesModelMap());
        printDocument("writeoff", List.of(1), false, null, writeoff, new RedirectAttributesModelMap());

        assertTrue(((String) waybill.asMap().get("number")).startsWith("НП-"));
        assertTrue(((String) writeoff.asMap().get("number")).startsWith("АС-"));
    }

    @Test
    void customNumberIsTrimmed() {
        Model model = new ExtendedModelMap();

        controller.print("waybill", List.of(1), " 15 ", null, "", "", null, null, null, null, null,
                false, model, new RedirectAttributesModelMap());

        assertEquals("15", model.asMap().get("number"));
    }

    @Test
    void blankOrganizationAndReasonGetPlaceholders() {
        Model model = new ExtendedModelMap();

        printDocument("writeoff", List.of(1), false, null, model, new RedirectAttributesModelMap());

        assertTrue(((String) model.asMap().get("org")).startsWith("____"));
        assertTrue(((String) model.asMap().get("reason")).startsWith("____"));
    }

    @Test
    void organizationAndReasonAreTrimmed() {
        Model model = new ExtendedModelMap();

        controller.print("writeoff", List.of(1), "", null, "  ООО Ромашка ", " износ ", null, null,
                null, null, null, false, model, new RedirectAttributesModelMap());

        assertEquals("ООО Ромашка", model.asMap().get("org"));
        assertEquals("износ", model.asMap().get("reason"));
    }

    @Test
    void dateDefaultsToToday() {
        Model model = new ExtendedModelMap();

        printDocument("waybill", List.of(1), false, null, model, new RedirectAttributesModelMap());

        String expected = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        assertEquals(expected, model.asMap().get("date"));
    }

    @Test
    void explicitDateIsFormatted() {
        Model model = new ExtendedModelMap();

        controller.print("waybill", List.of(1), "", LocalDate.of(2026, 3, 5), "", "", null, null,
                null, null, null, false, model, new RedirectAttributesModelMap());

        assertEquals("05.03.2026", model.asMap().get("date"));
    }

    @Test
    void documentItemsAreSortedByInventoryNumber() {
        when(equipmentRepository.findAllById(List.of(2, 1))).thenReturn(List.of(
                item(2, "INV-2", "IN_USE"), item(1, "INV-1", "IN_USE")));
        Model model = new ExtendedModelMap();

        printDocument("waybill", List.of(2, 1), false, null, model, new RedirectAttributesModelMap());

        assertEquals(List.of("INV-1", "INV-2"),
                items(model).stream().map(Equipment::getInventoryNumber).toList());
    }

    @Test
    void documentTitleDependsOnType() {
        Model waybill = new ExtendedModelMap();
        Model writeoff = new ExtendedModelMap();

        printDocument("waybill", List.of(1), false, null, waybill, new RedirectAttributesModelMap());
        printDocument("writeoff", List.of(1), false, null, writeoff, new RedirectAttributesModelMap());

        assertEquals("Накладная", waybill.asMap().get("docTitle"));
        assertEquals("Акт на списание", writeoff.asMap().get("docTitle"));
    }

    @Test
    void unknownDocumentTypeIsTreatedAsWaybill() {
        Model model = new ExtendedModelMap();

        printDocument("xxx", List.of(1), false, null, model, new RedirectAttributesModelMap());

        assertEquals("waybill", model.asMap().get("type"));
    }
}