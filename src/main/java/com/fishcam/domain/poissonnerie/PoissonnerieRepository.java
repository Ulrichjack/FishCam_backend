package com.fishcam.domain.poissonnerie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoissonnerieRepository extends JpaRepository<Poissonnerie, Long> {


    List<Poissonnerie> findByActiveTrue();


    Optional<com.fishcam.domain.poissonnerie.Poissonnerie> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

}

