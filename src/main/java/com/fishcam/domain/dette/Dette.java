package com.fishcam.domain.dette;

import java.time.LocalDateTime;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "dette")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dette {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id",nullable = false)
    private Poissonnerie poissonnerie;

    //montant initial
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialAmount;

    //montant restant a payer
    @Column(nullable = false, precision =10, scale = 2)
    private BigDecimal  remainingAmount;

    @Enumerated(EnumType.STRING)
    private StatutDette statut;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "create_by_id")
    private User createBy;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    
}
