package com.fishcam.domain.poissonnerie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoissonnerieRepository extends JpaRepository<Poissonnerie, Long> {

    //retourne tout les poissonneries actives
    List<Poissonnerie> findByActiveTrue();

    //cher par nom
    Optional<Poissonnerie> findByNameIgnoreCase(Long aLong);

    //verifie si une poissonnerie existe deja avec ce nom
    boolean existsByNameIgnoreCase(String name);

}
