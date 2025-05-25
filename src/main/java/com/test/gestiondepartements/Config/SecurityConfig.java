package com.test.gestiondepartements.Config;

import com.test.gestiondepartements.Security.Service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    @Lazy
    private UserDetailsServiceImpl userDetailsService;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/layout/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/", "/index", "/login", "/logout", "/error", "/403").permitAll()
                        .requestMatchers("/admin/**","/admin/dashboard","/admin/departments/**", "/admin/historique").hasAuthority("ADMIN")
                        .requestMatchers("/chef/**").hasAuthority("DEPARTMENT_HEAD")
                        .requestMatchers("/enseignant/**","/enseignant/profile").hasAuthority("ENSEIGNANT")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
}