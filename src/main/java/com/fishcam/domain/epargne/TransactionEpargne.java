package com.fishcam.domain.epargne;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import java.time.LocalDateTime;

//Historique complet des dépôts et retraits
//Permet de voir tous les mouvements d'argent dans le temps

@Entity
@Table(name = "transaction_saving")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEpargne {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "saving_id", nullable = false)
    private Epargne epargne;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransactionEpargne type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    private Poissonnerie poissonnerie;

    @ManyToOne
    private User effectuePar;

    @Column(nullable = false)
    private LocalDateTime transactionDate;


}
