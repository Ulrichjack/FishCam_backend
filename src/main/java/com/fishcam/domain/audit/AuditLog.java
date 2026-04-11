package com.fishcam.domain.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "CREATE", "UPDATE", "DELETE", "LOGIN"

    @Column(nullable = false)
    private String entityName; // e.g., "Client", "Vente", "ClotureJournaliere"

    private Long entityId; // The ID of the item that was changed

    @Column(nullable = false)
    private String performedBy; // The username or ID of the person who did it

    @Column(length = 1000)
    private String details; // Extra info, e.g., "Changed debt from 5000 to 2000"

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}