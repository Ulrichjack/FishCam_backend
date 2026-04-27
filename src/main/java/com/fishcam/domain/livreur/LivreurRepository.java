package com.fishcam.domain.livreur;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LivreurRepository extends JpaRepository<Livreur, Long> {

    boolean existsByTelephone(String telephone);
    boolean existsByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);
}
