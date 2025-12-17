package com.fishcam.domain.comptecourant;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction sur un compte courant.
 *
 * Chaque mouvement (emprunt ou remboursement) est enregistré.
 * On garde l'historique complet avec le solde avant/après.
 *
 * Exemples :
 *
 * Transaction 1 - EMPRUNT
 * - Type : EMPRUNT
 * - Montant : 3000 FCFA
 * - Solde avant : -2000 FCFA
 * - Solde après : -5000 FCFA
 * - Description : "Achat de carpes"
 *
 * Transaction 2 - REMBOURSEMENT
 * - Type : REMBOURSEMENT
 * - Montant : 2000 FCFA
 * - Solde avant : -5000 FCFA
 * - Solde après : -3000 FCFA
 * - Description : "Paiement cash"
 */
@Entity
@Table(name = "transaction_compte_courant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompteCourant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compte_courant_id", nullable = false)
    private CompteCourant compteCourant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransactionCC type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal soldePrecedent;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeApres;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @ManyToOne
    @JoinColumn(name = "effectue_par_id", nullable = false)
    private User effectuePar;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime transactionDate;

    @Column(length = 1000)
    private String notes;

    public boolean estEmprunt() {
        return type == TypeTransactionCC.EMPRUNT;
    }

    public boolean estRemboursement() {
        return type == TypeTransactionCC.REMBOURSEMENT;
    }

    public BigDecimal getVariationSolde() {
        return soldeApres.subtract(soldePrecedent);
    }
}