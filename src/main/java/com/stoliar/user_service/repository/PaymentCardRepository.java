package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.exception.CustomExceptions;
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
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    // NATIVE SQL QUERIES
    @Modifying
    @Query(value = """
    INSERT INTO payment_cards (user_id, number, holder, expiration_date, active, created_at)
    VALUES (:userId, :number, :holder, :expirationDate, true, CURRENT_TIMESTAMP)
    """, nativeQuery = true)
    int createCard(@Param("userId") Long userId,
                           @Param("number") String number,
                           @Param("holder") String holder,
                           @Param("expirationDate") LocalDate expirationDate);

    default PaymentCard updatePaymentCard(Long id, String number, String holder, LocalDate expirationDate) {
        return findById(id)
                .map(card -> {
                    card.setNumber(number);
                    card.setHolder(holder);
                    card.setExpirationDate(expirationDate);
                    return save(card);
                })
                .orElse(null);
    }

    default PaymentCard updateCardStatus(Long id, boolean active) {
        return findById(id).map(card -> {
            card.setActive(active);
            card.setUpdatedAt(LocalDateTime.now());
            return save(card);
        }).orElseThrow(() -> new CustomExceptions.EntityNotFoundException("Card not found with id: " + id));
    }

    // NAMED METHODS
    Optional<PaymentCard> findByNumber(String number);

    // JPQL QUERIES
    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") Long userId);

    // SPECIFICATION METHODS
    Page<PaymentCard> findAll(Specification<PaymentCard> spec, Pageable pageable);
}