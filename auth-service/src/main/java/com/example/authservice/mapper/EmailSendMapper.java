package com.example.authservice.mapper;

import com.example.authservice.dto.event.EmailSendEvent;
import com.example.authservice.entity.Outbox;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailSendMapper {

    EmailSendEvent toDto(Outbox outbox);

}
