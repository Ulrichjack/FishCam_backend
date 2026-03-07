package com.fishcam.domain.epargne;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEpargneRepository extends JpaRepository<TransactionEpargne, Long> {


    List<TransactionEpargne> findByEpargne(Epargne epargne);


    List<TransactionEpargne> findByEpargneOrderByTransactionDateDesc(Epargne epargne);

    //tous les depots ou tout les retraits
    List<TransactionEpargne> findByType (TypeTransactionEpargne type);


}
