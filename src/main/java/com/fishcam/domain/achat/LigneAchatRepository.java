package com.fishcam.domain.achat;

import com.fishcam.adapter.web.dto.response.TopProduitRentableResponse;
import com.fishcam.adapter.web.dto.response.TopProduitResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    Optional<LigneAchat> findFirstByProduitIdAndAchatJournalierPoissonnerieIdOrderByIdDesc(
            Long produitId, Long poissonnerieId);

    List<LigneAchat> findByAchatJournalier(AchatJournalier achatJournalier);


    @Query("SELECT SUM(la.montantCarton) FROM LigneAchat la WHERE la.achatJournalier.id = :factureId")
    BigDecimal calculateTotalAchatByFactureId(@Param("factureId") Long factureId);

    @Query("SELECT l FROM LigneAchat l WHERE l.produit.id = :produitId " +
            "AND l.achatJournalier.poissonnerie.id = :poissonnerieId " +
            "ORDER BY l.createdAt DESC")
    List<LigneAchat> findLatestPricesByProduitAndPoissonnerie(
            @Param("produitId") Long produitId,
            @Param("poissonnerieId") Long poissonnerieId,
            Pageable pageable);

    @Query("SELECT new com.fishcam.adapter.web.dto.response.TopProduitResponse(p.nom, SUM(l.quantiteCartons), SUM(l.montantCarton)) " +
            "FROM LigneAchat l JOIN l.produit p " +
            "WHERE l.achatJournalier.poissonnerie.id = :poissonnerieId " +
            "GROUP BY p.id, p.nom " +
            "ORDER BY SUM(l.quantiteCartons) DESC")
    List<TopProduitResponse> findTopProduitsByPoissonnerie(@Param("poissonnerieId") Long poissonnerieId, Pageable pageable);

    @Query("SELECT new com.fishcam.adapter.web.dto.response.TopProduitRentableResponse(p.nom, SUM((l.prixVenteKilo * l.poidsKg) - l.montantCarton)) " +
            "FROM LigneAchat l JOIN l.produit p " +
            "WHERE l.achatJournalier.poissonnerie.id = :poissonnerieId " +
            "GROUP BY p.id, p.nom " +
            "ORDER BY SUM((l.prixVenteKilo * l.poidsKg) - l.montantCarton) DESC")
    List<TopProduitRentableResponse> findTopProduitsRentablesByPoissonnerie(@Param("poissonnerieId") Long poissonnerieId, Pageable pageable);





}
