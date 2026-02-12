package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SerialInsertResponseData {

    @JsonProperty("activity_id")
    private Integer activityId;

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
