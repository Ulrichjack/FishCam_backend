package com.fishcam.domain.dette;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionDetteRepository extends JpaRepository<TransactionDette, Long> {

    //Historique des remboursements d'une dette
    List<TransactionDette> findByDette(Dette dette);

    //Historique trié par date décroissante (plus récent en premier)
    List<TransactionDette> findByDetteOrderByTransactionDateDesc(Dette dette);

    //Tous les remboursements d'une poissonnerie
    List<TransactionDette> findByPoissonnerie(Poissonnerie poissonnerie);

    //Remboursements entre deux dates
    //Pour rapports mensuels
    List<TransactionDette> findByTransactionDateBetween (Poissonnerie poissonnerie);

    //Requte sommes des remboursements
    @Query("SELECT SUM(t.amount) FROM TransactionDette t WHERE t.poissonnerie = :poissonnerie AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumAmountByPoissonnerieAndPeriod(@Param("poissonnerie") Poissonnerie poissonnerie, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
