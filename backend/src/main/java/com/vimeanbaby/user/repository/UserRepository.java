package com.vimeanbaby.user.repository;

import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<User> findByIdAndRole(Long id, Role role);

    long countByRole(Role role);

    long countByRoleAndActiveTrue(Role role);

    long countByRoleAndCreatedAtAfter(Role role, Instant after);

    List<User> findTop5ByRoleOrderByCreatedAtDesc(Role role);

    @Query("SELECT u.createdAt FROM User u WHERE u.role = :role AND u.createdAt >= :since")
    List<Instant> findSignupTimes(@Param("role") Role role, @Param("since") Instant since);
}
