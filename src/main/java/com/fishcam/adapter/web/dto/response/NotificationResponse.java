package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.notification.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private TypeNotification type;
    private String message;
    private Boolean read;
    private PoissonnerieResponse poissonnerie;
    private LocalDateTime createdAt;

}
