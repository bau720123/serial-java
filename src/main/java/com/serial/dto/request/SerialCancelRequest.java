package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.List;

public class SerialCancelRequest {

    @NotEmpty(message = "序號內容 欄位為必填。")
    @Size(max = 1000, message = "序號內容 一次最多只能處理 1000 筆。")
    @JsonProperty("content")
    private List<String> content;

    @NotBlank(message = "備註 欄位為必填。")
    @JsonProperty("note")
    private String note;

    public List<String> getContent() { return content; }
    public void setContent(List<String> content) { this.content = content; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
