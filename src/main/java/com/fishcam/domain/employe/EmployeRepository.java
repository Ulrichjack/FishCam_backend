package com.fishcam.domain.employe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeRepository  extends JpaRepository<Employe, Long> {

    List<Employe> findByPoissonnerieId(Long poissonnerieId);

}
