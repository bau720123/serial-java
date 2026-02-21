package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * POST /api/serials_additional_insert 的請求參數 DTO。
 *
 * <p>用於對既有活動追加產生序號。與新增活動（{@link SerialInsertRequest}）的差異：</p>
 * <ul>
 *   <li>不需要 {@code activity_name}（活動已存在）</li>
 *   <li>需要填寫 {@code note}（追加備註原因，為必填）</li>
 *   <li>指定的活動唯一 ID 必須已存在於系統中</li>
 * </ul>
 */
public class SerialAdditionalInsertRequest {

    /** 活動唯一識別碼（必填，目標活動必須已存在） */
    @NotBlank(message = "活動唯一 ID 欄位為必填。")
    @JsonProperty("activity_unique_id")
    private String activityUniqueId;

    /** 本批追加序號的生效開始時間（必填） */
    @NotNull(message = "開始日期 欄位為必填。")
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    /** 本批追加序號的生效結束時間（必填） */
    @NotNull(message = "結束日期 欄位為必填。")
    @JsonProperty("end_date")
    private LocalDateTime endDate;

    /** 本次追加的序號數量（必填，1～100 筆） */
    @NotNull(message = "產生數量 欄位為必填。")
    @Min(value = 1, message = "產生數量 不能小於 1。")
    @Max(value = 100, message = "產生數量 不能大於 100。")
    @JsonProperty("quota")
    private Integer quota;

    /** 追加原因備註（必填，會寫入每一筆追加序號的 note 欄位） */
    @NotBlank(message = "備註追加原因 欄位為必填。")
    @JsonProperty("note")
    private String note;

    public String getActivityUniqueId() { return activityUniqueId; }
    public void setActivityUniqueId(String activityUniqueId) { this.activityUniqueId = activityUniqueId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getQuota() { return quota; }
    public void setQuota(Integer quota) { this.quota = quota; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
