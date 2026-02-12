package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SerialCancelResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("cancel_at")
    private String cancelAt;

    @JsonProperty("success_data")
    private CancelData successData;

    @JsonProperty("fail_data")
    private CancelData failData;

    public SerialCancelResponse() {}

    public SerialCancelResponse(String status, String message, String cancelAt, CancelData successData, CancelData failData) {
        this.status = status;
        this.message = message;
        this.cancelAt = cancelAt;
        this.successData = successData;
        this.failData = failData;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCancelAt() { return cancelAt; }
    public void setCancelAt(String cancelAt) { this.cancelAt = cancelAt; }

    public CancelData getSuccessData() { return successData; }
    public void setSuccessData(CancelData successData) { this.successData = successData; }

    public CancelData getFailData() { return failData; }
    public void setFailData(CancelData failData) { this.failData = failData; }

    // Nested class
    public static class CancelData {
        @JsonProperty("serial_content")
        private String serialContent;

        public CancelData() {}

        public CancelData(String serialContent) {
            this.serialContent = serialContent;
        }

        public String getSerialContent() { return serialContent; }
        public void setSerialContent(String serialContent) { this.serialContent = serialContent; }
    }
}
