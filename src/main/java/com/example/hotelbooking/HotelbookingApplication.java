package com.example.hotelbooking;


import com.example.hotelbooking.Models.Role;
import com.example.hotelbooking.Models.User;
import com.example.hotelbooking.Respositories.RoleRepository;
import com.example.hotelbooking.Respositories.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@EnableScheduling
@Retryable
@SpringBootApplication
public class HotelbookingApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
		SpringApplication.run(HotelbookingApplication.class, args);
	}

//		@Bean
//		CommandLineRunner initAdminUser(UserRepository userRepository,
//		                                PasswordEncoder passwordEncoder,
//		                                RoleRepository roleRepository) {
//
//			return args -> {
//
//				// Step 1: Define role names
//				String[] roleNames = {"ADMIN", "USER", "MANAGER"};
//
//				// Step 2: Fetch or create roles
//				Set<Role> roles = new HashSet<>();
//
//				for (String roleName : roleNames) {
//					Role role = roleRepository.findByName(roleName)
//							.orElseGet(() -> roleRepository.save(
//									Role.builder().name(roleName).build()
//							));
//					roles.add(role);
//				}
//
//				// Step 3: Check if admin user exists
//				if (!userRepository.findByUsername("admin").isPresent()) {
//
//					User user = User.builder()
//							.username("admin")
//							.email("admin@gmail.com")
//							.password(passwordEncoder.encode("1234567890"))
//							.roles(roles) // ✅ assign roles here
//							.build();
//
//					userRepository.save(user);
//
//					System.out.println("Admin user created successfully.");
//
//				} else {
//					System.out.println("Admin user already exists. Skipping initialization.");
//				}
//			};
//		}

}
