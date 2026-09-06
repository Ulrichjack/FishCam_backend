package com.fishcam.application.gestion;

import com.fishcam.adapter.web.dto.request.SaveReleveSituationRequest;
import com.fishcam.adapter.web.dto.response.ReleveSituationResponse;
import com.fishcam.domain.gestion.ReleveSituationMensuelle;
import com.fishcam.domain.gestion.ReleveSituationMensuelleRepository;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleveSituationMensuelleService {

    private final ReleveSituationMensuelleRepository releveRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;

    @Transactional
    @LogAudit(action = "SAVE", entityName = "ReleveSituationMensuelle")
    public ReleveSituationResponse enregistrer(SaveReleveSituationRequest request, Long userId) {
        if (request.getDateReleve().isAfter(LocalDate.now())) {
            throw new BusinessException("Impossible d'enregistrer un relevé dans le futur.");
        }
        if (!request.getDateReleve().equals(YearMonth.from(request.getDateReleve()).atEndOfMonth())) {
            throw new BusinessException("Le relevé doit être daté du dernier jour du mois.");
        }

        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        ReleveSituationMensuelle releve = releveRepository
                .findByPoissonnerieAndDateReleve(poissonnerie, request.getDateReleve())
                .orElseGet(ReleveSituationMensuelle::new);

        releve.setPoissonnerie(poissonnerie);
        releve.setDateReleve(request.getDateReleve());
        releve.setValeurStock(request.getValeurStock());
        releve.setTotalCreancesClients(request.getTotalCreancesClients());
        releve.setModeEvaluation(request.getModeEvaluation());
        releve.setNote(request.getNote());
        releve.setSaisiPar(user);
        return toResponse(releveRepository.save(releve));
    }

    public List<ReleveSituationResponse> lister(Long poissonnerieId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
        return releveRepository.findByPoissonnerieOrderByDateReleveDesc(poissonnerie)
                .stream().map(this::toResponse).toList();
    }

    private ReleveSituationResponse toResponse(ReleveSituationMensuelle releve) {
        String saisiPar = releve.getSaisiPar().getFirstName() + " " + releve.getSaisiPar().getLastName();
        return new ReleveSituationResponse(
                releve.getId(),
                releve.getPoissonnerie().getId(),
                releve.getPoissonnerie().getName(),
                releve.getDateReleve(),
                releve.getValeurStock(),
                releve.getTotalCreancesClients(),
                releve.getModeEvaluation(),
                releve.getNote(),
                saisiPar.trim(),
                releve.getCreatedAt(),
                releve.getUpdatedAt()
        );
    }
}
