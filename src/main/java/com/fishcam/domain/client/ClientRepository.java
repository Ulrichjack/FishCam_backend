package com.fishcam.domain.client;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    //Tous les clients d'une poissonnerie
    List<Client> findByPoissonnerie(Poissonnerie poissonnerie);

    List<Client> findByActiveTrue();

    // Liste simple (utile pour les petits cas ou les tests)
    List<Client> findByPoissonnerieAndActiveTrue(Poissonnerie poissonnerie);

    // Version paginée – LA PLUS IMPORTANTE pour ton API
    Page<Client> findByPoissonnerieAndActiveTrue(Poissonnerie poissonnerie, Pageable pageable);


    Optional<Client> findByPhoneAndPoissonnerie(String phone, Poissonnerie poissonnerie);


    long countByPoissonnerie(Poissonnerie poissonnerie);

    //Recherche par nom ou prénom (contient, insensible à la casse)
    // Recherche UNIQUEMENT par prénom ou nom (insensible à la casse)
    Page<Client> findByPoissonnerieAndActiveTrueAndFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            Poissonnerie poissonnerie,
            String firstNameTerm,
            String lastNameTerm,
            Pageable pageable);


    // Liste paginée des clients inactifs d'une poissonnerie
    Page<Client> findByPoissonnerieAndActiveFalse(Poissonnerie poissonnerie, Pageable pageable);
}
