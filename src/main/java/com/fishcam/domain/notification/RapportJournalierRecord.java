package com.fishcam.domain.notification;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport_journalier_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"poissonnerie_id", "date_rapport"}))
@Getter
@Setter
@NoArgsConstructor
public class RapportJournalierRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @Column(name = "date_rapport", nullable = false)
    private LocalDate dateRapport;

    @CreationTimestamp
    private LocalDateTime generatedAt;

    public RapportJournalierRecord(Poissonnerie poissonnerie, LocalDate dateRapport) {
        this.poissonnerie = poissonnerie;
        this.dateRapport = dateRapport;
    }
}