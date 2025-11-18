package com.iuh.printshop.printshop_be.config;

import com.iuh.printshop.printshop_be.entity.Role;
import com.iuh.printshop.printshop_be.entity.User;
import com.iuh.printshop.printshop_be.repository.RoleRepository;
import com.iuh.printshop.printshop_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        Role adminRole;
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
        } else {
            adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        }
        
        if (!roleRepository.existsByName("ROLE_CUSTOMER")) {
            roleRepository.save(new Role("ROLE_CUSTOMER"));
        }

        // Create default admin user if it doesn't exist
        String adminEmail = "admin@printshop.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode("admin123")); // Default password
            adminUser.setFullName("Administrator");
            adminUser.setPhone("0123456789");
            adminUser.setDefaultAddress("Admin Address");
            adminUser.setIsActive(true); // Admin account is active by default

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            System.out.println("=========================================");
            System.out.println("Default Admin User Created:");
            System.out.println("Email: " + adminEmail);
            System.out.println("Password: admin123");
            System.out.println("=========================================");
        }
    }
}
