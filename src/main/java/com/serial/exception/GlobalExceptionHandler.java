package com.serial.exception;

import com.serial.dto.response.ApiResponse;
import com.serial.service.SerialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

/**
 * 全域例外處理器。
 *
 * <p>集中攔截所有 Controller 拋出的例外，統一轉換為標準 {@link ApiResponse} JSON 格式，
 * 確保 API 在任何錯誤情況下都回傳一致的結構，避免框架預設錯誤頁面洩漏給呼叫端。</p>
 *
 * <p>{@code @RestControllerAdvice}：等同於 {@code @ControllerAdvice + @ResponseBody}，
 * 所有 {@code @ExceptionHandler} 的回傳值都會自動序列化為 JSON。</p>
 *
 * <p>處理的例外類型：</p>
 * <ul>
 *   <li>{@code MethodArgumentNotValidException} → 422 驗證失敗（Bean Validation 觸發）</li>
 *   <li>{@code SerialService.ValidationException} → 422 驗證失敗（Service 層業務驗證觸發）</li>
 *   <li>{@code HttpMessageNotReadableException} → 422 JSON 格式錯誤</li>
 *   <li>{@code BusinessException} → 400 業務邏輯錯誤</li>
 *   <li>{@code Exception}（兜底） → 500 系統非預期錯誤</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理 Bean Validation（{@code @Valid}）觸發的驗證失敗例外。
     *
     * <p>將多個欄位的多個錯誤訊息整理為 {@code Map<field, List<message>>} 格式，
     * 回傳 HTTP 422 Unprocessable Entity。</p>
     *
     * @param ex Bean Validation 驗證失敗例外
     * @return 422 回應，含欄位錯誤詳情
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.computeIfAbsent(field, k -> new ArrayList<String>());
            @SuppressWarnings("unchecked")
            List<String> fieldErrors = (List<String>) errors.get(field);
            fieldErrors.add(msg);
        });
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(errors));
    }

    /**
     * 處理 Service 層業務驗證失敗例外（例如日期邏輯錯誤、活動 ID 重複）。
     *
     * @param ex Service 層驗證例外（含欄位錯誤 Map）
     * @return 422 回應，含欄位錯誤詳情
     */
    @ExceptionHandler(SerialService.ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceValidation(SerialService.ValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(ex.getErrors()));
    }

    /**
     * 處理請求 Body 的 JSON 格式錯誤（例如日期格式錯誤、欄位型態不符）。
     *
     * @param ex JSON 解析失敗例外
     * @return 422 回應，含通用錯誤說明
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleJsonParse(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("請求格式錯誤，請確認 JSON 格式與欄位型態正確。"));
    }

    /**
     * 處理業務邏輯例外（序號不存在、已核銷、已過期等）。
     *
     * @param ex 業務例外（含 HTTP 狀態碼與錯誤訊息）
     * @return 400 回應，含錯誤說明
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 兜底例外處理：捕捉所有未被上述 Handler 處理的例外。
     *
     * <p>避免 Spring 預設的錯誤頁面或堆疊訊息洩漏給 API 呼叫端，
     * 統一回傳 500 且僅顯示通用提示。</p>
     *
     * @param ex 任何未預期的例外
     * @return 500 回應，含通用錯誤說明
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統發生非預期錯誤，請稍後再試。"));
    }
}
