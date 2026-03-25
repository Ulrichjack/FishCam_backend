package com.fishcam.domain.bilan;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BilanMensuelRepository extends JpaRepository<BilanMensuel, Long> {

    boolean existsByPoissonnerieAndMoisAndAnnee(Poissonnerie p, Integer mois, Integer annee);

    Optional<BilanMensuel> findByPoissonnerieAndMoisAndAnnee(Poissonnerie p, Integer mois, Integer annee);

    List<BilanMensuel> findByPoissonnerieOrderByAnneeDescMoisDesc(Poissonnerie poissonnerie);


}
