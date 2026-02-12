package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class SerialRedeemRequest {

    @NotBlank(message = "序號內容 欄位為必填。")
    @JsonProperty("content")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
