package com.stoliar.user_service.mapper;

import com.stoliar.user_service.dto.UserCreateDTO;
import com.stoliar.user_service.dto.UserDTO;
import com.stoliar.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);
    
    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserDTO userDTO);
    
    List<UserDTO> toDTOList(List<User> users);
}