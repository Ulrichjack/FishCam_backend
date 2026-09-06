package com.fishcam.domain.gestion;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_gestion")
@Getter
@Setter
@NoArgsConstructor
public class ChargeGestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null pour une charge générale commune à toute l'entreprise. */
    @ManyToOne
    @JoinColumn(name = "poissonnerie_id")
    private Poissonnerie poissonnerie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategorieCharge categorie;

    @Column(nullable = false, length = 120)
    private String libelle;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private Boolean recurrente = true;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
