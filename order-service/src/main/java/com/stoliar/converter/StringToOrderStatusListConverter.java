package com.stoliar.converter;

import com.stoliar.entity.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StringToOrderStatusListConverter implements Converter<String, List<Order.OrderStatus>> {
    
    @Override
    public List<Order.OrderStatus> convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return List.of();
        }
        
        return Arrays.stream(source.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Order.OrderStatus::valueOf)
                .collect(Collectors.toList());
    }
}