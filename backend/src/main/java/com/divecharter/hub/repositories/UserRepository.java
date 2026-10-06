package com.divecharter.hub.repositories;

import com.divecharter.hub.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import com.divecharter.hub.models.enums.Role;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndCertVerifiedFalse(Role role, Pageable pageable);
}
