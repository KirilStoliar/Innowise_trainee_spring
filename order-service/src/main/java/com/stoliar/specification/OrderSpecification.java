package com.stoliar.specification;

import com.stoliar.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderSpecification {
    
    public Specification<Order> withFilters(
            LocalDateTime createdFrom, 
            LocalDateTime createdTo,
            List<Order.OrderStatus> statuses) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Базовое условие: только неудаленные записи
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            
            // Фильтр по диапазону дат создания
            if (createdFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), createdFrom));
            }
            
            if (createdTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), createdTo));
            }
            
            // Фильтр по статусам
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}