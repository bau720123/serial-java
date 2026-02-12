package com.serial.repository;

import com.serial.entity.SerialLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerialLogRepository extends JpaRepository<SerialLog, Integer> {
}
