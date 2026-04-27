package com.fishcam.domain.backup;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_record")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BackupRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateExecution;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeBackup type;

    @Column(nullable = false)
    private Boolean success;

}
