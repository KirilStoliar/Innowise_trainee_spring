package com.stoliar.user_service.mapper;

import com.stoliar.user_service.dto.PaymentCardCreateDTO;
import com.stoliar.user_service.dto.PaymentCardDTO;
import com.stoliar.user_service.entity.PaymentCard;
import com.stoliar.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    
    PaymentCardMapper INSTANCE = Mappers.getMapper(PaymentCardMapper.class);
    
    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDTO(PaymentCard paymentCard);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentCard toEntity(PaymentCardCreateDTO paymentCardCreateDTO, Long userId);
    
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);
    
    List<PaymentCardDTO> toDTOList(List<PaymentCard> paymentCards);
    
    @Named("userIdToUser")
    default User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}