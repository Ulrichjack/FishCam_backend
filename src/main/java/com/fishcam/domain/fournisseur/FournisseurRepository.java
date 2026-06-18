package com.fishcam.domain.fournisseur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {


    List<Fournisseur> findByActifTrue();

    boolean existsByNomIgnoreCase(String nom);

    @Query("SELECT f FROM Fournisseur f WHERE LOWER(f.nom) LIKE LOWER(CONCAT('%', :term, '%')) OR f.telephone LIKE CONCAT('%', :term, '%')")
    List<Fournisseur> searchByTerm(@Param("term") String term);


}
