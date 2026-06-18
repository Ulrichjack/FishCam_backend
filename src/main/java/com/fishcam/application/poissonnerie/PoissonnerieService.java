package com.fishcam.application.poissonnerie;

import com.fishcam.adapter.web.dto.request.CreatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.request.UpdatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.response.PoissonnerieResponse;
import com.fishcam.adapter.web.mapper.PoissonnerieMapper;
import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoissonnerieService {

    private final PoissonnerieRepository poissonnerieRepository;
    private final PoissonnerieMapper poissonnerieMapper;
    private final NotificationService notificationService;


    @LogAudit(action = "CREATE", entityName = "Poissonnerie")
    @Transactional
    public PoissonnerieResponse createPoissonnerie(CreatePoissonnerieRequest request) {
        String nom = request.getName().trim();
        if (poissonnerieRepository.existsByNameIgnoreCase(nom)) {
            throw new BusinessException("Une poissonnerie avec ce nom '" + nom + "' existe déjà");
        }
        Poissonnerie poissonnerie = poissonnerieMapper.toEntity(request);
        poissonnerie.setActive(true);
        poissonnerieRepository.save(poissonnerie);
        return poissonnerieMapper.toResponse(poissonnerie);
    }

    public Page<PoissonnerieResponse> getAllPoissonneries(Pageable pageable) {
        Page<Poissonnerie> page = poissonnerieRepository.findAll(pageable);
        return page.map(poissonnerieMapper::toResponse);
    }

    public PoissonnerieResponse getPoissonnerieById(Long id) {
        Poissonnerie poissonnerie = getEntityById(id);
        return poissonnerieMapper.toResponse(poissonnerie);
    }

    @LogAudit(action = "UPDATE", entityName = "Poissonnerie")
    @Transactional
    public PoissonnerieResponse updatePoissonnerie(Long id, UpdatePoissonnerieRequest request) {
        Poissonnerie poissonnerie = getEntityById(id);
        if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(poissonnerie.getName())) {
            if (poissonnerieRepository.existsByNameIgnoreCase(request.getName().trim())) {
                throw new BusinessException("Une poissonnerie avec ce nom '" + request.getName().trim() + "' existe déjà");
            }
        }
        poissonnerieMapper.updateEntityFromRequest(request, poissonnerie);
        return poissonnerieMapper.toResponse(poissonnerie);

    }

    @LogAudit(action = "DELETE", entityName = "Poissonnerie")
    @Transactional
    public void deletePoissonnerie(Long poissonerieId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La Poissonnerie non trouve avec l'id : " + poissonerieId
                ));
        poissonnerie.setActive(false);
        poissonnerieRepository.save(poissonnerie);
    }


    private Poissonnerie getEntityById(Long id) {
        return poissonnerieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée avec l'id : " + id));
    }

    @LogAudit(action = "CLOTURE", entityName = "Poissonnerie")
    @Transactional
    public void cloturerJournee(Long poissonnerieId) {
        Poissonnerie poissonnerie = getEntityById(poissonnerieId); // privée, utilisable ici
        notificationService.createRapportJournalier(poissonnerie);
    }

    @LogAudit(action = "REACTIVATE", entityName = "Poissonnerie")
    @Transactional
    public PoissonnerieResponse reactivatePoissonnerie (Long poissonnerieId){
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvé avec l'id : " + poissonnerieId));
        poissonnerie.setActive(true);
        Poissonnerie saved = poissonnerieRepository.save(poissonnerie);
        return poissonnerieMapper.toResponse(saved);
    }
}
