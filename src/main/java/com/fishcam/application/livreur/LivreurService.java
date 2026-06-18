package com.fishcam.application.livreur;

import com.fishcam.adapter.web.dto.request.CreateLivreurRequest;
import com.fishcam.adapter.web.dto.request.UpdateLivreurRequest;
import com.fishcam.adapter.web.dto.response.LivreurResponse;
import com.fishcam.adapter.web.mapper.LivreurMapper;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import com.fishcam.domain.livreur.EvaluationLivreurRepository;
import com.fishcam.domain.livreur.Livreur;
import com.fishcam.domain.livreur.LivreurRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LivreurService {

    private final FournisseurRepository fournisseurRepository;
    private final LivreurRepository livreurRepository;
    private final EvaluationLivreurRepository evaluationLivreurRepository;
    private final LivreurMapper livreurMapper;

    @LogAudit(action = "CREATE", entityName = "Livreur")
    @Transactional
    public LivreurResponse createLivreur(CreateLivreurRequest request){

        if (request.getTelephone() != null && livreurRepository.existsByTelephone(request.getTelephone())) {
            throw new BusinessException("Un livreur avec ce numéro de téléphone existe déjà.");
        }
        if (livreurRepository.existsByNomIgnoreCaseAndPrenomIgnoreCase(request.getNom(), request.getPrenom())) {
            throw new BusinessException("Un livreur avec ce nom et prénom existe déjà.");
        }

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(()-> new ResourceNotFoundException("Fournisseur non trouvé"));

        Livreur livreur = livreurMapper.toEntity(request,fournisseur);

        Livreur saved = livreurRepository.save(livreur);
        return livreurMapper.toResponse(saved);
    }

    public List<LivreurResponse> getAllLivreurs(){
        return livreurRepository.findAll()
                .stream()
                .map(livreurMapper::toResponse)
                .toList();
    }

    @Transactional
    public LivreurResponse toggleStatut(Long id){
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livreur non trouvée avec l'id : " + id));

       livreur.setActif(!livreur.getActif());
       Livreur saved = livreurRepository.save(livreur);
       return livreurMapper.toResponse(saved);
    }

    public LivreurResponse getLastLivreurUtilise() {
        return evaluationLivreurRepository.findFirstByOrderByCreatedAtDesc()
                .map(evaluation -> livreurMapper.toResponse(evaluation.getLivreur()))
                .orElse(null);
    }

    @LogAudit(action = "UPDATE", entityName = "Livreur")
    @Transactional
    public LivreurResponse updateLivreur(Long id, UpdateLivreurRequest request) {
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'id : " + id));

        // Vérification du téléphone s'il change
        if (request.getTelephone() != null && !request.getTelephone().equals(livreur.getTelephone())) {
            if (livreurRepository.existsByTelephone(request.getTelephone())) {
                throw new BusinessException("Un livreur avec ce numéro de téléphone existe déjà.");
            }
        }

        // Mise à jour du fournisseur si fourni
        if (request.getFournisseurId() != null &&
                (livreur.getFournisseur() == null || !livreur.getFournisseur().getId().equals(request.getFournisseurId()))) {
            Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé"));
            livreur.setFournisseur(fournisseur);
        }

        livreurMapper.updateEntityFromRequest(request, livreur);
        Livreur saved = livreurRepository.save(livreur);
        return livreurMapper.toResponse(saved);
    }


}
