package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SerialRedeemResponseData {

    @JsonProperty("serial_content")
    private String serialContent;

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
