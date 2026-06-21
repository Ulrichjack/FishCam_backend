package com.fishcam.domain.comptecourant;

import com.fishcam.adapter.web.dto.response.TopDebiteurResponse;
import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // VERROUILLAGE PESSIMISTE POUR LES TRANSACTIONS FINANCIÈRES
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompteCourant c WHERE c.id = :id")
    Optional<CompteCourant> findByIdWithLock(@Param("id") Long id);


    @Query("SELECT cc FROM CompteCourant cc WHERE cc.client.poissonnerie = :poissonnerie AND cc.solde < :seuil ORDER BY cc.solde ASC")
    List<CompteCourant> findComptesEnDette(@Param("poissonnerie") Poissonnerie poissonnerie, @Param("seuil") BigDecimal seuil);

    @Query("SELECT COALESCE(SUM(ABS(cc.solde)), 0) FROM CompteCourant cc WHERE cc.client.poissonnerie  = :poissonnerie AND cc.solde < 0")
    BigDecimal sumTotalDettes(@Param("poissonnerie") Poissonnerie poissonnerie);

    @Query("SELECT COUNT(cc) FROM CompteCourant cc WHERE cc.client.poissonnerie  = :poissonnerie AND cc.solde < 0")
    Long countComptesEnDette(@Param("poissonnerie") Poissonnerie poissonnerie);

    @Query("SELECT new com.fishcam.adapter.web.dto.response.TopDebiteurResponse(c.client.lastName, c.client.phone, c.solde) " +
            "FROM CompteCourant c " +
            "WHERE c.client.poissonnerie.id = :poissonnerieId AND c.solde < 0 " +
            "ORDER BY c.solde ASC")
    List<TopDebiteurResponse> findTopDebiteursByPoissonnerie(@Param("poissonnerieId") Long poissonnerieId, Pageable pageable);
}