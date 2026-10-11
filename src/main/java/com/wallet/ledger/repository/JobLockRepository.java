package com.wallet.ledger.repository;

import com.wallet.ledger.domain.JobLock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobLockRepository extends JpaRepository<JobLock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JobLock j where j.lockName = :lockName")
    Optional<JobLock> findByLockNameForUpdate(@Param("lockName") String lockName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM job_locks WHERE lock_name = :lockName FOR UPDATE NOWAIT", nativeQuery = true)
    Optional<JobLock> tryLockNowait(@Param("lockName") String lockName);
}
