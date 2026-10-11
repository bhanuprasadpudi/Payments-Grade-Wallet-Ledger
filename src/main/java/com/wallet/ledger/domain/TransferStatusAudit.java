package com.wallet.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_status_audits")
public class TransferStatusAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "transfer_id", nullable = false)
    private UUID transferId;
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private TransferStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private TransferStatus newStatus;
    @Column(name = "changed_by")
    private UUID changedBy;
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected TransferStatusAudit() {}

    public TransferStatusAudit(UUID transferId, TransferStatus oldStatus,
                               TransferStatus newStatus, UUID changedBy) {
        this.transferId = transferId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }
}
