package com.serial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 通用 API 回應封裝類別（泛型）。
 *
 * <p>所有 REST API 的回應都使用此格式，確保前端接收到一致的結構。</p>
 *
 * <p>回應 JSON 結構：</p>
 * <pre>{@code
 * {
 *   "status": "success" | "error",
 *   "message": "操作說明文字",
 *   "data": { ... },       // 成功時包含，失敗時為 null（不輸出）
 *   "errors": { ... }      // 驗證失敗時包含，成功時為 null（不輸出）
 * }
 * }</pre>
 *
 * <p>{@code @JsonInclude(NON_NULL)}：值為 null 的欄位不會輸出到 JSON，
 * 讓回應更簡潔。</p>
 *
 * @param <T> data 欄位的型別（例如：SerialInsertResponseData）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 回應狀態：{@code "success"} 或 {@code "error"} */
    @JsonProperty("status")
    private String status;

    /** 人類可讀的操作結果說明 */
    @JsonProperty("message")
    private String message;

    /** 成功時的回應資料（失敗或驗證錯誤時為 null，不輸出） */
    @JsonProperty("data")
    private T data;

    /** 驗證失敗時的錯誤詳情（欄位名稱 → 錯誤訊息清單，成功時為 null，不輸出） */
    @JsonProperty("errors")
    private Map<String, Object> errors;

    // 無參建構函式
    public ApiResponse() {}

    public ApiResponse(String status, String message, T data, Map<String, Object> errors) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    // 靜態工廠方法（Static Factory Methods）：提供語義清晰的建立方式

    /** 建立成功回應（含資料） */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, null);
    }

    /** 建立成功回應（無額外資料） */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("success", message, null, null);
    }

    /** 建立一般錯誤回應 */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, null);
    }

    /** 建立驗證失敗回應（含欄位錯誤詳情） */
    public static <T> ApiResponse<T> validationError(Map<String, Object> errors) {
        return new ApiResponse<>("error", "驗證失敗", null, errors);
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Map<String, Object> getErrors() { return errors; }
    public void setErrors(Map<String, Object> errors) { this.errors = errors; }
}
