package com.csci318.groupx.accountservice.infrastructure.repository;

import com.csci318.groupx.accountservice.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}