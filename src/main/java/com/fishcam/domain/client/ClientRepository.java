package com.fishcam.domain.client;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByCni(String cni);

    // Pour l'update : vérifier si la CNI existe pour un AUTRE client
    boolean existsByCniAndIdNot(String cni, Long id);

    // Version paginée – LA PLUS IMPORTANTE pour ton API
    Page<Client> findByPoissonnerieAndActiveTrue(Poissonnerie poissonnerie, Pageable pageable);


    Optional<Client> findByPhoneAndPoissonnerie(String phone, Poissonnerie poissonnerie);


    long countByPoissonnerie(Poissonnerie poissonnerie);

    //Recherche par nom ou prénom (contient, insensible à la casse)
    // Recherche UNIQUEMENT par prénom ou nom (insensible à la casse)
    @Query("SELECT c FROM Client c WHERE c.poissonnerie = :poissonnerie AND c.active = true AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<Client> searchByTerm(@Param("poissonnerie") Poissonnerie poissonnerie, @Param("term") String term, Pageable pageable);


    // Liste paginée des clients inactifs d'une poissonnerie
    Page<Client> findByPoissonnerieAndActiveFalse(Poissonnerie poissonnerie, Pageable pageable);
}
