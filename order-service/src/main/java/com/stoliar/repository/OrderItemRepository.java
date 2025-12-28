package com.stoliar.repository;

import com.stoliar.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Базовый запрос - без загрузки Item
    @Query("SELECT oi FROM OrderItem oi WHERE oi.id = :id")
    Optional<OrderItem> findByIdAndNotDeleted(@Param("id") Long id);

    // Запрос с загрузкой Item
    @Query("""
        SELECT oi FROM OrderItem oi 
        JOIN FETCH oi.item i 
        WHERE oi.id = :id
    """)
    Optional<OrderItem> findByIdWithItem(@Param("id") Long id);

    // Загрузка всех OrderItem для заказа с их товарами
    @Query("""
        SELECT oi FROM OrderItem oi 
        JOIN FETCH oi.item i 
        WHERE oi.order.id = :orderId
    """)
    List<OrderItem> findByOrderIdWithItems(@Param("orderId") Long orderId);

    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}