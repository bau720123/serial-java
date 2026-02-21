package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 新增/追加序號成功時的回應資料 DTO。
 *
 * <p>用於 POST /api/serials_insert 與 POST /api/serials_additional_insert 的回應。</p>
 *
 * <p>回應 JSON 結構（包在 ApiResponse.data 內）：</p>
 * <pre>{@code
 * {
 *   "activity_id": 1,
 *   "total_generated": 50
 * }
 * }</pre>
 */
public class SerialInsertResponseData {

    /** 活動的資料庫主鍵 ID */
    @JsonProperty("activity_id")
    private Integer activityId;

    /** 本次實際產生並儲存的序號數量 */
    @JsonProperty("total_generated")
    private Integer totalGenerated;

    public SerialInsertResponseData() {}

    public SerialInsertResponseData(Integer activityId, Integer totalGenerated) {
        this.activityId = activityId;
        this.totalGenerated = totalGenerated;
    }

    public Integer getActivityId() { return activityId; }
    public void setActivityId(Integer activityId) { this.activityId = activityId; }

    public Integer getTotalGenerated() { return totalGenerated; }
    public void setTotalGenerated(Integer totalGenerated) { this.totalGenerated = totalGenerated; }
}
