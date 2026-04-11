package com.fishcam.application.client;

import com.fishcam.adapter.web.dto.request.CreateClientRequest;
import com.fishcam.adapter.web.dto.request.UpdateClientRequest;
import com.fishcam.adapter.web.dto.response.ClientDetailResponse;
import com.fishcam.adapter.web.dto.response.ClientResponse;
import com.fishcam.adapter.web.mapper.ClientMapper;
import com.fishcam.domain.client.Client;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.epargne.EpargneRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final EpargneRepository epargneRepository;
    private final ClientMapper clientMapper;
    private final UserRepository userRepository;

    @LogAudit(action = "CREATE", entityName = "Client")
    @Transactional
    public ClientResponse createClient(CreateClientRequest request, Long userId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + request.getPoissonnerieId()));

        if (existsByPhoneAndPoissonnerie(request.getPhone(), poissonnerie.getId())) {
            throw new BusinessException(
                    "Un client avec ce numéro '" + request.getPhone() + "' existe déjà dans cette poissonnerie");
        }

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        Client client = clientMapper.toEntity(request);
        client.setPoissonnerie(poissonnerie);
        client.setCreatedBy(createdBy);
        client.setActive(true);

        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);
    }

    public Page<ClientResponse> getClientsByPoissonnerie(Long poissonnerieId, Pageable pageable) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));

        Page<Client> clientsPage = clientRepository.findByPoissonnerieAndActiveTrue(poissonnerie, pageable);
        return clientsPage.map(clientMapper::toResponse);
    }

    public ClientDetailResponse getClientDetail(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le client non trouvé avec l'id : " + clientId));

        ClientDetailResponse detail = clientMapper.toDetailResponse(client);

        BigDecimal soldeCompteCourant = compteCourantRepository.findByClient(client)
                .map(compte -> compte.getSolde())
                .orElse(BigDecimal.ZERO);

        BigDecimal soldeEpargne = epargneRepository.findByClient(client)
                .map(epargne -> epargne.getCurrentBalance())
                .orElse(BigDecimal.ZERO);

        detail.setSoldeCompteCourant(soldeCompteCourant);
        detail.setSoldeEpargne(soldeEpargne);

        return detail;
    }

    public Page<ClientResponse> searchClients(Long poissonnerieId, String searchTerm, Pageable pageable) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));

        Page<Client> result;
        if (searchTerm == null || searchTerm.trim().isBlank()) {
            result = clientRepository.findByPoissonnerieAndActiveTrue(poissonnerie, pageable);
        } else {
            String term = searchTerm.trim();
            result = clientRepository.findByPoissonnerieAndActiveTrueAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    poissonnerie, term, term, pageable);
        }
        return result.map(clientMapper::toResponse);
    }

    @LogAudit(action = "UPDATE", entityName = "Client")
    @Transactional
    public ClientResponse updateClient(Long clientId, UpdateClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le client non trouvé avec l'id : " + clientId));

        Poissonnerie poissonnerie = client.getPoissonnerie();

        if (request.getPhone() != null && !request.getPhone().equals(client.getPhone())) {
            if (existsByPhoneAndPoissonnerie(request.getPhone(), poissonnerie.getId())) {
                throw new BusinessException(
                        "Un client avec ce numéro '" + request.getPhone() +
                                "' existe déjà dans cette poissonnerie");
            }
        }
        clientMapper.updateEntityFromRequest(request, client);
        return clientMapper.toResponse(client);
    }

    @LogAudit(action = "DELETE", entityName = "Client")
    @Transactional
    public void deleteClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le client non trouvé avec l'id : " + clientId));

        client.setActive(false);
        clientRepository.save(client);
    }

    public boolean existsByPhoneAndPoissonnerie(String phone, Long poissonnerieId) {
        return poissonnerieRepository.findById(poissonnerieId)
                .map(p -> clientRepository.findByPhoneAndPoissonnerie(phone, p).isPresent())
                .orElse(false);
    }

    @LogAudit(action = "ACTIVE_REACTIVE", entityName = "Client")
    @Transactional
    public ClientResponse reactivateClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client non trouvé avec l'id : " + clientId));

        if (!client.getActive()) {
            client.setActive(true);
        }
        clientRepository.save(client);
        return clientMapper.toResponse(client);
    }

    public Page<ClientResponse> getInactiveClients(Long poissonnerieId, Pageable pageable) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));

        Page<Client> page = clientRepository.findByPoissonnerieAndActiveFalse(poissonnerie, pageable);
        return page.map(clientMapper::toResponse);
    }
}