package com.fishcam.domain.achat;


import com.fishcam.domain.produit.Produit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ligne_achat")
@Getter
@Setter
@NoArgsConstructor
public class LigneAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "achat_journalier_id", nullable = false)
    private AchatJournalier achatJournalier;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantiteCartons;

    @Column(nullable = false)
    private BigDecimal poidsKg;

    @Column(nullable = false)
    private BigDecimal montantCarton;

    @Column(nullable = false)
    private BigDecimal prixUnitaireCarton;

    @Column(nullable = false)
    private BigDecimal prixVenteKilo;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
