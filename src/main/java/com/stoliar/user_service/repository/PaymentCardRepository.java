package com.stoliar.user_service.repository;

import com.stoliar.user_service.entity.PaymentCard;
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
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    // NATIVE SQL QUERIES
    @Modifying
    @Query(value = "INSERT INTO payment_cards (user_id, number, holder, expiration_date, active, created_at)" +
    "SELECT :userId, :number, :holder, :expirationDate, true, CURRENT_TIMESTAMP", nativeQuery = true)
    PaymentCard createCard(@Param("userId") Long userId,
                   @Param("number") String number,
                   @Param("holder") String holder,
                   @Param("expirationDate") LocalDate expirationDate);

    @Modifying
    @Query(value = "INSERT INTO PaymentCard (active) SELECT (:active)", nativeQuery = true)
    boolean updateCardStatus(@Param("active") boolean active);

    // NAMED METHODS
    PaymentCard findByNumber(String number);

    // JPQL QUERIES
    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PaymentCard pc SET pc.number = :number, pc.holder = :holder, " +
            "pc.expirationDate = :expirationDate, pc.active = :active WHERE pc.id = :id")
    PaymentCard updatePaymentCard(@Param("id") Long id,
                          @Param("number") String number,
                          @Param("holder") String holder,
                          @Param("expirationDate") LocalDate expirationDate);

    // SPECIFICATION METHODS
    Page<PaymentCard> findAll(Specification<PaymentCard> spec, Pageable pageable);
}