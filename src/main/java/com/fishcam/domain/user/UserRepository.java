package com.fishcam.domain.user;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByPhone(String phone);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    List<User> findByDefaultPoissonnerie(Poissonnerie poissonnerie);

    List<User> findByScope(UserScope scope);

    boolean existsByPhone(String phone);
}
