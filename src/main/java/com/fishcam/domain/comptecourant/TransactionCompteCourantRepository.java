package com.fishcam.domain.comptecourant;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionCompteCourantRepository extends JpaRepository<TransactionCompteCourant, Long> {

    List<TransactionCompteCourant> findByCompteCourantOrderByTransactionDateDesc(CompteCourant compteCourant);

    List<TransactionCompteCourant> findByPoissonnerieOrderByTransactionDateDesc(Poissonnerie poissonnerie);

//    @Query("SELECT SUM(t.montant) FROM TransactionCompteCourant t WHERE t.compteCourant = :compte AND t.type = :type")
//    BigDecimal sumMontantByCompteCourantAndType(
//            @Param("compte") CompteCourant compte,
//            @Param("type") TypeTransactionCC type
//    );

    @Query("SELECT COUNT(t) FROM TransactionCompteCourant t WHERE t.poissonnerie = :poissonnerie AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    Long countByPoissonnerieAndTypeAndPeriod(
            @Param("poissonnerie") Poissonnerie poissonnerie,
            @Param("type") TypeTransactionCC type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(t.montant) FROM TransactionCompteCourant t WHERE t.poissonnerie = :poissonnerie AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumMontantByPoissonnerieAndTypeAndPeriod(
            @Param("poissonnerie") Poissonnerie poissonnerie,
            @Param("type") TypeTransactionCC type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}