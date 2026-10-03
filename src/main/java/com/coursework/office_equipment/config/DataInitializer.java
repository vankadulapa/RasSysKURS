package com.coursework.office_equipment.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Role;
import com.coursework.office_equipment.repository.RoleRepository;
import com.coursework.office_equipment.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createUser("admin", "admin123", "ADMIN");
        createUser("manager", "manager123", "MANAGER");
        createUser("viewer", "viewer123", "VIEWER");
    }

    private void createUser(String username, String password, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setActive(true);
        userRepository.save(u);
    }
}