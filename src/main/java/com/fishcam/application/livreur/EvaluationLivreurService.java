package com.fishcam.application.livreur;

import com.fishcam.adapter.web.dto.request.CreateEvaluationRequest;
import com.fishcam.adapter.web.dto.response.EvaluationLivreurResponse;
import com.fishcam.adapter.web.mapper.EvaluationLivreurMapper;
import com.fishcam.domain.achat.AchatJournalier;
import com.fishcam.domain.achat.AchatJournalierRepository;
import com.fishcam.domain.livreur.EvaluationLivreur;
import com.fishcam.domain.livreur.EvaluationLivreurRepository;
import com.fishcam.domain.livreur.Livreur;
import com.fishcam.domain.livreur.LivreurRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationLivreurService {

    private final EvaluationLivreurRepository evaluationLivreurRepository;
    private final AchatJournalierRepository achatJournalierRepository;
    private final LivreurRepository livreurRepository;
    private final UserRepository userRepository;
    private final EvaluationLivreurMapper evaluationLivreurMapper;

    @LogAudit(action = "CREATE", entityName = "EvaluationLivreur")
    @Transactional
    public EvaluationLivreurResponse createEvaluation(CreateEvaluationRequest request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        AchatJournalier achatJournalier = achatJournalierRepository.findById(request.getAchatJournalierId())
                .orElseThrow(() -> new ResourceNotFoundException("Achat journalier non trouvé"));

        Livreur livreur = livreurRepository.findById(request.getLivreurId())
                .orElseThrow(() -> new ResourceNotFoundException("livreur non trouvé"));

        if (evaluationLivreurRepository
                .existsByAchatJournalier(achatJournalier)){
            throw new BusinessException("Une évaluation existe déjà pour cet achat.");
        }
        EvaluationLivreur evaluationLivreur = evaluationLivreurMapper.toEntity(request, livreur, user, achatJournalier);
        evaluationLivreur.setDateEvaluation(LocalDate.now());
        EvaluationLivreur saved = evaluationLivreurRepository.save(evaluationLivreur);
        return evaluationLivreurMapper.toResponse(saved);
    }

    public List<EvaluationLivreurResponse> getEvaluationsByLivreur(Long livreurId){
        Livreur livreur = livreurRepository.findById(livreurId)
                .orElseThrow(() -> new ResourceNotFoundException("livreur non trouvé"));
        List <EvaluationLivreur> evaluationLivreur = evaluationLivreurRepository.findByLivreurOrderByDateEvaluationDesc(livreur);

        return evaluationLivreur.stream()
                .map(evaluationLivreurMapper::toResponse)
                .toList();
    }

    public EvaluationLivreurResponse getEvaluationByFacture(Long factureId) {
        return evaluationLivreurRepository.findByAchatJournalierId(factureId)
                .map(evaluationLivreurMapper::toResponse)
                .orElse(null); // Retourne null si pas d'évaluation
    }



}
