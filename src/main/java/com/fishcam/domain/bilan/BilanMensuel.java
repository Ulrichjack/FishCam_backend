package com.fishcam.domain.bilan;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bilan_mensuel", uniqueConstraints = @UniqueConstraint(columnNames = {"poissonnerie_id", "mois", "annee"}))
public class BilanMensuel {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mois;

    @Column(nullable = false)
    private Integer annee;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User generePar;

    @Column(nullable = false)
    private BigDecimal totalAchatMois;

    @Column(nullable = false)
    private BigDecimal totalVenteRealisee;

    @Column(nullable = false)
    private BigDecimal totalVentePrevisibleMois;

    @Column(nullable = false)
    private BigDecimal totalDepensesMois;

    @Column(nullable = false)
    private BigDecimal beneficeNetMois;

    @Column(nullable = false)
    private BigDecimal beneficeMeilleurJour;

    @Column(nullable = false)
    private BigDecimal montantDettesMois;

    @Column(nullable = false)
    private Integer nombreJoursTravailles;

    @Column(nullable = false)
    private LocalDate meilleurJourBenefice;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;



}
