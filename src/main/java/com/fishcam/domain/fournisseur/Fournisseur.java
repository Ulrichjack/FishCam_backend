package com.fishcam.domain.fournisseur;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Poissonnerie poissonnerie;

    @Column(nullable = false)
    private String nom;



    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String telephone;

    @Column(nullable = false,updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
