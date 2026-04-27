package com.fishcam.adapter.web.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesPoissonnerieResponse {
    private PoissonnerieResponse poissonnerieResponse;
    private Integer nombreClients;
    private StatistiquesEpargneResponse epargnes;
    private StatistiquesCompteCourantResponse courantResponse;
    private List<TopProduitResponse> topProduits;
    private List<TopDebiteurResponse> topDebiteurs;
    private List<RevenueJournalierResponse> revenueMensuel;
    private List<TopProduitRentableResponse> topProduitsRentables;
}
