package com.fishcam.application.comptecourant;

import com.fishcam.adapter.web.dto.request.TransfertEpargneVersCCRequest;
import com.fishcam.adapter.web.dto.response.TransfertEpargneVersCCResponse;
import com.fishcam.domain.comptecourant.*;
import com.fishcam.domain.epargne.*;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransfertEpargneVersCCUseCase {

    private final CompteCourantRepository compteCourantRepository;
    private final EpargneRepository epargneRepository;
    private final TransactionCompteCourantRepository transactionCCRepository;
    private final TransactionEpargneRepository transactionEpargneRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransfertEpargneVersCCResponse execute(TransfertEpargneVersCCRequest request, Long userId) {

        CompteCourant compte = compteCourantRepository.findById(request.getCompteCourantId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte courant non trouvé"));

        Epargne epargne = epargneRepository.findById(request.getEpargneId())
                .orElseThrow(() -> new ResourceNotFoundException("Épargne non trouvée"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!compte.getClient().getActive()) {
            throw new BusinessException("Impossible de faire un transfert : le client est inactif.");
        }

        if (!compte.getClient().getId().equals(epargne.getClient().getId())) {
            throw new BusinessException("Le compte courant et l'épargne ne sont pas du même client");
        }

        if (!compte.getClient().getId().equals(epargne.getClient().getId())) {
            throw new BusinessException("Le compte courant et l'épargne ne sont pas du même client");
        }

        if (compte.getStatut() != StatutCompteCourant.ACTIF) {
            throw new BusinessException("Ce compte n'est pas actif");
        }

        if (epargne.getCurrentBalance().compareTo(request.getMontant()) < 0) {
            throw new BusinessException("Solde épargne insuffisant. Solde actuel : "
                    + epargne.getCurrentBalance() + " FCFA");
        }

        if (request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant doit être supérieur à 0");
        }

        TransactionEpargne transactionEpargne = new TransactionEpargne();
        transactionEpargne.setEpargne(epargne);
        transactionEpargne.setType(TypeTransactionEpargne.RETRAIT);
        transactionEpargne.setAmount(request.getMontant());
        transactionEpargne.setPoissonnerie(epargne.getPoissonnerie());
        transactionEpargne.setEffectuePar(user);
        transactionEpargne.setTransactionDate(LocalDateTime.now());

        transactionEpargneRepository.save(transactionEpargne);

        epargne.setCurrentBalance(epargne.getCurrentBalance().subtract(request.getMontant()));
        epargneRepository.save(epargne);

        BigDecimal soldePrecedent = compte.getSolde();
        BigDecimal nouveauSolde = soldePrecedent.add(request.getMontant());

        TransactionCompteCourant transactionCC = new TransactionCompteCourant();
        transactionCC.setCompteCourant(compte);
        transactionCC.setType(TypeTransactionCC.REMBOURSEMENT);
        transactionCC.setMontant(request.getMontant());
        transactionCC.setSoldePrecedent(soldePrecedent);
        transactionCC.setSoldeApres(nouveauSolde);
        transactionCC.setPoissonnerie(compte.getPoissonnerie());
        transactionCC.setEffectuePar(user);
        transactionCC.setTransactionDate(LocalDateTime.now());
        transactionCC.setDescription("Remboursement via épargne");

        transactionCCRepository.save(transactionCC);

        compte.setSolde(nouveauSolde);
        compteCourantRepository.save(compte);

        TransfertEpargneVersCCResponse response = new TransfertEpargneVersCCResponse();
        response.setSuccess(true);
        response.setMessage("Transfert effectué avec succès");
        response.setMontantTransfere(request.getMontant());
        response.setNouveauSoldeEpargne(epargne.getCurrentBalance());
        response.setNouveauSoldeCompteCourant(compte.getSolde());
        response.setCompteSolde(nouveauSolde.compareTo(BigDecimal.ZERO) == 0);

        return response;
    }
}