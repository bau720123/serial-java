# Spring Boot 序號管理系統 - 程式碼詳細註解

## 📚 本文件說明

由於程式碼檔案眾多，完整加上逐行註解會讓程式碼過於冗長。
本文件採用「**重點註解 + 概念說明**」的方式，幫助您快速理解每個元件。

---

## 🏗️ 核心架構圖

```
HTTP請求
  ↓
Controller（接收請求、驗證輸入）
  ↓
Service（業務邏輯、資料驗證）
  ↓
Repository（資料庫操作）
  ↓
Entity（資料庫實體）
```

---

## 📁 檔案功能對照表

### 1. Entity（src/main/java/com/serial/entity/）

| 檔案 | 對應資料表 | 主要作用 |
|------|-----------|---------|
| `SerialActivity.java` | serial_activity | 活動主資料（活動名稱、唯一ID、日期、配額） |
| `SerialDetail.java` | serial_detail | 序號明細（序號內容、狀態、生效日期） |
| `SerialLog.java` | serial_log | API 呼叫日誌（請求、回應、時間戳） |

**重要註解**：
- `@Entity` → 告訴 JPA 這是一個資料庫實體
- `@Table(name = "...")` → 指定對應的資料表名稱
- `@Id` + `@GeneratedValue` → 主鍵自動遞增
- `@Column(name = "...")` → 指定資料庫欄位名稱
- `@ManyToOne` → 多對一關聯（多個序號屬於一個活動）
- `@CreationTimestamp` → 建立時自動填入當前時間
- `@UpdateTimestamp` → 更新時自動填入當前時間

---

### 2. Repository（src/main/java/com/serial/repository/）

| 檔案 | 主要功能 |
|------|---------|
| `SerialActivityRepository.java` | 查詢活動（依 activity_unique_id） |
| `SerialDetailRepository.java` | 查詢序號（支援悲觀鎖） |
| `SerialLogRepository.java` | 記錄日誌 |

**重要註解**：
- `extends JpaRepository<SerialDetail, Integer>` → 自動獲得 save(), findById(), delete() 等方法
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` → 悲觀鎖，防止核銷衝突
- `@Query("SELECT ...")` → 自訂 JPQL 查詢語句

**為什麼需要悲觀鎖？**
假設兩個人同時核銷同一個序號：
```
時間 | 使用者A | 使用者B
-----|---------|--------
T1   | 讀取序號（狀態=0） | 讀取序號（狀態=0）
T2   | 檢查通過 | 檢查通過
T3   | 標記為已使用 | 標記為已使用
```
結果：兩人都核銷成功！❌

使用悲觀鎖後：
```
時間 | 使用者A | 使用者B
-----|---------|--------
T1   | 鎖定並讀取 | 等待...
T2   | 核銷成功 | 等待...
T3   | 解鎖 | 鎖定並讀取（狀態=1）
T4   |  | 檢查失敗：已被核銷
```
結果：只有一人核銷成功！✅

---

### 3. DTO（src/main/java/com/serial/dto/）

#### Request（請求物件）
- `SerialInsertRequest.java` → 新增活動 + 序號
- `SerialAdditionalInsertRequest.java` → 追加序號
- `SerialRedeemRequest.java` → 核銷序號
- `SerialCancelRequest.java` → 註銷序號

**重要註解**：
- `@NotBlank` → 字串不能為空或空白
- `@NotNull` → 不能為 null
- `@Min` / `@Max` → 數值範圍限制
- `@JsonProperty("activity_name")` → JSON 欄位名稱對應

#### Response（回應物件）
- `ApiResponse.java` → 標準 API 回應格式
- `SerialInsertResponseData.java` → 新增結果
- `SerialRedeemResponseData.java` → 核銷結果
- `SerialCancelResponse.java` → 註銷結果

**為什麼要用 DTO？**
- 分離 API 層和資料庫層
- 保護內部實作細節（不暴露 Entity）
- 方便做欄位轉換和驗證

---

### 4. Service（src/main/java/com/serial/service/）

**SerialService.java** - 核心業務邏輯

#### 關鍵方法說明

**insertSerials（批次新增序號）**
```java
@Transactional  // ← 確保所有操作在同一個交易中
public SerialInsertResponseData insertSerials(SerialInsertRequest req) {
    // 1. 驗證輸入（活動ID不能重複、日期合理性）
    validateInsert(req);
    
    // 2. 建立活動記錄
    SerialActivity activity = new SerialActivity();
    activity.setActivityName(req.getActivityName());
    // ...
    activity = activityRepo.save(activity);  // ← 儲存到資料庫
    
    // 3. 產生隨機序號（英文字母 + 7位數字）
    int generated = generateAndSave(activity, ...);
    
    // 4. 回傳結果
    return new SerialInsertResponseData(activity.getId(), generated);
}
```

**redeemSerial（核銷序號）**
```java
@Transactional
public SerialRedeemResponseData redeemSerial(SerialRedeemRequest req) {
    String content = req.getContent().trim().toUpperCase();  // ← 統一轉大寫
    
    // 使用悲觀鎖查詢，防止重複核銷
    SerialDetail serial = detailRepo.findByContentWithLock(content)
        .orElseThrow(() -> new BusinessException("此序號不存在"));
    
    // 檢查狀態
    if (serial.getStatus() == SerialDetail.STATUS_USED) {
        throw new BusinessException("此序號已經被核銷使用");
    }
    
    // 檢查日期
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(serial.getStartDate())) {
        throw new BusinessException("此序號尚未生效");
    }
    
    // 標記為已使用
    serial.setStatus(SerialDetail.STATUS_USED);
    serial.setUpdatedAt(now);
    detailRepo.save(serial);
    
    return new SerialRedeemResponseData(serial.getContent(), now.format(FMT));
}
```

**generateAndSave（產生隨機序號）**
```java
private int generateAndSave(...) {
    final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Random random = new Random();
    Set<String> candidates = new LinkedHashSet<>();  // ← 使用 Set 自動去重
    
    // 產生候選序號
    while (candidates.size() < quota) {
        char letter = LETTERS.charAt(random.nextInt(26));  // ← 隨機字母
        String digits = String.format("%07d", random.nextInt(10_000_000));  // ← 7位數字
        candidates.add(letter + digits);  // 例如：A1234567
    }
    
    // 檢查資料庫中已存在的序號
    Set<String> existing = detailRepo.findExistingContents(candidates);
    candidates.removeAll(existing);  // ← 移除重複的
    
    // 如果移除後不夠數量，繼續產生
    while (candidates.size() < quota) {
        // ... 重複上面的邏輯
    }
    
    // 批次儲存
    detailRepo.saveAll(details);
    return details.size();
}
```

---

### 5. Controller（src/main/java/com/serial/controller/）

**SerialController.java** - REST API 端點

```java
@RestController  // ← 告訴 Spring 這是 REST API 控制器
@RequestMapping("/api")  // ← 所有方法的路徑前綴
public class SerialController {

