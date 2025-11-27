package com.fishcam.domain.poissonnerie;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "poissonnerie")
@Data
@AllArgsConstructor
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

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
   


}
