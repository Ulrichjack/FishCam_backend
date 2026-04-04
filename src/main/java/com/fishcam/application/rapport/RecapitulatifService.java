package com.fishcam.application.rapport;

import com.fishcam.adapter.web.dto.response.RecapitulatifLigneResponse;
import com.fishcam.adapter.web.dto.response.RecapitulatifResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecapitulatifService {

    private final ClotureJournaliereRepository clotureJournaliereRepository;
    private final PoissonnerieRepository poissonnerieRepository;

    public RecapitulatifResponse generateRecapitulatif(Long poissonnerieId, LocalDate start, LocalDate end){

        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));

        List<ClotureJournaliere> clotures = clotureJournaliereRepository.findByPoissonnerieAndDateBetweenOrderByDateAsc(poissonnerie,start,end);

         BigDecimal totalAchat = BigDecimal.ZERO;
         BigDecimal totalPrevu = BigDecimal.ZERO;
         BigDecimal totalRealise = BigDecimal.ZERO;
         BigDecimal totalDepenses = BigDecimal.ZERO;
         BigDecimal totalBenefice = BigDecimal.ZERO;

         List<RecapitulatifLigneResponse> lignes = new ArrayList<>();

        for (ClotureJournaliere cloture : clotures){
            totalAchat = totalAchat.add(cloture.getTotalAchat());
            totalPrevu = totalPrevu.add(cloture.getTotalVentePrevisible());
            totalRealise = totalRealise.add(cloture.getVenteRealisee());
            totalDepenses = totalDepenses.add(cloture.getTotalDepenses());
            totalBenefice = totalBenefice.add(cloture.getBeneficeNet());

            RecapitulatifLigneResponse ligne = new RecapitulatifLigneResponse(
                    cloture.getDate(),
                    cloture.getTotalAchat(),
                    cloture.getTotalVentePrevisible(),
                    cloture.getVenteRealisee(),
                    cloture.getTotalDepenses(),
                    cloture.getBeneficeNet()
            );
            lignes.add(ligne);
        }

        // Return the final calculated DTO
        return new RecapitulatifResponse(
                lignes,
                totalAchat,
                totalPrevu,
                totalRealise,
                totalDepenses,
                totalBenefice
        );
    }
}


