package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * POST /api/serials_insert 的請求參數 DTO（Data Transfer Object）。
 *
 * <p>用於接收建立新活動並產生序號的 API 請求 Body（JSON 格式）。</p>
 *
 * <p>{@code @JsonProperty}：指定 JSON 欄位名稱（snake_case）與 Java 屬性名稱（camelCase）的對應。</p>
 *
 * <p>請求範例：</p>
 * <pre>{@code
 * {
 *   "activity_name": "2025年會員活動",
 *   "activity_unique_id": "EVENT_2025_01",
 *   "start_date": "2025-01-01 00:00:00",
 *   "end_date": "2025-12-31 23:59:59",
 *   "quota": 50
 * }
 * }</pre>
 */
public class SerialInsertRequest {

    /** 活動名稱（必填，顯示用途） */
    @NotBlank(message = "活動名稱 欄位為必填。")
    @JsonProperty("activity_name")
    private String activityName;

    /** 活動唯一識別碼（必填，全系統不可重複，由呼叫方自訂） */
    @NotBlank(message = "活動唯一 ID 欄位為必填。")
    @JsonProperty("activity_unique_id")
    private String activityUniqueId;

    /** 序號生效開始時間（必填，格式：yyyy-MM-dd HH:mm:ss） */
    @NotNull(message = "開始日期 欄位為必填。")
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    /** 序號生效結束時間（必填，必須晚於開始時間且不能早於當前時間） */
    @NotNull(message = "結束日期 欄位為必填。")
    @JsonProperty("end_date")
    private LocalDateTime endDate;

    /** 本次產生的序號數量（必填，1～100 筆） */
    @NotNull(message = "產生數量 欄位為必填。")
    @Min(value = 1, message = "產生數量 不能小於 1。")
    @Max(value = 100, message = "產生數量 不能大於 100。")
    @JsonProperty("quota")
    private Integer quota;

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
}