    @PostMapping("/serials_insert")  // ← 對應 POST /api/serials_insert
    public ResponseEntity<ApiResponse<SerialInsertResponseData>> insertSerials(
            @Valid @RequestBody SerialInsertRequest request) {  // ← @Valid 觸發驗證
        
        // 呼叫 Service 處理業務邏輯
        SerialInsertResponseData data = serialService.insertSerials(request);
        
        // 回傳 HTTP 201 Created + JSON 回應
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("活動與序號已成功產生", data));
    }
}
```

**SerialAdminController.java** - 後台管理

```java
@Controller  // ← 不是 @RestController，因為要回傳 HTML
@RequestMapping("/admin/serials")
public class SerialAdminController {

    @GetMapping  // ← GET /admin/serials
    public String index(...) {
        // 查詢資料
        Page<SerialDetail> list = searchSerials(...);
        
        // 放入 Model 供 Thymeleaf 使用
        model.addAttribute("list", list);
        
        // 回傳模板名稱（會去找 templates/admin/serials/index.html）
        return "admin/serials/index";
    }
    
    @GetMapping("/export")  // ← GET /admin/serials/export
    public void export(..., HttpServletResponse response) {
        // 設定 CSV 檔案下載
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"...\"");
        
        // 使用 chunk 分批查詢（每次 1000 筆）
        while (true) {
            Page<SerialDetail> page = searchSerials(...);
            // 寫入 CSV
            if (!page.hasNext()) break;
            pageNumber++;
        }
    }
}
```

---

### 6. Middleware（src/main/java/com/serial/middleware/）

**ApiLoggerFilter.java** - API 日誌過濾器

```java
@Component  // ← Spring 元件
@Order(1)  // ← 執行順序（數字越小越早執行）
public class ApiLoggerFilter extends OncePerRequestFilter {  // ← 確保每個請求只執行一次

