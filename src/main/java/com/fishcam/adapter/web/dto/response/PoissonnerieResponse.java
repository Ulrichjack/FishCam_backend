package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoissonnerieResponse {

    private Long id;
    private String address;
    private String name;
    private String phone;
    private BigDecimal loyer;
    private BigDecimal fondDeCaisseDefaut;
    private Boolean pretActif;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
