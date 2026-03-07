package com.fishcam.domain.achat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AchatJournalierRepository extends JpaRepository<AchatJournalier, Long> {

    List<AchatJournalier> findByPoissonnerieIdAndDateAchat(Long poissonnerieId, LocalDate date);

}
