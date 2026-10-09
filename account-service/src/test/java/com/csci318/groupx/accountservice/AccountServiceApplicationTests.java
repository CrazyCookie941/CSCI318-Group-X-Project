package com.csci318.groupx.accountservice;

import com.csci318.groupx.accountservice.presentation.dto.AuthResponse;
import com.csci318.groupx.accountservice.presentation.dto.LoginRequest;
import com.csci318.groupx.accountservice.presentation.dto.SignupRequest;
import com.csci318.groupx.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:account_service_test;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.h2.console.enabled=false"
        }
)
class AccountServiceApplicationTests {

    @Autowired
    private AccountService accountService;

    @Test
    void signupAndLoginPreserveManagerRole() {
        SignupRequest signup = new SignupRequest();
        signup.setUsername("migration_test_manager");
        signup.setEmail("migration_test_manager@example.com");
        signup.setPassword("migration_test_password_123");
        signup.setRole("MANAGER");

        AuthResponse registered = accountService.signup(signup);
        assertTrue(registered.isSuccess(), registered.getMessage());
        assertNotNull(registered.getAccountId());
        assertEquals("MANAGER", registered.getRole());

        LoginRequest login = new LoginRequest();
        login.setIdentifier("migration_test_manager@example.com");
        login.setPassword("migration_test_password_123");
        AuthResponse authenticated = accountService.login(login);
        assertTrue(authenticated.isSuccess(), authenticated.getMessage());
        assertEquals(registered.getAccountId(), authenticated.getAccountId());
        assertEquals("MANAGER", authenticated.getRole());

        login.setPassword("wrong_password");
        assertFalse(accountService.login(login).isSuccess());
    }
}
