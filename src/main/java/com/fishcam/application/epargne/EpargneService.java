package com.fishcam.application.epargne;

import com.fishcam.adapter.web.dto.request.CreateEpargneRequest;
import com.fishcam.adapter.web.dto.request.DepotEpargneRequest;
import com.fishcam.adapter.web.dto.request.RetraitEpargneRequest;
import com.fishcam.adapter.web.dto.response.EpargneDetailResponse;
import com.fishcam.adapter.web.dto.response.EpargneResponse;
import com.fishcam.adapter.web.mapper.EpargneMapper;
import com.fishcam.adapter.web.mapper.TransactionEpargneMapper;
import com.fishcam.domain.client.Client;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.epargne.*;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpargneService {

    private final EpargneRepository epargneRepository;
    private final TransactionEpargneRepository transactionEpargneRepository;
    private final ClientRepository clientRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final EpargneMapper epargneMapper;
    private final TransactionEpargneMapper transactionEpargneMapper;


    @LogAudit(action = "CREATE", entityName = "Epargne")
    @Transactional
    public EpargneResponse createEpargne(CreateEpargneRequest request, Long userId) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        if (epargneRepository.existsByClient(client)) {
            throw new BusinessException("Ce client a déjà un compte épargne");
        }


        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        Epargne epargne = epargneMapper.toEntity(request);
        epargne.setClient(client);
        epargne.setCreatedBy(createdBy);
        epargne.setCurrentBalance(request.getInitialAmount());

        // IMPORTANT : Sauvegarder l'épargne EN PREMIER pour avoir un ID
        Epargne savedEpargne = epargneRepository.save(epargne);

        // Transaction initiale (maintenant epargne a un ID)
        TransactionEpargne transaction = new TransactionEpargne();
        transaction.setEpargne(savedEpargne);  // Utiliser savedEpargne avec ID
        transaction.setType(TypeTransactionEpargne.DEPOT);
        transaction.setAmount(request.getInitialAmount());
        transaction.setEffectuePar(createdBy);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionEpargneRepository.save(transaction);

        return epargneMapper.toResponse(savedEpargne);
    }

    @LogAudit(action = "DEPOT", entityName = "Epargne")
    @Transactional
    public EpargneResponse deposer(DepotEpargneRequest request, Long userId) {
        Epargne epargne = epargneRepository.findById(request.getEpargneId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte épargne non trouvé"));

        // 🔴 NOUVEAU : Vérifier si le client est actif
        if (!epargne.getClient().getActive()) {
            throw new BusinessException("Impossible de faire un dépôt : le client est inactif.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        epargne.setCurrentBalance(epargne.getCurrentBalance().add(request.getAmount()));

        TransactionEpargne transaction = new TransactionEpargne();
        transaction.setEpargne(epargne);
        transaction.setType(TypeTransactionEpargne.DEPOT);
        transaction.setAmount(request.getAmount());
        transaction.setEffectuePar(user);
        transaction.setPoissonnerie(epargne.getPoissonnerie());
        transaction.setTransactionDate(LocalDateTime.now());

        transactionEpargneRepository.save(transaction);

        return epargneMapper.toResponse(epargne);
    }


    @LogAudit(action = "RETRAIT", entityName = "Epargne")
    @Transactional
    public EpargneResponse retirer(RetraitEpargneRequest request, Long userId) {
        Epargne epargne = epargneRepository.findById(request.getEpargneId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte épargne non trouvé"));

        // 🔴 NOUVEAU : Vérifier si le client est actif
        if (!epargne.getClient().getActive()) {
            throw new BusinessException("Impossible de faire un retrait : le client est inactif.");
        }

        if (epargne.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Solde insuffisant. Solde actuel : " + epargne.getCurrentBalance());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        epargne.setCurrentBalance(epargne.getCurrentBalance().subtract(request.getAmount()));

        TransactionEpargne transaction = new TransactionEpargne();
        transaction.setEpargne(epargne);
        transaction.setType(TypeTransactionEpargne.RETRAIT);
        transaction.setAmount(request.getAmount());
        transaction.setEffectuePar(user);
        transaction.setPoissonnerie(epargne.getPoissonnerie());
        transaction.setTransactionDate(LocalDateTime.now());

        transactionEpargneRepository.save(transaction);

        return epargneMapper.toResponse(epargne);
    }


    public EpargneDetailResponse getEpargneDetail(Long epargneId) {
        Epargne epargne = epargneRepository.findById(epargneId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte épargne non trouvé"));

        List<TransactionEpargne> transactions =
                transactionEpargneRepository.findByEpargneOrderByTransactionDateDesc(epargne);

        EpargneDetailResponse detail = epargneMapper.toDetailResponse(epargne);
        detail.setTransactions(transactions.stream()
                .map(transactionEpargneMapper::toResponse)
                .toList());
        detail.setNombreTransactions(transactions.size());

        BigDecimal totalDepots = transactions.stream()
                .filter(t -> t.getType() == TypeTransactionEpargne.DEPOT)
                .map(TransactionEpargne::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRetraits = transactions.stream()
                .filter(t -> t.getType() == TypeTransactionEpargne.RETRAIT)
                .map(TransactionEpargne::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        detail.setTotalDepots(totalDepots);
        detail.setTotalRetraits(totalRetraits);

        return detail;
    }
}