package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 批次註銷序號的回應 DTO。
 *
 * <p>用於 POST /api/serials_cancel 的回應（此 API 不走 ApiResponse 包裝，直接回傳此物件）。</p>
 *
 * <p>回應 JSON 結構：</p>
 * <pre>{@code
 * {
 *   "status": "success",
 *   "message": "部分註銷成功",
 *   "cancel_at": "2025-06-01 12:00:00",
 *   "success_data": { "serial_content": "A0001234,B9876543" },
 *   "fail_data":    { "serial_content": "C1234567 (此序號已被核銷，無法再註銷)" }
 * }
 * }</pre>
 *
 * <p>{@code message} 的可能值：「全部註銷成功」、「全部註銷失敗」、「部分註銷成功」。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SerialCancelResponse {

    /** 操作狀態，固定為 {@code "success"}（業務層處理結果由 message 說明） */
    @JsonProperty("status")
    private String status;

    /** 操作結果說明：「全部註銷成功」/ 「全部註銷失敗」/ 「部分註銷成功」 */
    @JsonProperty("message")
    private String message;

    /** 執行註銷操作的時間點（格式：yyyy-MM-dd HH:mm:ss） */
    @JsonProperty("cancel_at")
    private String cancelAt;

    /** 成功註銷的序號資料（逗號分隔的序號字串） */
    @JsonProperty("success_data")
    private CancelData successData;

    /** 失敗的序號資料（逗號分隔，每個序號後附帶失敗原因） */
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

    /**
     * 巢狀類別：用於承載成功或失敗的序號清單字串。
     */
    public static class CancelData {
        /** 逗號分隔的序號字串（成功時只有序號；失敗時每個序號後附帶括號說明原因） */
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
