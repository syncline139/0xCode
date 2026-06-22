package com.example.authservice.mapper;

import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequest dto);
}
