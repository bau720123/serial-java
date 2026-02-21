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

/**
 * 序號明細資料存取層（Repository）。
 *
 * <p>提供序號相關的資料庫操作，特別針對核銷與批次處理的高併發場景
 * 使用悲觀鎖（Pessimistic Lock）防止 Race Condition。</p>
 */
@Repository
public interface SerialDetailRepository extends JpaRepository<SerialDetail, Integer> {

    /**
     * 依序號內容查詢單一序號，並加上悲觀寫鎖。
     *
     * <p>用於核銷（redeem）操作。加鎖確保同一序號在同一時間只有一個
     * 交易能夠讀取並修改，防止重複核銷。</p>
     *
     * @param content 序號內容（8碼大寫英數）
     * @return Optional 包裝的序號明細（含鎖）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerialDetail s WHERE s.content = :content")
    Optional<SerialDetail> findByContentWithLock(@Param("content") String content);

    /**
     * 批次依序號內容查詢多筆序號，並加上悲觀寫鎖。
     *
     * <p>用於批次註銷（cancel）操作，一次鎖定所有目標序號，
     * 避免逐筆查詢造成效能問題。</p>
     *
     * @param contents 序號內容 Set（自動去重，最多 1000 筆）
     * @return 符合條件的序號明細清單（含鎖）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerialDetail s WHERE s.content IN :contents")
    List<SerialDetail> findByContentInWithLock(@Param("contents") Set<String> contents);

    /**
     * 查詢候選序號中哪些已存在於資料庫中。
     *
     * <p>用於產生序號時的重複檢查：先批次查詢再排除已存在的，
     * 比逐筆檢查效率高得多。只回傳 content 欄位（不需要完整實體）。</p>
     *
     * @param contents 候選序號 Set
     * @return 已存在於資料庫中的序號內容 Set
     */
    @Query("SELECT s.content FROM SerialDetail s WHERE s.content IN :contents")
    Set<String> findExistingContents(@Param("contents") Set<String> contents);
}
