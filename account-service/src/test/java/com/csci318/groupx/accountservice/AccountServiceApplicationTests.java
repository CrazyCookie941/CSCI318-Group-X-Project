package com.csci318.groupx.accountservice;

import com.csci318.groupx.accountservice.presentation.dto.AuthResponse;
import com.csci318.groupx.accountservice.presentation.dto.AccountProfileResponse;
import com.csci318.groupx.accountservice.presentation.dto.ChangePasswordRequest;
import com.csci318.groupx.accountservice.presentation.dto.LoginRequest;
import com.csci318.groupx.accountservice.presentation.dto.SignupRequest;
import com.csci318.groupx.accountservice.presentation.dto.UpdateAccountRequest;
import com.csci318.groupx.accountservice.infrastructure.repository.AccountRepository;
import com.csci318.groupx.accountservice.service.AccountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @Autowired
    private AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    @Test
    void profileUpdatesArePersistedToTheDatabase() {
        SignupRequest signup = new SignupRequest();
        signup.setUsername("persistent_profile_user");
        signup.setEmail("persistent_profile_user@example.com");
        signup.setPassword("test_password_123");

        AuthResponse created = accountService.signup(signup);
        assertTrue(created.isSuccess(), created.getMessage());

        UpdateAccountRequest update = new UpdateAccountRequest();
        update.setUsername("updated_profile_user");
        update.setEmail("updated_profile_user@example.com");
        update.setLocation("Sydney");

        AccountProfileResponse updated =
                accountService.updateAccountProfile(created.getAccountId(), update);
        assertTrue(updated.isSuccess(), updated.getMessage());

        entityManager.clear();
        var reloaded = accountRepository.findById(created.getAccountId()).orElseThrow();
        assertEquals("updated_profile_user", reloaded.getUsername());
        assertEquals("updated_profile_user@example.com", reloaded.getEmail());
        assertEquals("Sydney", reloaded.getLocation());
    }

    @Test
    void passwordChangeRequiresCurrentPasswordAndUpdatesLoginCredentials() {
        SignupRequest signup = new SignupRequest();
        signup.setUsername("password_change_user");
        signup.setEmail("password_change_user@example.com");
        signup.setPassword("original_password");
        AuthResponse created = accountService.signup(signup);
        assertTrue(created.isSuccess(), created.getMessage());

        ChangePasswordRequest rejectedChange = new ChangePasswordRequest();
        rejectedChange.setCurrentPassword("incorrect_password");
        rejectedChange.setNewPassword("replacement_password");
        assertFalse(accountService.changePassword(
                created.getAccountId(), rejectedChange).success());

        LoginRequest oldPasswordLogin = new LoginRequest();
        oldPasswordLogin.setIdentifier("password_change_user");
        oldPasswordLogin.setPassword("original_password");
        assertTrue(accountService.login(oldPasswordLogin).isSuccess());

        ChangePasswordRequest validChange = new ChangePasswordRequest();
        validChange.setCurrentPassword("original_password");
        validChange.setNewPassword("replacement_password");
        assertTrue(accountService.changePassword(
                created.getAccountId(), validChange).success());

        assertFalse(accountService.login(oldPasswordLogin).isSuccess());
        oldPasswordLogin.setPassword("replacement_password");
        assertTrue(accountService.login(oldPasswordLogin).isSuccess());
    }
}
