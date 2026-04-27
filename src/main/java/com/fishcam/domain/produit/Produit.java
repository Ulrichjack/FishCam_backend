package com.fishcam.domain.produit;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produit")
@Getter
@Setter
@NoArgsConstructor
public class Produit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nom;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unite unite = Unite.KG;

    @Column(nullable = false)
    private BigDecimal poidsParCarton;

    @Column(nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}
