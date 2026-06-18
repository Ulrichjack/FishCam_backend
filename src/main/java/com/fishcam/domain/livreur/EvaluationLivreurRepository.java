package com.fishcam.domain.livreur;

import com.fishcam.domain.achat.AchatJournalier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationLivreurRepository extends JpaRepository <EvaluationLivreur, Long> {


    boolean existsByAchatJournalier(AchatJournalier achatJournalier);

    List<EvaluationLivreur> findByLivreurOrderByDateEvaluationDesc(Livreur  livreur);

    Optional<EvaluationLivreur> findFirstByOrderByCreatedAtDesc();

    Optional<EvaluationLivreur> findByAchatJournalierId(Long achatJournalierId);
}
