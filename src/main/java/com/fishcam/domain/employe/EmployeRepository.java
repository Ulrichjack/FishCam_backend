package com.fishcam.domain.employe;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    List<Employe> findByPoissonnerieId(Long poissonnerieId);

    Optional<Employe> findByTelephone(String phone);

    boolean existsByTelephone(String telephone);

}
