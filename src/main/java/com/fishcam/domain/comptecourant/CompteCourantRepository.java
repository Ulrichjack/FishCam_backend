package com.fishcam.domain.comptecourant;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompteCourantRepository extends JpaRepository<CompteCourant, Long> {

    Optional<CompteCourant> findByClient(Client client);

    boolean existsByClient(Client client);

    List<CompteCourant> findByPoissonnerie(Poissonnerie poissonnerie);

    List<CompteCourant> findByPoissonnerieAndStatut(Poissonnerie poissonnerie, StatutCompteCourant statut);

    @Query("SELECT cc FROM CompteCourant cc WHERE cc.poissonnerie = :poissonnerie AND cc.solde < :seuil ORDER BY cc.solde ASC")
    List<CompteCourant> findComptesEnDette(@Param("poissonnerie") Poissonnerie poissonnerie, @Param("seuil") BigDecimal seuil);

    @Query("SELECT COALESCE(SUM(ABS(cc.solde)), 0) FROM CompteCourant cc WHERE cc.poissonnerie = :poissonnerie AND cc.solde < 0")
    BigDecimal sumTotalDettes(@Param("poissonnerie") Poissonnerie poissonnerie);

    @Query("SELECT COUNT(cc) FROM CompteCourant cc WHERE cc.poissonnerie = :poissonnerie AND cc.solde < 0")
    Long countComptesEnDette(@Param("poissonnerie") Poissonnerie poissonnerie);
}