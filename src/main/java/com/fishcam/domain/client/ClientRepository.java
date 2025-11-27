package com.fishcam.domain.client;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    //Tous les clients d'une poissonnerie
    List<Client> findByPoissonnerie(Poissonnerie poissonnerie);

    List<Client> findByActiveTrue();


    Optional<Client> findByPhoneAndPoissonnerie(String phone, Poissonnerie poissonnerie);

     //Recherche par nom ou prénom (contient, insensible à la casse)
    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    long countByPoissonnerie(Poissonnerie poissonnerie);
}
