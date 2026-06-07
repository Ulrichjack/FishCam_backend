package com.fishcam.adapter.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String quartier;
    private PoissonnerieResponse poissonnerie;
    private Boolean active;
    private LocalDateTime createdAt;
    private BigDecimal soldeCompteCourant;
}
