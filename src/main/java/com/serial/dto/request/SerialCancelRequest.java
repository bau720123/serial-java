package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * POST /api/serials_cancel 的請求參數 DTO。
 *
 * <p>用於批次註銷指定的序號清單。支援部分成功，即使部分序號無法註銷，
 * 其餘有效序號仍會被正常處理。</p>
 *
 * <p>請求範例：</p>
 * <pre>{@code
 * {
 *   "content": ["A0001234", "B9876543"],
 *   "note": "活動提前結束"
 * }
 * }</pre>
 */
public class SerialCancelRequest {

    /**
     * 要註銷的序號清單（必填，至少 1 筆，最多 1000 筆）。
     * 每個序號必須為 8 個字元。
     */
    @NotEmpty(message = "序號內容 欄位為必填。")
    @Size(max = 1000, message = "序號內容 一次最多只能處理 1000 筆。")
    @JsonProperty("content")
    private List<String> content;

    /** 註銷原因備註（必填，會寫入每一筆成功註銷序號的 note 欄位） */
    @NotBlank(message = "備註 欄位為必填。")
    @JsonProperty("note")
    private String note;

    public List<String> getContent() { return content; }
    public void setContent(List<String> content) { this.content = content; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
