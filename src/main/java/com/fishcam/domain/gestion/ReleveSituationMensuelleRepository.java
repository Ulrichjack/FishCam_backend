package com.fishcam.domain.gestion;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReleveSituationMensuelleRepository extends JpaRepository<ReleveSituationMensuelle, Long> {
    Optional<ReleveSituationMensuelle> findByPoissonnerieAndDateReleve(
            Poissonnerie poissonnerie, LocalDate dateReleve);

    Optional<ReleveSituationMensuelle> findFirstByPoissonnerieAndDateReleveBeforeOrderByDateReleveDesc(
            Poissonnerie poissonnerie, LocalDate date);

    Optional<ReleveSituationMensuelle> findFirstByPoissonnerieAndDateReleveBetweenOrderByDateReleveDesc(
            Poissonnerie poissonnerie, LocalDate debut, LocalDate fin);

    List<ReleveSituationMensuelle> findByPoissonnerieOrderByDateReleveDesc(Poissonnerie poissonnerie);
}
