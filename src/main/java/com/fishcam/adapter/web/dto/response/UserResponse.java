package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.UserScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private UserScope scope;
    private PoissonnerieResponse defaultPoissonnerie;
    private Boolean active;
    private LocalDateTime createdAt;

}
