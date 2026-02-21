package com.serial.service;

import com.serial.dto.request.*;
import com.serial.dto.response.*;
import com.serial.entity.SerialActivity;
import com.serial.entity.SerialDetail;
import com.serial.exception.BusinessException;
import com.serial.repository.SerialActivityRepository;
import com.serial.repository.SerialDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 序號管理核心業務邏輯服務
 * 
 * 這個類別是整個系統的核心，負責：
 * 1. 批次新增序號（insertSerials）
 * 2. 批次追加序號（additionalInsertSerials）
 * 3. 核銷序號（redeemSerial）
 * 4. 批次註銷序號（cancelSerials）
 * 
 * @Service 註解：告訴 Spring 這是一個業務邏輯層元件
 * Spring 會自動建立這個類別的實例（Bean），並注入到需要它的地方
 */
@Service
public class SerialService {

    // 依賴注入：Spring 自動注入這兩個 Repository
    private final SerialActivityRepository activityRepo;  // 活動資料存取
    private final SerialDetailRepository detailRepo;      // 序號資料存取
    
    // 日期時間格式化工具：統一格式為 "yyyy-MM-dd HH:mm:ss"
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SerialService(SerialActivityRepository activityRepo, SerialDetailRepository detailRepo) {
        this.activityRepo = activityRepo;
        this.detailRepo = detailRepo;
    }

    /**
     * 建立新活動並批次產生序號。
     *
     * <p>{@code @Transactional}：整個方法在同一個資料庫交易中執行，
     * 任何例外都會自動 rollback，確保資料一致性。</p>
     *
     * @param req 請求參數（活動名稱、唯一 ID、有效期間、產生數量）
     * @return 活動 ID 與實際產生序號數量
     */
    @Transactional
    public SerialInsertResponseData insertSerials(SerialInsertRequest req) {
        // 驗證：唯一 ID 不能重複、日期邏輯正確
        validateInsert(req);

        // 建立活動主表記錄
        SerialActivity activity = new SerialActivity();
        activity.setActivityName(req.getActivityName());
        activity.setActivityUniqueId(req.getActivityUniqueId());
        activity.setStartDate(req.getStartDate());
        activity.setEndDate(req.getEndDate());
        activity.setQuota(req.getQuota());
        activity = activityRepo.save(activity);

        // 產生指定數量的不重複序號並儲存
        int generated = generateAndSave(activity, req.getStartDate(), req.getEndDate(), req.getQuota(), null);
        return new SerialInsertResponseData(activity.getId(), generated);
    }

    /**
     * 對現有活動追加產生額外序號。
     *
     * <p>找到現有活動後，更新其配額總數，並在相同活動下新增序號。</p>
     *
     * @param req 請求參數（活動唯一 ID、新有效期間、追加數量、備註）
     * @return 活動 ID 與本次新增序號數量
     */
    @Transactional
    public SerialInsertResponseData additionalInsertSerials(SerialAdditionalInsertRequest req) {
        // 驗證：活動必須已存在、日期邏輯正確
        validateAdditionalInsert(req);

        // 查詢現有活動（不存在則拋出業務例外）
        SerialActivity activity = activityRepo.findByActivityUniqueId(req.getActivityUniqueId())
                .orElseThrow(() -> new BusinessException("所選擇的 活動唯一 ID 無效（該活動不存在）。"));

        // 更新活動的有效期間與累積配額（原有配額 + 追加數量）
        activity.setStartDate(req.getStartDate());
        activity.setEndDate(req.getEndDate());
        activity.setQuota(activity.getQuota() + req.getQuota());
        activityRepo.save(activity);

        // 產生新序號並附上追加備註
        int generated = generateAndSave(activity, req.getStartDate(), req.getEndDate(), req.getQuota(), req.getNote());
        return new SerialInsertResponseData(activity.getId(), generated);
    }

    /**
     * 核銷指定序號（標記為已使用）。
     *
     * <p>使用悲觀鎖（{@code SELECT ... WITH (UPDLOCK)})防止同一序號
     * 在高併發下被重複核銷（Race Condition）。</p>
     *
     * @param req 請求參數（8 碼序號內容）
     * @return 序號內容與核銷時間
     */
    @Transactional
    public SerialRedeemResponseData redeemSerial(SerialRedeemRequest req) {
        // 統一轉大寫，確保大小寫不影響查詢
        String content = req.getContent().trim().toUpperCase();

        // 查詢序號並加悲觀寫鎖，防止並發核銷同一序號
        SerialDetail serial = detailRepo.findByContentWithLock(content)
                .orElseThrow(() -> new BusinessException("此序號不存在"));

        // 驗證序號狀態
        if (serial.getStatus() == SerialDetail.STATUS_USED) {
            throw new BusinessException("此序號已經被核銷使用");
        }
        if (serial.getStatus() == SerialDetail.STATUS_CANCELLED) {
            throw new BusinessException("此序號已被註銷，無法核銷");
        }

        // 驗證有效期間
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(serial.getStartDate())) {
            throw new BusinessException("此序號尚未生效");
        }
        if (now.isAfter(serial.getEndDate())) {
            throw new BusinessException("此序號已過期");
        }

