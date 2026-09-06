package com.fishcam.domain.comptecourant;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCompteCourantRepository extends JpaRepository<TransactionCompteCourant, Long> {

    List<TransactionCompteCourant> findByCompteCourantOrderByTransactionDateDesc(CompteCourant compteCourant);

    List<TransactionCompteCourant> findByPoissonnerieOrderByTransactionDateDesc(Poissonnerie poissonnerie);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransactionCompteCourant t JOIN FETCH t.compteCourant WHERE t.id = :id")
    Optional<TransactionCompteCourant> findByIdWithLock(@Param("id") Long id);

    boolean existsByTransactionOrigineAndType(
            TransactionCompteCourant transactionOrigine, TypeTransactionCC type);

    @Query("""
            SELECT t FROM TransactionCompteCourant t
            WHERE t.poissonnerie = :poissonnerie
              AND (
                (t.type IN :typesAvecDateOrigine
                  AND t.dateDetteOrigine IS NOT NULL
                  AND t.dateDetteOrigine <= :dateSituation)
                OR
                ((t.type NOT IN :typesAvecDateOrigine OR t.dateDetteOrigine IS NULL)
                  AND t.transactionDate < :finExclusive)
              )
            """)
    List<TransactionCompteCourant> findMouvementsEffectifsAu(
            @Param("poissonnerie") Poissonnerie poissonnerie,
            @Param("dateSituation") LocalDate dateSituation,
            @Param("finExclusive") LocalDateTime finExclusive,
            @Param("typesAvecDateOrigine") Collection<TypeTransactionCC> typesAvecDateOrigine);

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
