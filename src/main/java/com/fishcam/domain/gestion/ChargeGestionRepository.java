package com.fishcam.domain.gestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargeGestionRepository extends JpaRepository<ChargeGestion, Long> {

    List<ChargeGestion> findAllByOrderByDateDebutAscIdAsc();

    /**
     * Charges d'une même catégorie sur un même périmètre : une boutique précise,
     * ou les charges générales lorsque {@code poissonnerieId} est nul.
     */
    @Query("SELECT c FROM ChargeGestion c WHERE c.categorie = :categorie AND ("
            + "(:poissonnerieId IS NULL AND c.poissonnerie IS NULL) "
            + "OR (:poissonnerieId IS NOT NULL AND c.poissonnerie.id = :poissonnerieId))")
    List<ChargeGestion> findByCategorieAndPerimetre(
            @Param("categorie") CategorieCharge categorie,
            @Param("poissonnerieId") Long poissonnerieId);
}
