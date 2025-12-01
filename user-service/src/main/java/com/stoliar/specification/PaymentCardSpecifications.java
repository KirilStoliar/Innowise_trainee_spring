package com.stoliar.specification;

import com.stoliar.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecifications {

    public static Specification<PaymentCard> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}