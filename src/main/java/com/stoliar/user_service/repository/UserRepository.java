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

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // NATIVE SQL QUERIES
    @Modifying
    @Query(value = "INSERT INTO User (name, surname, birthDate, email, active, createdAt) " +
            "SELECT (:name, :surname, :birthDate, :email, :active, CURRENT_TIMESTAMP)", nativeQuery = true)
    void createUser(@Param("name") String name,
                    @Param("surname") String surname,
                    @Param("birthDate") java.time.LocalDate birthDate,
                    @Param("email") String email,
                    @Param("active") Boolean active);

    // Каскадные операции: активация/деактивация пользователя и всех его карт
    @Modifying
    @Query(value = "UPDATE users u SET active = true WHERE u.id = :userId; " +
            "UPDATE payment_cards pc SET active = true WHERE pc.user_id = :userId",
            nativeQuery = true)
    int activateUser(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE users u SET active = false WHERE u.id = :userId; " +
            "UPDATE payment_cards pc SET active = false WHERE pc.user_id = :userId",
            nativeQuery = true)
    int deactivateUser(@Param("userId") Long userId);

    // NAMED METHODS
    User findUserById(Long id);

    // JPQL QUERIES
    @Modifying
    @Query("UPDATE User u SET u.name = :name, u.surname = :surname, u.birthDate = :birthDate, u.email = :email WHERE u.id = :id")
    int updateUser(@Param("id") Long id,
                   @Param("name") String name,
                   @Param("surname") String surname,
                   @Param("birthDate") LocalDate birthDate,
                   @Param("email") String email);

    // SPECIFICATION METHODS

    Page<User> findAll(Specification<User> spec, Pageable pageable);
}