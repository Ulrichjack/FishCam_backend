package com.fishcam.domain.epargne;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEpargneRepository extends JpaRepository<TransactionEpargne, Long> {

    //historique d'une epargne
    List<TransactionEpargne> findByEpargne(Epargne epargne);

    //historique trie
    List<TransactionEpargne> findByEpargneOrderByTransactionDateDesc(Epargne epargne);

    //tous les depots ou tout les retraits
    List<TransactionEpargne> findBytype (TypeTransactionEpargne type);

    //toutes les transaction d'une poissonnerie
    List<TransactionEpargne> findByPoissonnerie (Poissonnerie poissonnerie);

}
