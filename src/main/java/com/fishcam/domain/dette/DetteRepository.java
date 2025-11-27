package com.fishcam.domain.dette;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DetteRepository extends JpaRepository<Dette, Long> {

    //Toutes les dettes d'un client
    List<Dette> findByClient(Client client);

    //Toutes les dettes d'une poissonnerie
    List<Dette> findByPoissonnerie (Poissonnerie poissonnerie);

    //Dettes actives ou soldées
    List<Dette> findByStatut (StatutDette statut);

    //Ex: dettes actives de AKWA
    List<Dette> findByPoissonnerieAndStatut(Poissonnerie poissonnerie, StatutDette statut);

    //Dettes supérieures à un montant
    //Pour alertes > 5000 FCFA
    List<Dette> findByRemaningAmountGreaterThan(BigDecimal amout);

    //Combine poissonnerie + montant
    List<Dette> findByPoissonnerieAndRemainningAmountGreaterThan(Poissonnerie poissonnerie, BigDecimal amount);

    //gere les grosses dettes
    @Query("SELECT d FROM Dette d WHERE d.poissonnerie = :poissonnerie AND d.statut = :statut ORDER BY d.remainingAmount DESC")
    List<Dette> findTopDebtors(@Param("poissonnerie") Poissonnerie poissonnerie, @Param("statut") StatutDette statut, Pageable pageable);

    //determiner total des dettes d'une poissonnerie
    @Query("SELECT SUM(d.remainingAmount) FROM Dette d WHERE d.poissonnerie = :poissonnerie AND d.statut = 'ACTIVE'")
    BigDecimal sumRemainingAmountByPoissonnerie(@Param("poissonnerie") Poissonnerie poissonnerie);


}
