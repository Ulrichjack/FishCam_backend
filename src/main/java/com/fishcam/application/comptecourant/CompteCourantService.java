package com.fishcam.application.comptecourant;

import com.fishcam.adapter.web.dto.request.EmpruntRequest;
import com.fishcam.adapter.web.dto.request.ModifierLimiteCreditRequest;
import com.fishcam.adapter.web.dto.request.RemboursementCCRequest;
import com.fishcam.adapter.web.dto.response.CompteCourantDetailResponse;
import com.fishcam.adapter.web.dto.response.CompteCourantResponse;
import com.fishcam.adapter.web.dto.response.TransactionGlobalResponse;
import com.fishcam.adapter.web.mapper.CompteCourantMapper;
import com.fishcam.adapter.web.mapper.TransactionCCMapper;
import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.client.Client;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.comptecourant.*;
import com.fishcam.domain.epargne.TransactionEpargne;
import com.fishcam.domain.epargne.TransactionEpargneRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompteCourantService {

    private final CompteCourantRepository compteCourantRepository;
    private final TransactionCompteCourantRepository transactionCompteCourantRepository;
    private final ClientRepository clientRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CompteCourantMapper compteCourantMapper;
    private final TransactionCCMapper transactionCCMapper;
    private final TransactionCustomRepository transactionCustomRepository;

    @LogAudit(action = "CREATE", entityName = "CompteCourant")
    @Transactional
    public CompteCourantResponse createCompteCourant(Long clientId, Long userId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        if (!client.getPoissonnerie().getPretActif()) {
            throw new BusinessException("Impossible de créer un compte courant : les prêts ne sont pas autorisés dans cette poissonnerie.");
        }

        if (compteCourantRepository.existsByClient(client)) {
            throw new BusinessException("Ce client a déjà un compte courant");
        }



        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        CompteCourant compte = new CompteCourant();
        compte.setClient(client);
        compte.setCreatedBy(createdBy);
        compte.setSolde(BigDecimal.ZERO);
        compte.setLimiteCreditMax(new BigDecimal("50000"));
        compte.setStatut(StatutCompteCourant.ACTIF);


        CompteCourant saved = compteCourantRepository.save(compte);
        return compteCourantMapper.toResponse(saved);
    }

    @LogAudit(action = "EMPRUNT", entityName = "CompteCourant")
    @Transactional
    public CompteCourantResponse enregistrerEmprunt(EmpruntRequest request, Long userId) {
        CompteCourant compte = compteCourantRepository.findByIdWithLock(request.getCompteCourantId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte courant non trouvé"));

        //  Vérifier si le client est actif
        if (!compte.getClient().getActive()) {
            throw new BusinessException("Impossible d'enregistrer un emprunt : le client est inactif.");
        }

        if (!compte.getPoissonnerie().getPretActif()) {
            throw new BusinessException("La gestion des prêts/dettes n'est pas activée pour cette poissonnerie ("
                    + compte.getPoissonnerie().getName() + "). Vente au comptant uniquement.");
        }

        if (compte.getStatut() != StatutCompteCourant.ACTIF) {
            throw new BusinessException("Ce compte n'est pas actif");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        BigDecimal soldePrecedent = compte.getSolde();
        BigDecimal nouveauSolde = soldePrecedent.subtract(request.getMontant());

        // 🔴 NOUVEAU : Vérifier si le nouveau solde dépasse la limite autorisée
        // La limite est stockée en positif (ex: 50000), on la passe en négatif (-50000) pour comparer avec le solde
        BigDecimal limiteNegative = compte.getLimiteCreditMax().negate();
        if (nouveauSolde.compareTo(limiteNegative) < 0) {
            throw new BusinessException("Limite de crédit dépassée. Le client ne peut plus emprunter.");
        }

        boolean depassaitSeuilAvant = soldePrecedent.compareTo(new BigDecimal("-5000")) < 0;
        boolean depasseSeuilApres = nouveauSolde.compareTo(new BigDecimal("-5000")) < 0;

        TransactionCompteCourant transaction = new TransactionCompteCourant();
        transaction.setCompteCourant(compte);
        transaction.setType(TypeTransactionCC.EMPRUNT);
        transaction.setMontant(request.getMontant());
        transaction.setSoldePrecedent(soldePrecedent);
        transaction.setSoldeApres(nouveauSolde);
        transaction.setDescription(request.getDescription());
        transaction.setPoissonnerie(compte.getPoissonnerie());
        transaction.setEffectuePar(user);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionCompteCourantRepository.save(transaction);

        compte.setSolde(nouveauSolde);
        compteCourantRepository.save(compte);

        if (!depassaitSeuilAvant && depasseSeuilApres) {
            notificationService.createAlerteCompteCourant(compte, "FRANCHISSEMENT_SEUIL");
        } else if (depassaitSeuilAvant && depasseSeuilApres) {
            BigDecimal augmentation = request.getMontant();
            if (augmentation.compareTo(new BigDecimal("3000")) >= 0) {
                notificationService.createAlerteCompteCourant(compte, "AUGMENTATION_SIGNIFICATIVE");
            }
        }

        return compteCourantMapper.toResponse(compte);
    }

    @LogAudit(action = "REMBOURSEMENT", entityName = "CompteCourant")
    @Transactional
    public CompteCourantResponse enregistrerRemboursement(RemboursementCCRequest request, Long userId) {
        CompteCourant compte = compteCourantRepository.findByIdWithLock(request.getCompteCourantId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte courant non trouvé"));

        // Vérifier si le client est actif
        if (!compte.getClient().getActive()) {
            throw new BusinessException("Impossible d'enregistrer un remboursement : le client est inactif.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        BigDecimal soldePrecedent = compte.getSolde();
        BigDecimal nouveauSolde = soldePrecedent.add(request.getMontant());

        TransactionCompteCourant transaction = new TransactionCompteCourant();
        transaction.setCompteCourant(compte);
        transaction.setType(TypeTransactionCC.REMBOURSEMENT);
        transaction.setMontant(request.getMontant());
        transaction.setSoldePrecedent(soldePrecedent);
        transaction.setSoldeApres(nouveauSolde);
        transaction.setDescription("Remboursement cash");
        transaction.setPoissonnerie(compte.getPoissonnerie());
        transaction.setEffectuePar(user);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(request.getNotes());

        transactionCompteCourantRepository.save(transaction);

        compte.setSolde(nouveauSolde);
        compteCourantRepository.save(compte);

        if (nouveauSolde.compareTo(BigDecimal.ZERO) == 0) {
            notificationService.createNotificationCompteSolde(compte);
        }

        return compteCourantMapper.toResponse(compte);
    }

    @LogAudit(action = "UPDATE", entityName = "CompteCourant")
    @Transactional
    public CompteCourantResponse modifierLimiteCredit(Long compteId, ModifierLimiteCreditRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        CompteCourant compte = compteCourantRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte courant non trouvé"));

        // ═══════════════════════════════════════════════════════════
        // CONTRÔLE 1 : Vérification des rôles
        // ═══════════════════════════════════════════════════════════
        if (user.getRole() != Role.SUPER_ADMIN && user.getRole() != Role.PATRON) {
            throw new BusinessException("❌ Accès refusé : Seul le patron peut modifier les limites de crédit");
        }

        // ═══════════════════════════════════════════════════════════
        // CONTRÔLE 2 : Limite maximum selon le rôle (SUPPRIMÉ POUR PATRON)
        // Le PATRON peut maintenant mettre n'importe quelle limite
        // Seul SUPER_ADMIN a accès illimité
        // ═══════════════════════════════════════════════════════════
        // Plus de limite pour PATRON !

        // ═══════════════════════════════════════════════════════════
        // CONTRÔLE 3 : Validation montant minimum
        // ═══════════════════════════════════════════════════════════
        BigDecimal limiteMinimum = new BigDecimal("5000");
        if (request.getNouvelleLimit().compareTo(limiteMinimum) < 0) {
            throw new BusinessException("❌ La limite minimum est de 5000 FCFA");
        }

        // ═══════════════════════════════════════════════════════════
        // CONTRÔLE 4 : Interdire de diminuer si dette actuelle dépasse
        // ═══════════════════════════════════════════════════════════
        BigDecimal detteActuelle = compte.getMontantDette();
        if (detteActuelle.compareTo(request.getNouvelleLimit()) > 0) {
            throw new BusinessException(
                    String.format(
                            " Impossible de réduire la limite à %s FCFA : " +
                                    "Le client doit actuellement %s FCFA. " +
                                    "Il doit d'abord rembourser.",
                            request.getNouvelleLimit(),
                            detteActuelle
                    )
            );
        }

        // ═══════════════════════════════════════════════════════════
        // ALERTE SYSTÉMATIQUE sur TOUTE modification
        // ═══════════════════════════════════════════════════════════
        BigDecimal ancienneLimit = compte.getLimiteCreditMax();

        // Toujours envoyer notification (sauf si pas de changement)
        if (!ancienneLimit.equals(request.getNouvelleLimit())) {
            notificationService.createAlerteModificationLimite(
                    compte,
                    ancienneLimit,
                    request.getNouvelleLimit(),
                    user
            );
        }

        // ═══════════════════════════════════════════════════════════
        // APPLIQUER LA MODIFICATION
        // ═══════════════════════════════════════════════════════════
        compte.setLimiteCreditMax(request.getNouvelleLimit());
        compteCourantRepository.save(compte);

        System.out.println(String.format(
                "✅ Limite modifiée : Client %s | Ancienne: %s → Nouvelle: %s | Par: %s %s (%s)",
                compte.getClient().getFirstName(),
                ancienneLimit,
                request.getNouvelleLimit(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        ));

        return compteCourantMapper.toResponse(compte);
    }

    public CompteCourantResponse getCompteCourantByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        CompteCourant compte = compteCourantRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Ce client n'a pas de compte courant"));

        return compteCourantMapper.toResponse(compte);
    }

    public CompteCourantDetailResponse getCompteCourantDetail(Long compteId) {
        CompteCourant compte = compteCourantRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte courant non trouvé"));

        List<TransactionCompteCourant> transactions =
                transactionCompteCourantRepository.findByCompteCourantOrderByTransactionDateDesc(compte);

        CompteCourantDetailResponse detail = compteCourantMapper.toDetailResponse(compte);
        detail.setTransactions(transactions.stream()
                .map(transactionCCMapper::toResponse)
                .toList());
        detail.setNombreTransactions(transactions.size());

        BigDecimal totalEmprunts = transactions.stream()
                .filter(t -> t.getType() == TypeTransactionCC.EMPRUNT)
                .map(TransactionCompteCourant::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemboursements = transactions.stream()
                .filter(t -> t.getType() == TypeTransactionCC.REMBOURSEMENT)
                .map(TransactionCompteCourant::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        detail.setTotalEmprunts(totalEmprunts);
        detail.setTotalRemboursements(totalRemboursements);

        return detail;
    }

    public List<CompteCourantResponse> getComptesEnDette(Long poissonnerieId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));

        return compteCourantRepository.findComptesEnDette(poissonnerie, BigDecimal.ZERO)
                .stream()
                .map(compteCourantMapper::toResponse)
                .toList();
    }

    public Page<TransactionGlobalResponse> getAllTransactions(Long poissonnerieId, String type, String searchTerm, LocalDate date, Pageable pageable) {
        // Vérifier que la poissonnerie existe
        poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));

        // Appeler le repository qui gère le SQL
        return transactionCustomRepository.findAllTransactionsDynamically(poissonnerieId, type, searchTerm, date, pageable);
    }


}