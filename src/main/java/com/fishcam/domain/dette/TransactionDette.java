package com.fishcam.domain.dette;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "transaction_dette")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDette {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "dette_id", nullable = false)
    private Dette dette;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "default_poissonnerie_id")
    private Poissonnerie defaultPoissonnerie;

    @ManyToOne
    @JoinColumn(name = "effectue_par_id", nullable = false)
    private User effectuePar;


}
