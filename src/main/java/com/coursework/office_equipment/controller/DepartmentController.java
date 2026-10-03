package com.coursework.office_equipment.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.repository.DepartmentRepository;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("items", departmentRepository.search(q.trim()));
        model.addAttribute("q", q);
        return "departments";
    }

    @PostMapping("/add")
    public String add(@RequestParam String name, @RequestParam(defaultValue = "") String room,
                      RedirectAttributes ra) {
        if (name.isBlank()) {
            ra.addFlashAttribute("error", "Название подразделения не должно быть пустым");
        } else {
            Department d = new Department();
            d.setName(name.trim());
            d.setRoom(room.isBlank() ? null : room.trim());
            departmentRepository.save(d);
            ra.addFlashAttribute("message", "Подразделение добавлено");
        }
        return "redirect:/departments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            departmentRepository.deleteById(id);
            ra.addFlashAttribute("message", "Подразделение удалено");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Нельзя удалить: в подразделении есть сотрудники");
        }
        return "redirect:/departments";
    }
}