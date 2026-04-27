package com.fishcam.domain.livreur;

import com.fishcam.domain.achat.AchatJournalier;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_livreur")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationLivreur {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateEvaluation;

    @Column(nullable = false)
    private Integer qualiteProduit;


    @Column(nullable = false)
    private Integer respectPoids;

    @Column(length = 500)
    private String commentaire;

    @Column(nullable = false)
    private Boolean problemeSignale = false;

    @ManyToOne
    @JoinColumn(name = "livreur_id", nullable = false)
    private Livreur livreur;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "achat_journalier_id", nullable = false)
    private AchatJournalier achatJournalier;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


}
