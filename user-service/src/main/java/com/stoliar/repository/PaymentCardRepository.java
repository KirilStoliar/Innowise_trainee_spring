package com.stoliar.repository;

import com.stoliar.dto.PaymentCardDTO;
import com.stoliar.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    // NATIVE SQL QUERIES WITH RETURNING
    @Query(value = """
    INSERT INTO payment_cards (user_id, number, holder, expiration_date, active, created_at, updated_at)
    VALUES (:userId, :number, :holder, :expirationDate, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING *
    """, nativeQuery = true)
    PaymentCard createCard(@Param("userId") Long userId,
                           @Param("number") String number,
                           @Param("holder") String holder,
                           @Param("expirationDate") LocalDate expirationDate);

    // NAMED METHODS
    Optional<PaymentCard> findByNumber(String number);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") Long userId);

    Page<PaymentCardDTO> getAllCardsByUserId(Long userId, Pageable pageable);

    // SPECIFICATION METHODS
    Page<PaymentCard> findAll(Specification<PaymentCard> spec, Pageable pageable);
}