package com.serial.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * POST /api/serials_redeem 的請求參數 DTO。
 *
 * <p>用於核銷單一序號。提交的序號會自動去除前後空白並轉換為大寫，
 * 因此大小寫不影響核銷結果。</p>
 *
 * <p>請求範例：</p>
 * <pre>{@code
 * {
 *   "content": "A0001234"
 * }
 * }</pre>
 */
public class SerialRedeemRequest {

    /** 要核銷的序號內容（必填，8碼英數，大小寫不敏感） */
    @NotBlank(message = "序號內容 欄位為必填。")
    @JsonProperty("content")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
