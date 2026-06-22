package com.example.authservice.mapper;

import com.example.authservice.dto.event.EmailVerificationEvent;
import com.example.authservice.entity.Outbox;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailSendMapper {

    EmailVerificationEvent toDto(Outbox outbox);

}
