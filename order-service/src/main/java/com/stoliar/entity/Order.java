package com.stoliar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Data
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email; // Для связи с User Service

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private Boolean deleted = false;

    @OneToMany(
            mappedBy = "order",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH},
            orphanRemoval = false,  // отключаем, так как будем удалять через репозиторий
            fetch = FetchType.LAZY
    )
    private List<OrderItem> orderItems;

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}