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
@Table(
        name = "releve_situation_mensuelle",
        uniqueConstraints = @UniqueConstraint(columnNames = {"poissonnerie_id", "date_releve"})
)
@Getter
@Setter
@NoArgsConstructor
public class ReleveSituationMensuelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @Column(name = "date_releve", nullable = false)
    private LocalDate dateReleve;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurStock;

    /** Null lorsque le cahier des dettes n'a pas encore été reconstitué. */
    @Column(precision = 15, scale = 2)
    private BigDecimal totalCreancesClients;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModeEvaluation modeEvaluation;

    @Column(length = 500)
    private String note;

    @ManyToOne
    @JoinColumn(name = "saisi_par_id", nullable = false)
    private User saisiPar;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