    @Override
    protected void doFilterInternal(...) {
        // 包裝 Request 和 Response，讓我們可以重複讀取內容
        ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(response);
        
        LocalDateTime requestAt = LocalDateTime.now();  // ← 記錄請求時間
        
        // 繼續執行後續的過濾器和控制器
        filterChain.doFilter(wrappedReq, wrappedRes);
        
        LocalDateTime responseAt = LocalDateTime.now();  // ← 記錄回應時間
        
        // 讀取請求和回應的內容
        String reqBody = new String(wrappedReq.getContentAsByteArray(), StandardCharsets.UTF_8);
        String resBody = new String(wrappedRes.getContentAsByteArray(), StandardCharsets.UTF_8);
        
        // 儲存到資料庫
        SerialLog logEntry = new SerialLog();
        logEntry.setApi(buildFullUrl(request));  // ← 完整 URL
        logEntry.setRequest(compactJson(reqBody));  // ← 壓縮 JSON
        // ...
        serialLogRepository.save(logEntry);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只記錄 /api/* 的請求
        return !request.getRequestURI().startsWith("/api/");
    }
}
```

---

### 7. Exception（src/main/java/com/serial/exception/）

**GlobalExceptionHandler.java** - 全域例外處理

```java
@RestControllerAdvice  // ← 全域例外處理器
public class GlobalExceptionHandler {

    // 處理 Bean Validation 驗證失敗（422 錯誤）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleBeanValidation(...) {
        // 收集所有驗證錯誤
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            // errors = {"activity_name": ["活動名稱 欄位為必填。"]}
        });
        
        // 回傳 422 + 錯誤訊息
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.validationError(errors));
    }
    
    // 處理業務邏輯錯誤（400 錯誤）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    // 處理所有未預期的錯誤（500 錯誤）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統發生非預期錯誤，請稍後再試。"));
    }
}
```

---

### 8. Config（src/main/java/com/serial/config/）

**JacksonConfig.java** - JSON 序列化設定

```java
@Configuration  // ← Spring 設定類別
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean  // ← 告訴 Spring 這個方法會產生一個 Bean
    @Primary  // ← 主要的 ObjectMapper（優先使用）
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 註冊 Java Time Module（處理 LocalDateTime）
        mapper.registerModule(new JavaTimeModule());
        
        // 自訂序列化格式（覆蓋預設行為）
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FMT));
        customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FMT));
        mapper.registerModule(customModule);
        
        // 關閉「將日期序列化為時間戳」（我們要字串格式）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
```

**為什麼需要這個設定？**
預設情況下，Jackson 會把 `LocalDateTime` 序列化成：
```json
{
  "created_at": "2026-02-19T10:30:00"  // ← ISO 8601 格式
}
```

設定後變成：
```json
{
  "created_at": "2026-02-19 10:30:00"  // ← 我們要的格式
}
```

---

## 🔧 關鍵技術說明

### 1. @Transactional 交易管理

```java
@Transactional
public void someMethod() {
    // 在這個方法中的所有資料庫操作
    // 要麼全部成功，要麼全部回滾
    
    activityRepo.save(activity);     // 操作 1
    detailRepo.saveAll(details);     // 操作 2
    
    // 如果操作 2 失敗，操作 1 也會被回滾
}
```

### 2. Optional 空值處理

```java
// 舊式寫法（容易 NullPointerException）
SerialDetail detail = detailRepo.findByContent(content);
if (detail == null) {
    throw new BusinessException("序號不存在");
}

// 新式寫法（使用 Optional）
SerialDetail detail = detailRepo.findByContent(content)
    .orElseThrow(() -> new BusinessException("序號不存在"));
```

### 3. Stream API 資料處理

```java
// 將序號列表轉為大寫並去重
Set<String> contentSet = req.getContent().stream()
    .map(c -> c.trim().toUpperCase())  // ← 轉換
    .collect(Collectors.toCollection(LinkedHashSet::new));  // ← 收集
```

---

## 💡 學習路徑建議

### 第 1 週：基礎概念
1. 理解 MVC 架構
2. 學習 Spring Boot 基本註解
3. 了解 JPA / Hibernate 基礎

### 第 2 週：資料層
1. 研究 Entity 類別
2. 理解 Repository 查詢方法
3. 學習悲觀鎖的用途

### 第 3 週：業務層
1. 閱讀 SerialService.java
2. 理解每個方法的業務邏輯
3. 學習交易管理 @Transactional

### 第 4 週：控制層
1. 研究 REST API 設計
2. 理解 MVC Controller 差異
3. 學習全域例外處理

---

## 📚 推薦資源

- **Spring Boot 官方教學**：https://spring.io/guides
- **JPA 查詢方法命名**：https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods
- **Bean Validation 規範**：https://beanvalidation.org/

---

**祝學習順利！有任何問題都可以在程式碼中加上註解或提問。** 🎓
