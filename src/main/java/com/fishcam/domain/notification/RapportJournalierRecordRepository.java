package com.fishcam.domain.notification;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RapportJournalierRecordRepository extends JpaRepository<RapportJournalierRecord, Long> {

    boolean existsByPoissonnerieAndDateRapport(Poissonnerie poissonnerie, LocalDate dateRapport);

    List<RapportJournalierRecord> findByPoissonnerieOrderByDateRapportDesc(Poissonnerie poissonnerie);


}