package com.fishcam.domain.poissonnerie;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "poissonnerie")
@Getter
@Setter
@NoArgsConstructor
public class Poissonnerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 225)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private BigDecimal loyer = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal fondDeCaisseDefaut = BigDecimal.valueOf(10000);

    @Column(nullable = false)
    private Boolean pretActif = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
