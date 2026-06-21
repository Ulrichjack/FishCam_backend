package com.fishcam.domain.epargne;

import com.fishcam.domain.client.Client;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface EpargneRepository extends JpaRepository<Epargne, Long> {

    //L'épargne d'un client (0 ou 1 maximum)
    Optional<Epargne> findByClient(Client client);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Epargne e WHERE e.id = :id")
    Optional<Epargne> findByIdWithLock(@Param("id") Long id);

    //Vérifie si un client a déjà une épargne
    boolean existsByClient(Client client);

    @Query("SELECT COUNT(e) FROM Epargne e WHERE e.client.poissonnerie.id = :poissonnerieId")
    Integer countByPoissonnerieId(@Param("poissonnerieId") Long poissonnerieId);

    @Query("SELECT SUM(e.currentBalance) FROM Epargne e WHERE e.client.poissonnerie.id = :poissonnerieId")
    BigDecimal sumSoldeByPoissonnerieId(@Param("poissonnerieId") Long poissonnerieId);

}
