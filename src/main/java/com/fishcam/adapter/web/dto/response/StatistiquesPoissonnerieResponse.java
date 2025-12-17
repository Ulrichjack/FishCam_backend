package com.fishcam.adapter.web.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesPoissonnerieResponse {
    private PoissonnerieResponse poissonnerieResponse;
    private  Integer nombreClients;
    private StatistiquesEpargneResponse epargnes;
}
