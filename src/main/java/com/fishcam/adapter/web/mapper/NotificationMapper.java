package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.NotificationResponse;
import com.fishcam.domain.notification.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification entity);

}
