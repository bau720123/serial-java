package com.serial.controller;

import com.serial.dto.request.*;
import com.serial.dto.response.*;
import com.serial.service.SerialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 序號管理 REST API 控制器。
 *
 * <p>負責接收外部 HTTP 請求，進行基本格式驗證（{@code @Valid}），
 * 再委派給 {@link SerialService} 執行核心業務邏輯，最後封裝回應。</p>
 *
 * <p>基底路徑：{@code /api}</p>
 *
 * <p>{@code @RestController}：等同於 {@code @Controller + @ResponseBody}，
 * 方法回傳值會自動序列化為 JSON。</p>
 */
@RestController
@RequestMapping("/api")
public class SerialController {

    /** 核心業務邏輯服務，透過建構函式注入（Spring 推薦方式） */
    private final SerialService serialService;

    public SerialController(SerialService serialService) {
        this.serialService = serialService;
    }

    /**
     * POST /api/serials_insert — 建立新活動並批次產生序號。
     *
     * <p>流程：驗證請求 → 建立活動記錄 → 隨機產生不重複序號 → 儲存。</p>
     *
     * @param request 包含活動名稱、唯一 ID、有效期間、產生數量
     * @return 201 Created，回傳活動 ID 與實際產生序號數量
     */
    @PostMapping("/serials_insert")
    public ResponseEntity<ApiResponse<SerialInsertResponseData>> insertSerials(
            @Valid @RequestBody SerialInsertRequest request) {
        SerialInsertResponseData data = serialService.insertSerials(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("活動與序號已成功產生", data));
    }

    /**
     * POST /api/serials_additional_insert — 對現有活動追加產生額外序號。
     *
     * <p>流程：驗證活動存在 → 更新活動配額 → 產生新序號 → 儲存。</p>
     *
     * @param request 包含活動唯一 ID、新有效期間、追加數量、備註
     * @return 201 Created，回傳活動 ID 與本次新增序號數量
     */
    @PostMapping("/serials_additional_insert")
    public ResponseEntity<ApiResponse<SerialInsertResponseData>> additionalInsertSerials(
            @Valid @RequestBody SerialAdditionalInsertRequest request) {
        SerialInsertResponseData data = serialService.additionalInsertSerials(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("序號已成功產生", data));
    }

    /**
     * POST /api/serials_redeem — 核銷指定序號（標記為已使用）。
     *
     * <p>流程：查詢序號（加悲觀鎖）→ 驗證狀態與有效期 → 更新為已核銷。</p>
     *
     * @param request 包含要核銷的序號內容（8 碼）
     * @return 200 OK，回傳序號內容與核銷時間
     */
    @PostMapping("/serials_redeem")
    public ResponseEntity<ApiResponse<SerialRedeemResponseData>> redeemSerial(
            @Valid @RequestBody SerialRedeemRequest request) {
        SerialRedeemResponseData data = serialService.redeemSerial(request);
        return ResponseEntity.ok(ApiResponse.success("核銷成功", data));
    }

    /**
     * POST /api/serials_cancel — 批次註銷指定序號（標記為不可使用）。
     *
     * <p>流程：驗證序號格式 → 批次查詢（加悲觀鎖）→ 各別判斷可否註銷 → 批次更新。</p>
     *
     * @param request 包含序號清單（最多 1000 筆）與備註原因
     * @return 200 OK，回傳成功/失敗清單與原因
     */
    @PostMapping("/serials_cancel")
    public ResponseEntity<SerialCancelResponse> cancelSerials(
            @Valid @RequestBody SerialCancelRequest request) {
        SerialCancelResponse response = serialService.cancelSerials(request);
        return ResponseEntity.ok(response);
    }
}
