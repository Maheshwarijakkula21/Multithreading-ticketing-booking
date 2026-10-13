
package com.ticketbooking.config;

import com.ticketbooking.model.User;
import com.ticketbooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DemoUsersConfig {

    @Bean
    CommandLineRunner createDemoUsers(
            UserRepository users,
            PasswordEncoder encoder) {

        return args -> {
            resetCustomerPassword(users, encoder);
            System.out.println("Demo users ready!");
        };
    }

    @Transactional
    public void resetCustomerPassword(
            UserRepository users,
            PasswordEncoder encoder) {

        User customer = users.findByUsername("customer")
            .orElseGet(() -> new User(
                "Demo Customer",
                "customer",
                encoder.encode("Customer@123"),
                "CUSTOMER"
            ));

        customer.setPasswordHash(encoder.encode("Customer@123"));
        users.save(customer);
    }
}
