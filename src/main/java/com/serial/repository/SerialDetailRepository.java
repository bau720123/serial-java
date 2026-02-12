package com.serial.repository;

import com.serial.entity.SerialDetail;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SerialDetailRepository extends JpaRepository<SerialDetail, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerialDetail s WHERE s.content = :content")
    Optional<SerialDetail> findByContentWithLock(@Param("content") String content);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerialDetail s WHERE s.content IN :contents")
    List<SerialDetail> findByContentInWithLock(@Param("contents") Set<String> contents);

    @Query("SELECT s.content FROM SerialDetail s WHERE s.content IN :contents")
    Set<String> findExistingContents(@Param("contents") Set<String> contents);
}
