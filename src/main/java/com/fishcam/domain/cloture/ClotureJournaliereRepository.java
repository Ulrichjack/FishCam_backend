package com.fishcam.domain.cloture;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClotureJournaliereRepository
        extends JpaRepository<ClotureJournaliere, Long> {

    // Check if cloture already exists for this boutique on this date
    boolean existsByPoissonnerieAndDate(
            Poissonnerie poissonnerie, LocalDate date);

    // Get cloture for a specific boutique and date
    Optional<ClotureJournaliere> findByPoissonnerieAndDate(
            Poissonnerie poissonnerie, LocalDate date);

    // Get history for a boutique ordered by most recent first
    List<ClotureJournaliere> findByPoissonnerieOrderByDateDesc(
            Poissonnerie poissonnerie);

    @Query("SELECT c FROM ClotureJournaliere c " +
            "WHERE c.poissonnerie = :poissonnerie " +
            "AND EXTRACT(MONTH FROM c.date) = :mois " +
            "AND EXTRACT(YEAR  FROM c.date) = :annee " +
            "ORDER BY c.date ASC")
    List<ClotureJournaliere> findByPoissonnerieAndMoisAndAnnee(
            @Param("poissonnerie") Poissonnerie poissonnerie,
            @Param("mois") Integer mois,
            @Param("annee") Integer annee
    );

    List<ClotureJournaliere> findByPoissonnerieAndDateBetweenOrderByDateAsc(
            Poissonnerie poissonnerie,
            LocalDate start,
            LocalDate end
    );



}