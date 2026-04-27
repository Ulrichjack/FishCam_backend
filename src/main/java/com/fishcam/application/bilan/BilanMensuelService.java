package com.fishcam.application.bilan;


import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.adapter.web.dto.response.ComparaisonBoutiquesResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BilanMensuelService {

    private final ClotureJournaliereRepository clotureRepository;
    private final PoissonnerieRepository poissonnerieRepository;

    public BilanMensuelResponse getBilanMensuel(Long poissonnerieId, Integer mois, Integer annee) {

        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));



        List<ClotureJournaliere> clotures = clotureRepository.findByPoissonnerieAndMoisAndAnnee(poissonnerie, mois, annee);
        if (clotures.isEmpty()) {
            throw new BusinessException("Aucune clôture trouvée pour ce mois");
        }

        BigDecimal totalAchatMois = BigDecimal.ZERO;
        BigDecimal totalVenteRealisee = BigDecimal.ZERO;
        BigDecimal totalDepensesMois = BigDecimal.ZERO;
        BigDecimal beneficeNetMois = BigDecimal.ZERO;
        BigDecimal meilleurBenefice = BigDecimal.ZERO;
        BigDecimal totalVentePrevisibleMois = BigDecimal.ZERO;
        LocalDate meilleurJour     = null;


        for (ClotureJournaliere cloture : clotures){
            totalAchatMois     = totalAchatMois.add(cloture.getTotalAchat());
            totalVenteRealisee = totalVenteRealisee.add(cloture.getVenteRealisee());
            totalDepensesMois  = totalDepensesMois.add(cloture.getTotalDepenses());
            beneficeNetMois    = beneficeNetMois.add(cloture.getBeneficeNet());
            totalVentePrevisibleMois = totalVentePrevisibleMois
                    .add(cloture.getTotalVentePrevisible());

            if (cloture.getBeneficeNet().compareTo(meilleurBenefice) > 0) {
                meilleurBenefice = cloture.getBeneficeNet();
                meilleurJour     = cloture.getDate();
            }

        }
        BigDecimal montantDettes = clotures
                .get(clotures.size() - 1)
                .getMontantDettesJour();

        BilanMensuelResponse bilan = new BilanMensuelResponse();
        bilan.setPoissonnerieNom(poissonnerie.getName());
        bilan.setMois(mois);
        bilan.setAnnee(annee);
        bilan.setTotalAchatMois(totalAchatMois);
        bilan.setTotalVenteRealisee(totalVenteRealisee);
        bilan.setTotalDepensesMois(totalDepensesMois);
        bilan.setBeneficeNetMois(beneficeNetMois);
        bilan.setMontantDettesMois(montantDettes);
        bilan.setTotalVentePrevisibleMois(totalVentePrevisibleMois);
        bilan.setMeilleurJourBenefice(meilleurJour);
        bilan.setBeneficeMeilleurJour(meilleurBenefice);

        bilan.setNombreJoursTravailles(clotures.size());


        return  bilan;
    }

    public ComparaisonBoutiquesResponse compareBoutiques(Integer mois, Integer annee) {
        BigDecimal totalAchatGlobal = BigDecimal.ZERO;
        BigDecimal totalVenteGlobal = BigDecimal.ZERO;
        BigDecimal totalDepensesGlobal = BigDecimal.ZERO;
        BigDecimal beneficeNetGlobal = BigDecimal.ZERO;

        List<BilanMensuelResponse> bilans = new ArrayList<>();
        List<Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();

        for (Poissonnerie poissonnerie : poissonneries) {
            try {
                BilanMensuelResponse bilan = getBilanMensuel(poissonnerie.getId(), mois, annee);
                bilans.add(bilan);
                totalAchatGlobal = totalAchatGlobal.add(bilan.getTotalAchatMois());
                totalVenteGlobal = totalVenteGlobal.add(bilan.getTotalVenteRealisee());
                totalDepensesGlobal = totalDepensesGlobal.add(bilan.getTotalDepensesMois());
                beneficeNetGlobal = beneficeNetGlobal.add(bilan.getBeneficeNetMois());

            } catch (BusinessException e) {
                log.warn("Pas de bilan trouvé pour la boutique {} sur la période {}-{} : {}",
                        poissonnerie.getName(), mois, annee, e.getMessage());
            }
        }
        return new ComparaisonBoutiquesResponse(
                bilans,
                totalVenteGlobal,
                totalAchatGlobal,
                totalDepensesGlobal,
                beneficeNetGlobal,
                mois,
                annee
        );
    }

}
