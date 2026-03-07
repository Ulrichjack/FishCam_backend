package com.fishcam.domain.achat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LigneAchatRepository  extends JpaRepository<LigneAchat,Long> {

    @Query("SELECT la FROM LigneAchat la " +
            "JOIN la.achatJournalier aj " +
            "WHERE la.produit.id = :produitId " +
            "AND aj.poissonnerie.id = :poissonnerieId " +
            "ORDER BY la.id DESC LIMIT 1")
    Optional<LigneAchat> findDernierPrix(Long produitId, Long poissonnerieId);

}
