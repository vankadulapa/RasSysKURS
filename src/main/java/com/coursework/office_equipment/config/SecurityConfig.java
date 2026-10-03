package com.coursework.office_equipment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String REMEMBER_ME_KEY = "IstNurEineSchmaleBrucke-DIeUferSindVernunftUntTrieb";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    AppUserDetailsService userDetailsService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/departments/**", "/employees/**", "/documents/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/equipment/new", "/equipment/*/edit",
                                 "/equipment/save", "/equipment/*/delete")
                    .hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/equipment", true)
                .permitAll())
            .rememberMe(remember -> remember
                .key(REMEMBER_ME_KEY)
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(14 * 24 * 60 * 60))
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }
}