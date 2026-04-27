package com.fishcam.domain.fournisseur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {


    List<Fournisseur> findByActifTrue();

    boolean existsByNomIgnoreCase(String nom);
}
