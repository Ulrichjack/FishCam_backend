package com.fishcam.domain.comptecourant;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compte_courant")
@Getter
@Setter
@NoArgsConstructor
public class CompteCourant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteCreditMax = new BigDecimal("50000");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCompteCourant statut = StatutCompteCourant.ACTIF;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateOuverture;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Poissonnerie getPoissonnerie() {
        return client != null ? client.getPoissonnerie() : null;
    }

    public boolean estEnDette() {
        return solde.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean estSolde() {
        return solde.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean depasseSeuilAlerte() {
        return solde.compareTo(new BigDecimal("-5000")) < 0;
    }

    public boolean depasseLimiteCredit() {
        return solde.compareTo(limiteCreditMax.negate()) < 0;
    }

    public BigDecimal getMontantDette() {
        return estEnDette() ? solde.abs() : BigDecimal.ZERO;
    }

    public BigDecimal getCreditRestant() {
        return limiteCreditMax.add(solde);
    }
}