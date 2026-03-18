package com.fishcam.domain.produit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    List<Produit> findByNomContainingIgnoreCaseAndActifTrue(String nom);

    // Vérifier si nom existe déjà
    boolean existsByNomIgnoreCase(String nom);

    Optional<Produit> findByNomIgnoreCase(String nom);


    Page<Produit> findByActifTrue(Pageable pageable);

}
