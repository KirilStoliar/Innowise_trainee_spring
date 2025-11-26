package com.stoliar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "payment_cards", indexes = {
    @Index(name = "idx_card_number", columnList = "number"),
    @Index(name = "idx_card_expiration_date", columnList = "expiration_date")
    },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_cards_number", columnNames = "number")
        })
@Getter
@Setter
@ToString
public class PaymentCard extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_card_user"))
    private User user;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 19, message = "Card number must be between 16 and 19 characters")
    @Column(name = "number", nullable = false, length = 19)
    private String number;

    @NotBlank(message = "Card holder is required")
    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}