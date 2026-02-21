package com.serial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 序號活動主表 Entity，對應資料庫表 {@code serial_activity}。
 *
 * <p>一個「活動」代表一次序號發放事件（例如：特定促銷活動）。
 * 每個活動可以包含多筆序號明細（{@link SerialDetail}）。</p>
 *
 * <p>資料庫設定：</p>
 * <ul>
 *   <li>{@code activity_unique_id} 有唯一性約束（不可重複建立同名活動）</li>
 *   <li>索引建立在 {@code activity_unique_id} 與日期欄位，加速查詢</li>
 * </ul>
 */
@Entity
@Table(
    name = "serial_activity",
    uniqueConstraints = @UniqueConstraint(name = "UQ_ActivityUniqueID", columnNames = "activity_unique_id"),
    indexes = {
        @Index(name = "IX_serial_activity_unique_id", columnList = "activity_unique_id"),
        @Index(name = "IX_serial_activity_dates", columnList = "start_date, end_date")
    }
)
public class SerialActivity {

    /** 自動遞增主鍵（由資料庫 IDENTITY 產生） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 活動名稱，例如：「2025年會員回饋活動」 */
    @Column(name = "activity_name", nullable = false, length = 255)
    private String activityName;

    /** 活動唯一識別碼（由呼叫方提供，全系統唯一） */
    @Column(name = "activity_unique_id", nullable = false, length = 100, unique = true)
    private String activityUniqueId;

    /** 序號生效開始時間 */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** 序號生效結束時間（序號在此時間點之後無法核銷） */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** 此活動累積產生的序號總配額（追加時累加） */
    @Column(name = "quota", nullable = false)
    private Integer quota;

    /**
     * 記錄建立時間，由 Hibernate 自動設定，之後不允許更新。
     * {@code @CreationTimestamp}：INSERT 時自動填入當前時間。
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 記錄最後更新時間，由 Hibernate 自動維護。
     * {@code @UpdateTimestamp}：每次 UPDATE 時自動更新為當前時間。
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 關聯的序號明細清單（一對多關係）。
     * {@code cascade = ALL}：對活動的操作會級聯到所有關聯序號。
     * {@code fetch = LAZY}：預設不載入明細（需要時才查詢，避免效能問題）。
     */
    @OneToMany(mappedBy = "serialActivity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SerialDetail> serialDetails;

    // 無參建構函式（JPA 規範要求）
    public SerialActivity() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getActivityUniqueId() { return activityUniqueId; }
    public void setActivityUniqueId(String activityUniqueId) { this.activityUniqueId = activityUniqueId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getQuota() { return quota; }
    public void setQuota(Integer quota) { this.quota = quota; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SerialDetail> getSerialDetails() { return serialDetails; }
    public void setSerialDetails(List<SerialDetail> serialDetails) { this.serialDetails = serialDetails; }

    /** 以 id 作為實體相等性的依據（JPA 最佳實踐）。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialActivity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
