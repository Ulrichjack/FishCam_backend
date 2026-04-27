package com.fishcam.application.cloture;

import com.fishcam.adapter.web.dto.request.ClotureJournaliereRequest;
import com.fishcam.adapter.web.dto.response.ClotureJournaliereResponse;
import com.fishcam.adapter.web.dto.response.PreparationClotureResponse;
import com.fishcam.adapter.web.mapper.ClotureMapper;
import com.fishcam.domain.achat.AchatJournalier;
import com.fishcam.domain.achat.AchatJournalierRepository;
import com.fishcam.domain.achat.LigneAchat;
import com.fishcam.domain.achat.LigneAchatRepository;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClotureJournaliereService {

    private final AchatJournalierRepository achatJournalierRepository;
    private final ClotureJournaliereRepository clotureJournaliereRepository;
    private final UserRepository userRepository;
    private final LigneAchatRepository ligneAchatRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final TransactionCompteCourantRepository transactionCompteCourantRepository;
    private final ClotureMapper clotureMapper;


    public PreparationClotureResponse preparerCloture(Long poissonnerieId, LocalDate date){

        // 1. Charger la poissonnerie
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));

        // 2. Charger toutes les factures du jour
        List<AchatJournalier> factures = achatJournalierRepository
                .findByPoissonnerieIdAndDateAchat(poissonnerieId, date);

        // 3. Charger toutes les lignes de toutes les factures
        List<LigneAchat> toutesLesLignes = factures.stream()
                .flatMap(f -> ligneAchatRepository.findByAchatJournalier(f).stream())
                .toList();

        // 4. Calculer totalAchat = somme des montantCarton
        BigDecimal totalAchat = toutesLesLignes.stream()
                .map(LigneAchat::getMontantCarton)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Calculer totalVentePrevisible = somme de (prixVenteKilo × poidsKg)
        BigDecimal totalVentePrevisible = toutesLesLignes.stream()
                .map(l -> l.getPrixVenteKilo().multiply(l.getPoidsKg()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Récupérer fondDeCaisseDefaut depuis Poissonnerie
        BigDecimal fondDeCaisseDefaut = poissonnerie.getFondDeCaisseDefaut();

        // 7. Récupérer dettes depuis CompteCourant
        BigDecimal montantDettes = compteCourantRepository
                .sumTotalDettes(poissonnerie);
        Long nombreDettes = compteCourantRepository
                .countComptesEnDette(poissonnerie);

        // 8. Récupérer remboursements du jour
        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour = date.atTime(23, 59, 59);
        BigDecimal montantRembourse = transactionCompteCourantRepository
                .sumMontantByPoissonnerieAndTypeAndPeriod(
                        poissonnerie,
                        TypeTransactionCC.REMBOURSEMENT,
                        debutJour,
                        finJour);

        // 9. Construire la réponse
        PreparationClotureResponse response = new PreparationClotureResponse();
        response.setTotalAchat(totalAchat);
        response.setTotalVentePrevisible(totalVentePrevisible);
        response.setFondDeCaisseDefaut(fondDeCaisseDefaut);
        response.setMontantDettesJour(montantDettes != null ? montantDettes : BigDecimal.ZERO);
        response.setNombreDettesJour(nombreDettes.intValue());
        response.setMontantRembourseJour(montantRembourse != null ? montantRembourse : BigDecimal.ZERO);

        return response;
    }

    @LogAudit(action = "CLOTURE", entityName = "ClotureJournaliere")
    @Transactional
    public ClotureJournaliereResponse cloturer(ClotureJournaliereRequest request, Long userId){

        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + request.getPoissonnerieId()
                ));

        if (clotureJournaliereRepository
                .existsByPoissonnerieAndDate(poissonnerie, request.getDate())) {
            throw new BusinessException("Journée déjà clôturée pour cette boutique");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User non trouvée avec l'id : " + userId));

        PreparationClotureResponse preparer =  preparerCloture(request.getPoissonnerieId(), request.getDate());

        BigDecimal venteRealisee = request.getArgentCaisse().subtract(request.getFondDeCaisse());

        BigDecimal transport = request.getTransport() != null
                ? request.getTransport() : BigDecimal.ZERO;
        BigDecimal ration = request.getRation() != null
                ? request.getRation() : BigDecimal.ZERO;
        BigDecimal autresFrais = request.getAutresFrais() != null
                ? request.getAutresFrais() : BigDecimal.ZERO;

        BigDecimal totalDepenses = transport.add(ration).add(autresFrais);

        BigDecimal beneficeNet = venteRealisee.subtract(totalDepenses).subtract(preparer.getTotalAchat());
        BigDecimal ecartVente = preparer.getTotalVentePrevisible().subtract(venteRealisee);

        ClotureJournaliere cloture = clotureMapper.toEntity(request);
        cloture.setPoissonnerie(poissonnerie);
        cloture.setCloturePar(user);
        cloture.setTotalAchat(preparer.getTotalAchat());
        cloture.setTotalVentePrevisible(preparer.getTotalVentePrevisible());
        cloture.setVenteRealisee(venteRealisee);
        cloture.setTotalDepenses(totalDepenses);
        cloture.setBeneficeNet(beneficeNet);
        cloture.setMontantDettesJour(preparer.getMontantDettesJour());
        cloture.setMontantRembourseJour(preparer.getMontantRembourseJour());
        cloture.setNombreDettesJour(preparer.getNombreDettesJour());

        ClotureJournaliere saved = clotureJournaliereRepository.save(cloture);
        ClotureJournaliereResponse response = clotureMapper.toResponse(saved);
        response.setEcartVente(ecartVente);
        return response;

    }

    public ClotureJournaliereResponse getCloture(Long poissonnerieId, LocalDate date){
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId
                ));
        ClotureJournaliere clotureJournaliere = clotureJournaliereRepository.findByPoissonnerieAndDate(poissonnerie, date)
                .orElseThrow(()-> new BusinessException("Aucune clôture trouvée pour cette date"));
        return  clotureMapper.toResponse(clotureJournaliere);
    }

    public List<ClotureJournaliereResponse> getHistorique(Long poissonnerieId){
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId
                ));
        return clotureJournaliereRepository.findByPoissonnerieOrderByDateDesc(poissonnerie)
                .stream()
                .map(clotureMapper::toResponse)
                .toList();

    }

}