        // 更新狀態為已核銷
        serial.setStatus(SerialDetail.STATUS_USED);
        serial.setUpdatedAt(now);
        detailRepo.save(serial);

        return new SerialRedeemResponseData(serial.getContent(), now.format(FMT));
    }

    /**
     * 批次註銷指定序號（標記為不可使用）。
     *
     * <p>支援部分成功：即使部分序號無法註銷，其餘序號仍會正常處理。
     * 回應中會明確列出成功與失敗清單。</p>
     *
     * @param req 請求參數（序號清單、備註原因）
     * @return 包含成功/失敗序號清單的完整回應
     */
    @Transactional
    public SerialCancelResponse cancelSerials(SerialCancelRequest req) {
        // 先驗證每個序號長度為 8 碼
        validateCancelContents(req.getContent());

        // 去除空白並轉大寫，使用 LinkedHashSet 保持輸入順序並自動去重
        Set<String> contentSet = req.getContent().stream()
                .map(c -> c.trim().toUpperCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 批次查詢並加悲觀寫鎖
        List<SerialDetail> serials = detailRepo.findByContentInWithLock(contentSet);
        // 轉為 Map 方便 O(1) 查找
        Map<String, SerialDetail> serialMap = serials.stream()
                .collect(Collectors.toMap(SerialDetail::getContent, s -> s));

        LocalDateTime now = LocalDateTime.now();
        List<String> successList = new ArrayList<>();  // 成功註銷的序號
        List<String> failList = new ArrayList<>();       // 失敗的序號（含原因）

        // 逐一判斷每個序號是否可以註銷
        for (String content : contentSet) {
            SerialDetail serial = serialMap.get(content);
            if (serial == null) {
                failList.add(content + " (此序號不存在)");
                continue;
            }
            if (serial.getStatus() == SerialDetail.STATUS_CANCELLED) {
                failList.add(content + " (此序號已被註銷，請勿重複註銷)");
                continue;
            }
            if (serial.getStatus() == SerialDetail.STATUS_USED) {
                failList.add(content + " (此序號已被核銷，無法再註銷)");
                continue;
            }
            // 可以註銷：更新狀態、備註、時間
            serial.setStatus(SerialDetail.STATUS_CANCELLED);
            serial.setNote(req.getNote());
            serial.setUpdatedAt(now);
            successList.add(content);
        }

        // 批次儲存所有成功註銷的序號
        if (!successList.isEmpty()) {
            List<SerialDetail> toUpdate = serials.stream()
                    .filter(s -> successList.contains(s.getContent()))
                    .collect(Collectors.toList());
            detailRepo.saveAll(toUpdate);
        }

        // 決定整體結果訊息
        String message = failList.isEmpty() ? "全部註銷成功"
                : successList.isEmpty() ? "全部註銷失敗"
                : "部分註銷成功";

        SerialCancelResponse.CancelData successData = new SerialCancelResponse.CancelData(String.join(",", successList));
        SerialCancelResponse.CancelData failData = new SerialCancelResponse.CancelData(String.join(",", failList));

        return new SerialCancelResponse("success", message, now.format(FMT), successData, failData);
    }

    /**
     * 驗證新增活動請求。
     * 檢查：活動唯一 ID 是否已存在、日期邏輯是否合理。
     */
    private void validateInsert(SerialInsertRequest req) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        if (activityRepo.existsByActivityUniqueId(req.getActivityUniqueId())) {
            errors.computeIfAbsent("activity_unique_id", k -> new ArrayList<>())
                    .add("活動唯一 ID 已存在，請勿重複新增。");
        }
        validateDates(req.getStartDate(), req.getEndDate(), errors);
        throwIfErrors(errors);
    }

    /**
     * 驗證追加序號請求。
     * 檢查：活動唯一 ID 必須存在、日期邏輯是否合理。
     */
    private void validateAdditionalInsert(SerialAdditionalInsertRequest req) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        if (!activityRepo.existsByActivityUniqueId(req.getActivityUniqueId())) {
            errors.computeIfAbsent("activity_unique_id", k -> new ArrayList<>())
                    .add("所選擇的 活動唯一 ID 無效（該活動不存在）。");
        }
        validateDates(req.getStartDate(), req.getEndDate(), errors);
        throwIfErrors(errors);
    }

    /**
     * 驗證日期邏輯：結束日期不能早於開始日期，也不能早於當前時間。
     *
     * @param start  開始日期
     * @param end    結束日期
     * @param errors 錯誤訊息收集容器（有錯誤時放入此 Map）
     */
    private void validateDates(LocalDateTime start, LocalDateTime end, Map<String, List<String>> errors) {
        List<String> dateErrors = new ArrayList<>();
        if (start != null && end != null && end.isBefore(start)) {
            dateErrors.add("結束日期 必須晚於或等於 開始日期。");
        }
        if (end != null && end.isBefore(LocalDateTime.now())) {
            dateErrors.add("結束日期 不能早於當前時間，否則序號將立即過期。");
        }
        if (!dateErrors.isEmpty()) errors.put("end_date", dateErrors);
    }

    /**
     * 驗證要註銷的序號清單格式。
     * 每個序號必須恰好 8 個字元。
     */
    private void validateCancelContents(List<String> contents) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (int i = 0; i < contents.size(); i++) {
            String c = contents.get(i);
            if (c == null || c.trim().length() != 8) {
                errors.computeIfAbsent("content." + i, k -> new ArrayList<>())
                        .add("序號項目 [" + c + "] 必須是 8 個字元。");
            }
        }
        throwIfErrors(errors);
    }

    /**
     * 隨機產生不重複序號並批次儲存。
     *
     * <p>序號格式：1 個大寫英文字母 + 7 位數字，例如 {@code A0001234}。</p>
     *
     * <p>產生流程：</p>
     * <ol>
     *   <li>先產生目標數量的候選序號（可能含重複）</li>
     *   <li>查詢資料庫中已存在的序號並從候選集排除</li>
     *   <li>不足時繼續補充，直到數量達標</li>
     *   <li>批次寫入資料庫</li>
     * </ol>
     *
     * @param activity  所屬活動
     * @param startDate 序號生效日
     * @param endDate   序號失效日
     * @param quota     需要產生的數量
     * @param note      備註（追加時填入原因，初次建立為 null）
     * @return 實際產生並儲存的序號數量
     */
    private int generateAndSave(SerialActivity activity, LocalDateTime startDate, LocalDateTime endDate, int quota, String note) {
        final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        Set<String> candidates = new LinkedHashSet<>();  // 使用 LinkedHashSet 保持順序並自動去重

        // 第一輪：產生候選序號
        while (candidates.size() < quota) {
            char letter = LETTERS.charAt(random.nextInt(26));
            String digits = String.format("%07d", random.nextInt(10_000_000));
            candidates.add(letter + digits);
        }

        // 查詢資料庫中已存在的序號，避免重複
        Set<String> existing = detailRepo.findExistingContents(candidates);
        candidates.removeAll(existing);

        // 第二輪：若有碰撞（序號已存在），補充缺少的數量
        while (candidates.size() < quota) {
            char letter = LETTERS.charAt(random.nextInt(26));
            String digits = String.format("%07d", random.nextInt(10_000_000));
            String c = letter + digits;
            if (!existing.contains(c)) candidates.add(c);
        }

        // 建立 SerialDetail 實體清單並批次儲存
        List<SerialDetail> details = new ArrayList<>();
        for (String content : candidates) {
            if (details.size() >= quota) break;
            SerialDetail detail = new SerialDetail();
            detail.setSerialActivity(activity);
            detail.setContent(content);
            detail.setStatus(SerialDetail.STATUS_UNUSED);  // 初始狀態：未核銷
            detail.setNote(note);
            detail.setStartDate(startDate);
            detail.setEndDate(endDate);
            details.add(detail);
        }

        detailRepo.saveAll(details);
        return details.size();
    }

    /**
     * 若 errors Map 不為空，拋出 ValidationException。
     * 統一驗證錯誤的觸發邏輯。
     */
    private void throwIfErrors(Map<String, List<String>> errors) {
        if (!errors.isEmpty()) {
            Map<String, Object> apiErrors = new LinkedHashMap<>(errors);
            throw new ValidationException(apiErrors);
        }
    }

    /**
     * 業務層驗證例外。
     *
     * <p>由 Service 內部驗證邏輯拋出（非 Bean Validation），
     * 由 {@code GlobalExceptionHandler} 統一捕捉並回傳 422 Unprocessable Entity。</p>
     */
    public static class ValidationException extends RuntimeException {
        /** 欄位名稱 → 錯誤訊息清單 的 Map */
        private final Map<String, Object> errors;

        public ValidationException(Map<String, Object> errors) {
            super("驗證失敗");
            this.errors = errors;
        }

        public Map<String, Object> getErrors() {
            return errors;
        }
    }
}
