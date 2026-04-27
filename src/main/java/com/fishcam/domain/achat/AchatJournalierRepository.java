package com.fishcam.domain.achat;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AchatJournalierRepository extends JpaRepository<AchatJournalier, Long> {

    List<AchatJournalier> findByPoissonnerieIdAndDateAchat(Long poissonnerieId, LocalDate date);

    Optional<AchatJournalier> findByPoissonnerieAndDateAchat(Poissonnerie poissonnerie, LocalDate date);

    List<AchatJournalier> findByPoissonnerieAndDateAchatBetweenOrderByDateAchat(
            Poissonnerie poissonnerie, LocalDate debut, LocalDate fin);



}
