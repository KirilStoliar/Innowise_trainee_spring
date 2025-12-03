package com.stoliar.converter;

import com.stoliar.entity.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToOrderStatusConverter implements Converter<String, Order.OrderStatus> {
    
    @Override
    public Order.OrderStatus convert(String source) {
        try {
            return Order.OrderStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + source);
        }
    }
}