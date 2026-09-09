package com.sih.lmpc_compliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scan_id", nullable = false)
    private UUID scanId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    private String format;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private Instant generatedAt;
}
