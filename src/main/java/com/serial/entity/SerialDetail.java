package com.serial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 序號明細表 Entity，對應資料庫表 {@code serial_detail}。
 *
 * <p>每一筆記錄代表一個獨立的序號（8碼），隸屬於某個活動（{@link SerialActivity}）。
 * 序號有三種狀態：未核銷、已核銷、已註銷，由 {@code status} 欄位控制。</p>
 *
 * <p>資料庫設定：</p>
 * <ul>
 *   <li>{@code content} 有唯一性約束，確保序號全系統不重複</li>
 *   <li>索引建立在 {@code content}、{@code status}、外鍵與日期欄位，加速查詢</li>
 * </ul>
 */
@Entity
@Table(
    name = "serial_detail",
    uniqueConstraints = @UniqueConstraint(name = "UQ_SerialContent", columnNames = "content"),
    indexes = {
        @Index(name = "IX_serial_detail_activity_id", columnList = "serial_activity_id"),
        @Index(name = "IX_serial_detail_status", columnList = "status"),
        @Index(name = "IX_serial_detail_dates", columnList = "start_date, end_date"),
        @Index(name = "IX_serial_detail_content", columnList = "content")
    }
)
public class SerialDetail {

    /** 狀態：未核銷（預設值），序號尚未被使用 */
    public static final int STATUS_UNUSED = 0;

    /** 狀態：已核銷，序號已被成功使用，不可再次核銷 */
    public static final int STATUS_USED = 1;

    /** 狀態：已註銷，序號被管理者主動作廢，無法再使用 */
    public static final int STATUS_CANCELLED = 2;

    /** 自動遞增主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 所屬活動（多對一關係）。
     * {@code fetch = LAZY}：不自動載入活動資料，需要時才查詢。
     * {@code @JoinColumn}：指定外鍵欄位名稱為 {@code serial_activity_id}。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_activity_id", nullable = false, foreignKey = @ForeignKey(name = "FK_serial_detail_activity"))
    private SerialActivity serialActivity;

    /** 序號內容（8碼英數，全大寫），例如：{@code A0001234}，全系統唯一 */
    @Column(name = "content", nullable = false, length = 8, unique = true)
    private String content;

    /**
     * 序號狀態。
     * 可用值參考類別常數：{@link #STATUS_UNUSED}、{@link #STATUS_USED}、{@link #STATUS_CANCELLED}。
     */
    @Column(name = "status", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer status;

    /** 備註說明（追加時的原因說明、或註銷原因），可為 null */
    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    /** 序號生效開始時間（早於此時間不得核銷） */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** 序號生效結束時間（晚於此時間不得核銷） */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** 記錄建立時間，由 Hibernate 自動設定，不可更新 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 最後更新時間（核銷或註銷時手動設定） */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 無參建構函式（JPA 規範要求）
    public SerialDetail() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public SerialActivity getSerialActivity() { return serialActivity; }
    public void setSerialActivity(SerialActivity serialActivity) { this.serialActivity = serialActivity; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** 以 id 作為實體相等性的依據（JPA 最佳實踐）。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialDetail that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
