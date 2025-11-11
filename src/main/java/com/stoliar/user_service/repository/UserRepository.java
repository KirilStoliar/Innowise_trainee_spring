package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // NATIVE SQL QUERIES
    @Modifying
    @Query(value = """
    INSERT INTO users (name, surname, birth_date, email, active, created_at)
    VALUES (:name, :surname, :birthDate, :email, true, CURRENT_TIMESTAMP)
    """, nativeQuery = true)
    int createUser(@Param("name") String name,
                    @Param("surname") String surname,
                    @Param("birthDate") java.time.LocalDate birthDate,
                    @Param("email") String email);

    default User updateUser(Long id, String name, String surname, LocalDate birthDate, String email) {
        return findById(id)
                .map(user -> {
                    user.setName(name);
                    user.setSurname(surname);
                    user.setBirthDate(birthDate);
                    user.setEmail(email);
                    user.setUpdatedAt(LocalDateTime.now());
                    return save(user);
                })
                .orElse(null);
    }

    @Modifying
    @Query(value = """
    UPDATE users SET active = :active, updated_at = CURRENT_TIMESTAMP
    WHERE id = :id RETURNING *
    """, nativeQuery = true)
    User updateUserStatus(@Param("id") Long id,
                          @Param("active") boolean active);

    // Подсчет активных карт пользователя
    @Query(value = "SELECT COUNT(*) FROM payment_cards WHERE user_id = :userId AND active = true",
            nativeQuery = true)
    int countActiveCardsByUserId(@Param("userId") Long userId);

    // NAMED METHODS
    User findUserById(Long id);
    boolean existsByEmail(String email);

    // SPECIFICATION METHODS

    Page<User> findAll(Specification<User> spec, Pageable pageable);
}