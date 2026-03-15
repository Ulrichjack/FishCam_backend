package com.fishcam.domain.achat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    @Query("SELECT l FROM LigneAchat l WHERE l.produit.id = :produitId " +
            "AND l.achatJournalier.poissonnerie.id = :poissonnerieId " +
            "ORDER BY l.id DESC LIMIT 1")
    Optional<LigneAchat> findDernierPrix(
            @Param("produitId") Long produitId,
            @Param("poissonnerieId") Long poissonnerieId
    );

    List<LigneAchat> findByAchatJournalier(AchatJournalier achatJournalier);

}
