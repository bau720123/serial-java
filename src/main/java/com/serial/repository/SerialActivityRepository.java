package com.serial.repository;

import com.serial.entity.SerialActivity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 序號活動資料存取層（Repository）。
 *
 * <p>繼承 {@link JpaRepository} 後，Spring Data JPA 會自動產生
 * 基本的 CRUD 方法（save、findById、findAll、delete 等），無需手動實作。</p>
 *
 * <p>{@code @Repository}：標記為 Spring Bean，同時啟用 JPA 例外轉換
 * （將資料庫底層例外轉為 Spring 統一的 {@code DataAccessException}）。</p>
 */
@Repository
public interface SerialActivityRepository extends JpaRepository<SerialActivity, Integer> {

    /**
     * 依活動唯一 ID 查詢活動（不加鎖）。
     * Spring Data JPA 依方法名稱自動產生查詢 SQL。
     *
     * @param activityUniqueId 活動唯一識別碼
     * @return 找到時回傳 Optional 包裝的活動，否則回傳 Optional.empty()
     */
    Optional<SerialActivity> findByActivityUniqueId(String activityUniqueId);

    /**
     * 確認活動唯一 ID 是否已存在（用於新增前的重複檢查）。
     *
     * @param activityUniqueId 活動唯一識別碼
     * @return true 表示已存在
     */
    boolean existsByActivityUniqueId(String activityUniqueId);

    /**
     * 依活動唯一 ID 查詢活動，並加上悲觀寫鎖（PESSIMISTIC_WRITE）。
     *
     * <p>在高併發場景下使用，防止多個交易同時讀取並修改同一筆活動記錄，
     * SQL 層面會執行 {@code SELECT ... WITH (UPDLOCK)}（SQL Server）。</p>
     *
     * @param activityUniqueId 活動唯一識別碼
     * @return Optional 包裝的活動（含鎖）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM SerialActivity a WHERE a.activityUniqueId = :uid")
    Optional<SerialActivity> findByActivityUniqueIdWithLock(@Param("uid") String activityUniqueId);
}
