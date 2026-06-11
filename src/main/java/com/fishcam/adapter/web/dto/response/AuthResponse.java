package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.UserScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String phone;
        private String firstName;
        private String lastName;
        private Role role;
        private UserScope scope;
        private Long poissonnerieId;
        private String poissonnerieName;
    }
}