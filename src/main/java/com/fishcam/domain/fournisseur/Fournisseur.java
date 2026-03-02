package com.fishcam.domain.fournisseur;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
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

    @Email
    @Column(nullable = false)
    private String email;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String telephone;

    @Column(nullable = false,updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
