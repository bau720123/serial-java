package com.serial.repository;

import com.serial.entity.SerialActivity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerialActivityRepository extends JpaRepository<SerialActivity, Integer> {
    Optional<SerialActivity> findByActivityUniqueId(String activityUniqueId);
    boolean existsByActivityUniqueId(String activityUniqueId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM SerialActivity a WHERE a.activityUniqueId = :uid")
    Optional<SerialActivity> findByActivityUniqueIdWithLock(@Param("uid") String activityUniqueId);
}
