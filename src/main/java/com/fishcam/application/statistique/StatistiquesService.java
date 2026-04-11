package com.fishcam.application.statistique;

import com.fishcam.adapter.web.dto.response.*;
import com.fishcam.domain.achat.LigneAchatRepository;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.epargne.EpargneRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatistiquesService {

    private final PoissonnerieRepository poissonnerieRepository;
    private final ClientRepository clientRepository;
    private final EpargneRepository epargneRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final LigneAchatRepository ligneAchatRepository;
    private final ClotureJournaliereRepository clotureRepository; // <-- ADD THIS


    @Transactional(readOnly = true)
    public StatistiquesPoissonnerieResponse getDashboardStats(Long poissonnerieId) {
        log.info("Fetching dashboard statistics for poissonnerie ID: {}", poissonnerieId);

        // 1. Fetch Poissonnerie
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie not found"));

        // 2. Client Stats
        long totalClients = clientRepository.countByPoissonnerie(poissonnerie);

        // 3. Epargne Stats
        Integer totalComptesEpargne = epargneRepository.countByPoissonnerieId(poissonnerieId);
        BigDecimal totalArgentEpargne = epargneRepository.sumSoldeByPoissonnerieId(poissonnerieId);
        if (totalArgentEpargne == null) totalArgentEpargne = BigDecimal.ZERO;

        BigDecimal moyenneEpargne = (totalComptesEpargne != null && totalComptesEpargne > 0)
                ? totalArgentEpargne.divide(new BigDecimal(totalComptesEpargne), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        StatistiquesEpargneResponse epargneStats = new StatistiquesEpargneResponse(
                poissonnerieId,
                poissonnerie.getName(),
                totalComptesEpargne,
                totalArgentEpargne,
                moyenneEpargne
        );

        // 4. Compte Courant (Debt) Stats
        Long compteEnDetteCount = compteCourantRepository.countComptesEnDette(poissonnerie);
        BigDecimal totalDettes = compteCourantRepository.sumTotalDettes(poissonnerie);
        if (totalDettes == null)
            totalDettes = BigDecimal.ZERO;
        StatistiquesCompteCourantResponse courantResponseStats = new StatistiquesCompteCourantResponse(
                compteEnDetteCount,
                totalDettes
        );

        //5. List topProduit, débiteur, produit_rentable
        Pageable top5 = PageRequest.of(0, 5);
        List<TopProduitResponse> topProduits = ligneAchatRepository.findTopProduitsByPoissonnerie(poissonnerieId, top5);
        List<TopDebiteurResponse> topDebiteurs = compteCourantRepository.findTopDebiteursByPoissonnerie(poissonnerieId, top5);
        List <TopProduitRentableResponse> topProduitRentableResponses = ligneAchatRepository.findTopProduitsRentablesByPoissonnerie(poissonnerieId, top5);

        // 6. Monthly Revenue Chart (Current Month)
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        List<ClotureJournaliere> cloturesDuMois = clotureRepository
                .findByPoissonnerieAndDateBetweenOrderByDateAsc(poissonnerie, firstDayOfMonth, lastDayOfMonth);

        List<RevenueJournalierResponse> revenueMensuel = new ArrayList<>();
        for (ClotureJournaliere cloture : cloturesDuMois) {
            revenueMensuel.add(new RevenueJournalierResponse(
                    cloture.getDate(),
                    cloture.getVenteRealisee() // The total sales recorded at the end of the day
            ));
        }

        // 7. Build Response
        PoissonnerieResponse poissonnerieResponse = new PoissonnerieResponse();
        poissonnerieResponse.setId(poissonnerie.getId());
        poissonnerieResponse.setName(poissonnerie.getName());
        poissonnerieResponse.setAddress(poissonnerie.getAddress());

        StatistiquesPoissonnerieResponse response = new StatistiquesPoissonnerieResponse();
        response.setPoissonnerieResponse(poissonnerieResponse);
        response.setNombreClients((int) totalClients);
        response.setEpargnes(epargneStats);
        response.setCourantResponse(courantResponseStats);
        response.setTopProduits(topProduits);
        response.setTopDebiteurs(topDebiteurs);
        response.setTopProduitsRentables(topProduitRentableResponses);
        response.setRevenueMensuel(revenueMensuel);

        return response;
    }

    public StatistiquesGlobalesResponse getGlobalDashboardStats(){
        List <Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();
        Integer clientsGlobal = 0;
        BigDecimal epargneGlobal = BigDecimal.ZERO;
        BigDecimal dettesGlobal = BigDecimal.ZERO;
        ArrayList<StatistiquesPoissonnerieResponse> details = new ArrayList<>();

        for(Poissonnerie poissonnerie: poissonneries ){

            StatistiquesPoissonnerieResponse stats = getDashboardStats(poissonnerie.getId());
            clientsGlobal += stats.getNombreClients();
            epargneGlobal = epargneGlobal.add(stats.getEpargnes().getTotalEpargne());
            dettesGlobal = dettesGlobal.add(stats.getCourantResponse().getTotalDettes());

            details.add(stats);
        }
        return  new StatistiquesGlobalesResponse(
                clientsGlobal,
                epargneGlobal,
                dettesGlobal,
                details

        );

    }




}