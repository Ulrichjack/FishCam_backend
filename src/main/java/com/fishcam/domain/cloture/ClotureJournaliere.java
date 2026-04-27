package com.fishcam.domain.cloture;


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
@Table(
        name = "cloture_journaliere",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"poissonnerie_id", "date"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ClotureJournaliere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id",nullable = false)
    private Poissonnerie poissonnerie;

    @ManyToOne
    @JoinColumn(name = "cloture_par_id",nullable = false)
    private User cloturePar;

    @Column(nullable = false)
    private BigDecimal totalAchat;

    @Column(nullable = false)
    private BigDecimal totalVentePrevisible;

    @Column(nullable = false)
    private BigDecimal montantDettesJour;

    @Column(nullable = false)
    private BigDecimal montantRembourseJour;

    @Column(nullable = false)
    private Integer nombreDettesJour;

    @Column(nullable = false)
    private BigDecimal argentCaisse;

    @Column(nullable = false)
    private BigDecimal fondDeCaisse;

    @Column(nullable = false)
    private BigDecimal transport = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal ration = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal autresFrais = BigDecimal.ZERO;

    @Column(length = 500)
    private String descriptionAutres;

    @Column(nullable = false)
    private BigDecimal venteRealisee;

    @Column(nullable = false)
    private BigDecimal totalDepenses;

    @Column(nullable = false)
    private BigDecimal beneficeNet;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


}
