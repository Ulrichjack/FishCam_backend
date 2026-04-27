package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurResponse {

    private Long id;
    private String nom;
    private String ville;
    private String telephone;
    private Boolean actif = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
