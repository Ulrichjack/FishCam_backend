package com.fishcam.domain.epargne;

import com.fishcam.domain.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpargneRepository extends JpaRepository<Epargne, Long> {

    //L'épargne d'un client (0 ou 1 maximum)
    Optional<Epargne> findByClient(Client client);

    //Vérifie si un client a déjà une épargne
    boolean existsByClient(Client client);


}
