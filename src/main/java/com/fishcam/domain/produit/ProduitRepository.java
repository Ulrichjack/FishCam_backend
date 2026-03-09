package com.fishcam.domain.produit;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    List<Produit> findByNomContainingIgnoreCaseAndActifTrue(String nom);

    // Vérifier si nom existe déjà
    boolean existsByNom(String nom);


    Page<Produit> findByActifTrue(Pageable pageable);

}
