package com.coursework.office_equipment.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.RoleRepository;
import com.coursework.office_equipment.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder encoder;

    public UserAdminController(UserRepository userRepository, RoleRepository roleRepository,
                               EmployeeRepository employeeRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.encoder = encoder;
    }

    @GetMapping
    public String list(Model model, Authentication auth) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("currentUser", auth.getName());
        return "admin/users";
    }

    @PostMapping("/create")
    public String create(@RequestParam String username, @RequestParam String password,
                         @RequestParam Integer roleId,
                         @RequestParam(required = false) Integer employeeId,
                         RedirectAttributes ra) {
        String login = username.trim();
        if (login.isEmpty() || password.length() < 6) {
            ra.addFlashAttribute("error", "Логин не должен быть пустым, пароль — не короче 6 символов");
            return "redirect:/admin/users";
        }
        if (userRepository.findByUsername(login).isPresent()) {
            ra.addFlashAttribute("error", "Пользователь с таким логином уже существует");
            return "redirect:/admin/users";
        }
        Employee emp = null;
        if (employeeId != null) {
            if (userRepository.findByEmployeeId(employeeId).isPresent()) {
                ra.addFlashAttribute("error", "Этот сотрудник уже привязан к другой учётной записи");
                return "redirect:/admin/users";
            }
            emp = employeeRepository.findById(employeeId).orElseThrow();
        }
        AppUser u = new AppUser();
        u.setUsername(login);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(roleRepository.findById(roleId).orElseThrow());
        u.setEmployee(emp);
        u.setActive(true);
        userRepository.save(u);
        ra.addFlashAttribute("message", "Пользователь «" + login + "» создан");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/employee")
    public String changeEmployee(@PathVariable Integer id,
                                 @RequestParam(required = false) Integer employeeId,
                                 RedirectAttributes ra) {
        AppUser u = userRepository.findById(id).orElseThrow();
        if (employeeId == null) {
            u.setEmployee(null);
            userRepository.save(u);
            ra.addFlashAttribute("message", "Привязка к сотруднику снята");
            return "redirect:/admin/users";
        }
        var other = userRepository.findByEmployeeId(employeeId);
        if (other.isPresent() && !other.get().getId().equals(id)) {
            ra.addFlashAttribute("error", "Этот сотрудник уже привязан к пользователю «"
                    + other.get().getUsername() + "»");
        } else {
            u.setEmployee(employeeRepository.findById(employeeId).orElseThrow());
            userRepository.save(u);
            ra.addFlashAttribute("message", "Сотрудник привязан");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Integer id, @RequestParam Integer roleId,
                             Authentication auth, RedirectAttributes ra) {
        AppUser u = userRepository.findById(id).orElseThrow();
        if (u.getUsername().equals(auth.getName())) {
            ra.addFlashAttribute("error", "Нельзя менять роль самому себе");
        } else {
            u.setRole(roleRepository.findById(roleId).orElseThrow());
            userRepository.save(u);
            ra.addFlashAttribute("message", "Роль изменена");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        AppUser u = userRepository.findById(id).orElseThrow();
        if (u.getUsername().equals(auth.getName())) {
            ra.addFlashAttribute("error", "Нельзя отключить самого себя");
        } else {
            u.setActive(!u.isActive());
            userRepository.save(u);
            ra.addFlashAttribute("message", u.isActive() ? "Пользователь включён" : "Пользователь отключён");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        AppUser u = userRepository.findById(id).orElseThrow();
        if (u.getUsername().equals(auth.getName())) {
            ra.addFlashAttribute("error", "Нельзя удалить самого себя");
        } else {
            userRepository.delete(u);
            ra.addFlashAttribute("message", "Пользователь удалён");
        }
        return "redirect:/admin/users";
    }
}