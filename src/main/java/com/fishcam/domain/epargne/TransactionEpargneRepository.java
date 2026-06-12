package com.fishcam.domain.epargne;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEpargneRepository extends JpaRepository<TransactionEpargne, Long> {


    List<TransactionEpargne> findByPoissonnerieOrderByTransactionDateDesc(Poissonnerie poissonnerie);

    List<TransactionEpargne> findByEpargneOrderByTransactionDateDesc(Epargne epargne);


}
