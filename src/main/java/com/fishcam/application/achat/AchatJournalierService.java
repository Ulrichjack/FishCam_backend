package com.fishcam.application.achat;

import com.fishcam.adapter.web.dto.request.CreateFactureRequest;
import com.fishcam.adapter.web.dto.request.CreateLigneRequest;
import com.fishcam.adapter.web.dto.request.UpdateLigneRequest;
import com.fishcam.adapter.web.dto.response.DernierPrixResponse;
import com.fishcam.adapter.web.dto.response.FactureDetailResponse;
import com.fishcam.adapter.web.dto.response.FactureResponse;
import com.fishcam.adapter.web.dto.response.LigneAchatResponse;
import com.fishcam.adapter.web.mapper.AchatMapper;
import com.fishcam.domain.achat.*;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.produit.Produit;
import com.fishcam.domain.produit.ProduitRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchatJournalierService {

    private final AchatJournalierRepository achatJournalierRepository;
    private final LigneAchatRepository ligneAchatRepository;
    private final ProduitRepository produitRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UserRepository userRepository;
    private final AchatMapper achatMapper;


    @LogAudit(action = "CREATE", entityName = "AchatJournalier")
    @Transactional
    public FactureResponse createFacture(CreateFactureRequest request, Long userId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + request.getPoissonnerieId()
                ));
        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvée avec l'id : " + request.getFournisseurId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User non trouvée avec l'id : " + userId));

        AchatJournalier facture = achatMapper.toEntity(request);
        facture.setPoissonnerie(poissonnerie);
        facture.setFournisseur(fournisseur);
        facture.setEnregistrePar(user);
        facture.setCloture(false);

        AchatJournalier savedAchat = achatJournalierRepository.save(facture);
        return mapFactureToResponseWithTotal(savedAchat);

    }

    public List<FactureResponse> getFacturesByPoissonnerieAndDate(Long poissonnerieId, LocalDate date) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId
                ));

        return achatJournalierRepository.findByPoissonnerieIdAndDateAchat(poissonnerieId, date)
                .stream()
                .map(this::mapFactureToResponseWithTotal)
                .toList();
    }

    public FactureDetailResponse getFactureDetail(Long factureId) {
        AchatJournalier facture = achatJournalierRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture non trouvée avec l'id : " + factureId
                ));
        FactureDetailResponse detail = achatMapper.toDetailResponse(facture);

        List<LigneAchat> lignes = ligneAchatRepository.findByAchatJournalier(facture);
        List<LigneAchatResponse> lignesResponse = lignes.stream()
                .map(achatMapper::toLigneResponse)
                .toList();
        lignesResponse.forEach(ligne -> {
            BigDecimal prixAchatKilo = ligne.getMontantCarton()
                    .divide(ligne.getPoidsKg(), 2, RoundingMode.HALF_UP);
            BigDecimal prixVenteTotal = ligne.getPrixVenteKilo()
                    .multiply(ligne.getPoidsKg());
            BigDecimal margeKilo = ligne.getPrixVenteKilo().subtract(prixAchatKilo);
            BigDecimal margeTotal = prixVenteTotal.subtract(ligne.getMontantCarton());

            ligne.setPrixAchatKilo(prixAchatKilo);
            ligne.setPrixVenteTotal(prixVenteTotal);
            ligne.setMargeKilo(margeKilo);
            ligne.setMargeTotal(margeTotal);
        });

        // Calculer les totaux depuis les lignes
        BigDecimal totalAchat = lignesResponse.stream()
                .map(LigneAchatResponse::getMontantCarton)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVente = lignesResponse.stream()
                .map(LigneAchatResponse::getPrixVenteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeTotal = totalVente.subtract(totalAchat);
        detail.setLigneAchatResponses(lignesResponse);
        detail.setTotalAchat(totalAchat);
        detail.setTotalVente(totalVente);
        detail.setMargeTotal(margeTotal);

        return detail;
    }

    @LogAudit(action = "CLOTURE", entityName = "AchatJournalier")
    @Transactional
    public FactureResponse cloturerFacture(Long factureId) {
        AchatJournalier facture = achatJournalierRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture non trouvée avec l'id : " + factureId
                ));
        if (facture.getCloture()) {
            throw new BusinessException("Facture déjà clôturée");
        }
        facture.setCloture(true);
        AchatJournalier savedFacture = achatJournalierRepository.save(facture);
        return mapFactureToResponseWithTotal(savedFacture);
    }

    @LogAudit(action = "ADD Ligne", entityName = "AchatJournalier")
    @Transactional
    public LigneAchatResponse addLigne(Long factureId, CreateLigneRequest request) {
        AchatJournalier facture = achatJournalierRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture non trouvée avec l'id : " + factureId
                ));
        if (facture.getCloture()) {
            throw new BusinessException("Facture déjà clôturée, modification impossible");
        }
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'id : " + request.getProduitId()));

        LigneAchat ligne = achatMapper.toLigneEntity(request);
        ligne.setAchatJournalier(facture);
        ligne.setProduit(produit);
        ligne.setPrixUnitaireCarton(request.getPrixUnitaireCarton());

        BigDecimal totalLigne = request.getPrixUnitaireCarton()
                .multiply(BigDecimal.valueOf(request.getQuantiteCartons()));
        ligne.setMontantCarton(totalLigne);



        LigneAchat savedLigne = ligneAchatRepository.save(ligne);
        LigneAchatResponse response = achatMapper.toLigneResponse(savedLigne);

        // Champs calculés
        BigDecimal prixAchatKilo = response.getMontantCarton()
                .divide(response.getPoidsKg(), 2, RoundingMode.HALF_UP);
        BigDecimal prixVenteTotal = response.getPrixVenteKilo()
                .multiply(response.getPoidsKg());

        response.setPrixAchatKilo(prixAchatKilo);
        response.setPrixVenteTotal(prixVenteTotal);
        response.setMargeKilo(response.getPrixVenteKilo().subtract(prixAchatKilo));
        response.setMargeTotal(prixVenteTotal.subtract(response.getMontantCarton()));

        return response;

    }


    @LogAudit(action = "UPDATE", entityName = "AchatJournalier")
    @Transactional
    public LigneAchatResponse updateLigne(Long factureId, Long ligneId, UpdateLigneRequest request) {
        // 1. Charger facture
        AchatJournalier facture = achatJournalierRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture non trouvée avec l'id : " + factureId));

        if (facture.getCloture()) {
            throw new BusinessException("Facture clôturée, modification impossible");
        }
        LigneAchat ligne = ligneAchatRepository.findById(ligneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ligne non trouvée avec l'id : " + ligneId));

        if (!ligne.getAchatJournalier().getId().equals(factureId)) {
            throw new BusinessException("Cette ligne n'appartient pas à cette facture");
        }

        if (request.getQuantiteCartons() != null) {
            ligne.setQuantiteCartons(request.getQuantiteCartons());
        }
        if (request.getPoidsKg() != null) {
            ligne.setPoidsKg(request.getPoidsKg());
        }
        if (request.getPrixUnitaireCarton() != null) {
            ligne.setPrixUnitaireCarton(request.getPrixUnitaireCarton());
        }
        if (request.getPrixVenteKilo() != null) {
            ligne.setPrixVenteKilo(request.getPrixVenteKilo());
        }

        BigDecimal nouveauTotal = ligne.getPrixUnitaireCarton()
                .multiply(BigDecimal.valueOf(ligne.getQuantiteCartons()));
        ligne.setMontantCarton(nouveauTotal);

        LigneAchat savedLigne = ligneAchatRepository.save(ligne);
        LigneAchatResponse response = achatMapper.toLigneResponse(savedLigne);

        BigDecimal prixAchatKilo = response.getMontantCarton()
                .divide(response.getPoidsKg(), 2, RoundingMode.HALF_UP);
        BigDecimal prixVenteTotal = response.getPrixVenteKilo()
                .multiply(response.getPoidsKg());

        response.setPrixAchatKilo(prixAchatKilo);
        response.setPrixVenteTotal(prixVenteTotal);
        response.setMargeKilo(response.getPrixVenteKilo().subtract(prixAchatKilo));
        response.setMargeTotal(prixVenteTotal.subtract(response.getMontantCarton()));

        return response;
    }

    @LogAudit(action = "DELETE", entityName = "AchatJournalier")
    @Transactional
    public void deleteLigne(Long factureId, Long ligneId) {
        AchatJournalier facture = achatJournalierRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture non trouvée avec l'id : " + factureId));

        if (facture.getCloture()) {
            throw new BusinessException("Facture clôturée, suppression impossible");
        }

        LigneAchat ligne = ligneAchatRepository.findById(ligneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ligne non trouvée avec l'id : " + ligneId));

        if (!ligne.getAchatJournalier().getId().equals(factureId)) {
            throw new BusinessException("Cette ligne n'appartient pas à cette facture");
        }

        ligneAchatRepository.delete(ligne);
    }


    public DernierPrixResponse getDernierPrix(Long produitId, Long poissonnerieId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec l'id : " + produitId));

        // 1. Fetch the last TWO purchases (PageRequest.of(0, 2))
        Pageable pageable = PageRequest.of(0, 2);
        List<LigneAchat> results = ligneAchatRepository
                .findLatestPricesByProduitAndPoissonnerie(produitId, poissonnerieId, pageable);

        DernierPrixResponse response = new DernierPrixResponse();
        response.setPoidsParCarton(produit.getPoidsParCarton());

        // 2. If the product was NEVER bought before
        if (results.isEmpty()) {
            response.setFluctuation(TypeFluctuation.NOUVEAU);
            return response;
        }

        // 3. Get the most recent purchase
        LigneAchat lastPurchase = results.get(0);
        response.setMontantCarton(lastPurchase.getMontantCarton());
        response.setPrixVenteKilo(lastPurchase.getPrixVenteKilo());

        // 4. Compare with the previous purchase
        if (results.size() == 1) {
            // It was only bought ONCE in history.
            response.setFluctuation(TypeFluctuation.NOUVEAU);
            response.setDifference(BigDecimal.ZERO);
        } else {
            // We have a previous purchase!
            LigneAchat previousPurchase = results.get(1);
            response.setAncienMontantCarton(previousPurchase.getMontantCarton());

            // Calculate the difference: (Today's Price) - (Yesterday's Price)
            BigDecimal difference = lastPurchase.getMontantCarton().subtract(previousPurchase.getMontantCarton());
            response.setDifference(difference);

            // Determine if it went UP, DOWN, or stayed STABLE
            int comparison = difference.compareTo(BigDecimal.ZERO);
            if (comparison > 0) {
                response.setFluctuation(TypeFluctuation.HAUSSE);
            } else if (comparison < 0) {
                response.setFluctuation(TypeFluctuation.BAISSE);
            } else {
                response.setFluctuation(TypeFluctuation.STABLE);
            }
        }

        return response;
    }

    /**
     * Convertit une entité AchatJournalier en FactureResponse et calcule le total des achats.
     * @param facture L'entité AchatJournalier
     * @return Le DTO FactureResponse avec le total calculé.
     */
    private FactureResponse mapFactureToResponseWithTotal(AchatJournalier facture) {
        FactureResponse response = achatMapper.toResponse(facture);
        BigDecimal total = ligneAchatRepository.calculateTotalAchatByFactureId(facture.getId());
        response.setTotalAchat(total != null ? total : BigDecimal.ZERO);
        return response;
    }
}
