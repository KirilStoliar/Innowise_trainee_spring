package com.stoliar.mapper;

import com.stoliar.dto.PaymentRequest;
import com.stoliar.dto.PaymentResponse;
import com.stoliar.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Статус определяется внешним API
    @Mapping(target = "timestamp", ignore = true) // Устанавливается автоматически
    Payment toEntity(PaymentRequest paymentRequest);
    
    PaymentResponse toResponse(Payment payment);
}