package com.fishcam.application.fournisseur;

import com.fishcam.adapter.web.dto.request.CreateFournisseurRequest;
import com.fishcam.adapter.web.dto.request.UpdateFournisseurRequest;
import com.fishcam.adapter.web.dto.response.FournisseurResponse;
import com.fishcam.adapter.web.mapper.FournisseurMapper;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;


    @Transactional
    public FournisseurResponse createFournisseur(CreateFournisseurRequest request){
        Fournisseur fournisseur = fournisseurMapper.toEntity(request);
        fournisseur.setActif(true);

        if (fournisseurRepository.existsByNom(request.getNom())) {
            throw new BusinessException("Un fournisseur avec ce nom existe déjà");
        }

        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        return fournisseurMapper.toResponse(savedFournisseur);
    }

    public List<FournisseurResponse> getAllFournisseurs() {
        return fournisseurRepository.findByActifTrue()
                .stream()
                .map(fournisseurMapper::toResponse)
                .toList();
    }

    @Transactional
    public FournisseurResponse updateFournisseur(Long fournisseurId, UpdateFournisseurRequest request){
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvée avec l'id : " + fournisseurId));

        if (request.getNom() != null && !request.getNom().trim().isBlank()) {
            fournisseur.setNom(request.getNom().trim());
        }

        if (request.getVille() != null && !request.getVille().trim().isBlank()) {
            fournisseur.setVille(request.getVille().trim());
        }

        if (request.getTelephone() != null && !request.getTelephone().trim().isBlank()) {
            fournisseur.setTelephone(request.getTelephone().trim());
        }
        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        return fournisseurMapper.toResponse(savedFournisseur);
    }


    public FournisseurResponse getFournisseurById(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvé avec l'id : " + id));
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Transactional
    public void deleteFournisseur(Long fournisseurId){
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fournisseur non trouvée avec l'id : " + fournisseurId));
        fournisseur.setActif(false);
        fournisseurRepository.save(fournisseur);

    }

}
