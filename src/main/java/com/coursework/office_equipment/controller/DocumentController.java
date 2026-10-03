package com.coursework.office_equipment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.UserRepository;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public DocumentController(EquipmentRepository equipmentRepository,
                              EmployeeRepository employeeRepository,
                              UserRepository userRepository) {
        this.equipmentRepository = equipmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String page(@RequestParam(defaultValue = "waybill") String type,
                       Model model, Authentication auth) {
        Integer meEmployeeId = userRepository.findByUsername(auth.getName())
                .map(AppUser::getEmployee)
                .map(Employee::getId)
                .orElse(null);
        model.addAttribute("type", "writeoff".equals(type) ? "writeoff" : "waybill");
        model.addAttribute("items", equipmentRepository.findAll().stream()
                .filter(e -> !"WRITTEN_OFF".equals(e.getStatus()))
                .sorted(Comparator.comparing(Equipment::getInventoryNumber))
                .toList());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("meEmployeeId", meEmployeeId);
        model.addAttribute("today", LocalDate.now().toString());
        return "documents";
    }

    @PostMapping("/print")
    public String print(@RequestParam String type,
                        @RequestParam(required = false) List<Integer> ids,
                        @RequestParam(defaultValue = "") String number,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(defaultValue = "") String org,
                        @RequestParam(defaultValue = "") String reason,
                        @RequestParam(required = false) Integer fromId,
                        @RequestParam(required = false) Integer toId,
                        @RequestParam(required = false) Integer chairmanId,
                        @RequestParam(required = false) Integer member1Id,
                        @RequestParam(required = false) Integer member2Id,
                        @RequestParam(defaultValue = "false") boolean apply,
                        Model model, RedirectAttributes ra) {
        String t = "writeoff".equals(type) ? "writeoff" : "waybill";
        if (ids == null || ids.isEmpty()) {
            ra.addFlashAttribute("error", "Выберите хотя бы одну единицу техники");
            return "redirect:/documents?type=" + t;
        }

        List<Equipment> items = equipmentRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(Equipment::getInventoryNumber))
                .toList();
        LocalDate docDate = date != null ? date : LocalDate.now();
        String prefix = "waybill".equals(t) ? "НП-" : "АС-";
        String docNumber = number.isBlank()
                ? prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                : number.trim();

        final Employee from = employee(fromId);
        final Employee to = employee(toId);

        boolean applied = false;
        if (apply) {
            if ("waybill".equals(t)) {
                if (to != null) {
                    items.forEach(e -> {
                        e.setOwner(to);
                        if ("IN_STOCK".equals(e.getStatus())) {
                            e.setStatus("IN_USE");
                        }
                    });
                    equipmentRepository.saveAll(items);
                    applied = true;
                }
            } else {
                items.forEach(e -> e.setStatus("WRITTEN_OFF"));
                equipmentRepository.saveAll(items);
                applied = true;
            }
        }

        model.addAttribute("type", t);
        model.addAttribute("docTitle", "waybill".equals(t) ? "Накладная" : "Акт на списание");
        model.addAttribute("number", docNumber);
        model.addAttribute("date", docDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        model.addAttribute("org", org.isBlank() ? "______________________" : org.trim());
        model.addAttribute("reason", reason.isBlank() ? "______________________" : reason.trim());
        model.addAttribute("items", items);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("chairman", employee(chairmanId));
        model.addAttribute("member1", employee(member1Id));
        model.addAttribute("member2", employee(member2Id));
        model.addAttribute("applied", applied);
        return "document-print";
    }

    private Employee employee(Integer id) {
        return id == null ? null : employeeRepository.findById(id).orElse(null);
    }
}