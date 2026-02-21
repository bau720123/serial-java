package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 序號核銷成功時的回應資料 DTO。
 *
 * <p>用於 POST /api/serials_redeem 的回應。</p>
 *
 * <p>回應 JSON 結構（包在 ApiResponse.data 內）：</p>
 * <pre>{@code
 * {
 *   "serial_content": "A0001234",
 *   "redeemed_at": "2025-06-01 12:00:00"
 * }
 * }</pre>
 */
public class SerialRedeemResponseData {

    /** 已核銷的序號內容（8碼大寫英數） */
    @JsonProperty("serial_content")
    private String serialContent;

    /** 核銷完成的時間（格式：yyyy-MM-dd HH:mm:ss） */
    @JsonProperty("redeemed_at")
    private String redeemedAt;

    public SerialRedeemResponseData() {}

    public SerialRedeemResponseData(String serialContent, String redeemedAt) {
        this.serialContent = serialContent;
        this.redeemedAt = redeemedAt;
    }

    public String getSerialContent() { return serialContent; }
    public void setSerialContent(String serialContent) { this.serialContent = serialContent; }

    public String getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(String redeemedAt) { this.redeemedAt = redeemedAt; }
}
