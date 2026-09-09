package com.sih.lmpc_compliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "declarations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scan_id", nullable = false)
    private UUID scanId;

    @Column(name = "declaration_type_id")
    private String declarationTypeId;

    @Column(name = "extracted_text")
    private String extractedText;

    @Column(name = "font_height_mm")
    private BigDecimal fontHeightMm;

    @Column(name = "is_molded")
    private boolean molded;

    @Column(columnDefinition = "jsonb")
    private String bbox;

    private BigDecimal confidence;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
