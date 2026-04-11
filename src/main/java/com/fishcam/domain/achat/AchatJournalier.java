package com.fishcam.domain.achat;


import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achat_journalier")
@Getter
@Setter
@NoArgsConstructor
public class AchatJournalier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateAchat;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User enregistrePar;

    @Column(nullable = false)
    private Boolean cloture = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
