package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class SerialAdditionalInsertRequest {

    @NotBlank(message = "活動唯一 ID 欄位為必填。")
    @JsonProperty("activity_unique_id")
    private String activityUniqueId;

    @NotNull(message = "開始日期 欄位為必填。")
    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @NotNull(message = "結束日期 欄位為必填。")
    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @NotNull(message = "產生數量 欄位為必填。")
    @Min(value = 1, message = "產生數量 不能小於 1。")
    @Max(value = 100, message = "產生數量 不能大於 100。")
    @JsonProperty("quota")
    private Integer quota;

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
