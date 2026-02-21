package com.serial.exception;

/**
 * 業務邏輯例外（Business Exception）。
 *
 * <p>用於表達「業務規則被違反」的例外情況，例如：</p>
 * <ul>
 *   <li>序號不存在</li>
 *   <li>序號已被核銷或註銷</li>
 *   <li>序號尚未生效或已過期</li>
 * </ul>
 *
 * <p>與框架的驗證例外（{@code MethodArgumentNotValidException}）不同，
 * 此類別代表業務層主動拋出的錯誤，固定對應 HTTP 400 Bad Request。</p>
 *
 * <p>由 {@link GlobalExceptionHandler} 統一捕捉並轉為 API 錯誤回應。</p>
 *
 * <p>繼承 {@code RuntimeException}（非受檢例外），無需在方法簽章中宣告 {@code throws}。</p>
 */
public class BusinessException extends RuntimeException {

    /** 對應的 HTTP 狀態碼（固定為 400 Bad Request） */
    private final int httpStatus;

    /**
     * 建立業務例外，HTTP 狀態碼預設為 400。
     *
     * @param message 對終端使用者顯示的錯誤說明
     */
    public BusinessException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    /**
     * 取得對應的 HTTP 狀態碼。
     *
     * @return 400
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
