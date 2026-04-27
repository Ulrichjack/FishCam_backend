package com.fishcam.domain.client;

import com.fishcam.domain.comptecourant.CompteCourant;
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
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 50)
    private String cni;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String quartier;

    private LocalDate dateOfBirth;

    @Column(length = 1000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "poissonnerie_id", nullable = false)
    private Poissonnerie poissonnerie;

    @ManyToOne
    @JoinColumn(name = "create_by_id")
    private User createdBy;

    private Boolean active;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "client")
    private CompteCourant compteCourant;


}
