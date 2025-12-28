package com.stoliar.repository;

import com.stoliar.entity.Payment;
import com.stoliar.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 1. Поиск по user_id
    List<Payment> findByUserId(Long userId);
    
    // 2. Поиск по order_id
    List<Payment> findByOrderId(Long orderId);
    
    // 3. Поиск по status
    List<Payment> findByStatus(PaymentStatus status);
    
    // 4. Поиск по user_id ИЛИ order_id ИЛИ status
    // (комбинированный поиск с одним параметром)
    @Query("SELECT p FROM Payment p WHERE " +
           "(:userId IS NULL OR p.userId = :userId) AND " +
           "(:orderId IS NULL OR p.orderId = :orderId) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Payment> findPaymentsByCriteria(
            @Param("userId") Long userId,
            @Param("orderId") Long orderId,
            @Param("status") PaymentStatus status);

    // 5. Общая сумма платежей для конкретного пользователя за период
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p " +
           "WHERE p.userId = :userId " +
           "AND p.timestamp BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSumByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // 6. Общая сумма платежей для всех пользователей за период
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p " +
           "WHERE p.timestamp BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSumByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}