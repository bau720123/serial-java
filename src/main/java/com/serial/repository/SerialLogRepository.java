package com.serial.repository;

import com.serial.entity.SerialLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * API 日誌資料存取層（Repository）。
 *
 * <p>目前僅需要新增（save）功能，由 {@link com.serial.middleware.ApiLoggerFilter}
 * 在每次 API 請求完成後呼叫 {@code save()} 寫入日誌。</p>
 *
 * <p>繼承 {@link JpaRepository} 已提供完整的 CRUD 操作，
 * 此介面無需額外定義查詢方法。</p>
 */
@Repository
public interface SerialLogRepository extends JpaRepository<SerialLog, Integer> {
}
