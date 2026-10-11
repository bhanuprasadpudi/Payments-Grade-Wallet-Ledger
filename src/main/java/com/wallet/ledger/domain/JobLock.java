package com.wallet.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "job_locks")
public class JobLock {

    @Id
    @Column(name = "lock_name", length = 100)
    private String lockName;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "locked_by", length = 200)
    private String lockedBy;

    @Column(name = "acquired_at")
    private Instant acquiredAt;

    protected JobLock() {
    }

    public JobLock(String lockName) {
        this.lockName = lockName;
    }

    public String getLockName() { return lockName; }
    public Instant getLockedUntil() { return lockedUntil; }
    public String getLockedBy() { return lockedBy; }
    public Instant getAcquiredAt() { return acquiredAt; }
}
