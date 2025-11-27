package com.fishcam.domain.epargne;

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
@Table(name = "epargne_saving")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Epargne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @ManyToOne
    private Poissonnerie poissonnerie;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentBalance;

    @OneToOne   
    private User createBy;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
}
