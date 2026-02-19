# Spring Boot 序號管理系統 - 程式碼註解說明

本文件提供整個專案的架構說明和關鍵程式碼註解，幫助您快速理解每個元件的作用。

---

## 📂 專案架構說明

###

 1. **Entity（實體層）**
- `SerialActivity.java` - 活動主表（對應資料庫 serial_activity）
- `SerialDetail.java` - 序號明細表（對應資料庫 serial_detail）
- `SerialLog.java` - API 日誌表（對應資料庫 serial_log）

**作用**：這些類別直接對應資料庫的表格，使用 JPA 註解來定義欄位、關聯和索引。

---

### 2. **Repository（資料存取層）**
- `SerialActivityRepository.java` - 活動資料存取
- `SerialDetailRepository.java` - 序號資料存取
- `SerialLogRepository.java` - 日誌資料存取

**作用**：提供資料庫 CRUD 操作，繼承 JpaRepository 自動獲得基本的增刪改查功能。

---

### 3. **DTO（資料傳輸物件）**
- `request/` - 接收前端請求的資料格式
- `response/` - 回傳給前端的資料格式

**作用**：分離 API 層和資料庫層的資料結構，保護內部實作細節。

---

### 4. **Service（業務邏輯層）**
- `SerialService.java` - 核心業務邏輯（序號生成、核銷、註銷）

**作用**：實作所有業務規則和驗證邏輯，是整個系統的核心。

---

### 5. **Controller（控制器層）**
- `SerialController.java` - REST API 端點（4 個 API）
- `admin/SerialAdminController.java` - 後台管理介面

**作用**：接收 HTTP 請求，呼叫 Service 處理，返回 HTTP 回應。

---

### 6. **Middleware（中介層）**
- `ApiLoggerFilter.java` - API 日誌紀錄過濾器

**作用**：攔截所有 /api/* 請求，記錄到資料庫。

---

### 7. **Exception（例外處理）**
- `BusinessException.java` - 業務邏輯例外
- `GlobalExceptionHandler.java` - 全域例外處理器

**作用**：統一處理所有錯誤，回傳標準化的錯誤訊息。

---

### 8. **Config（設定）**
- `JacksonConfig.java` - JSON 序列化設定

**作用**：設定日期時間格式為 "yyyy-MM-dd HH:mm:ss"。

---

## 🔑 關鍵概念說明

### JPA 註解
- `@Entity` - 標記這是一個資料庫實體類別
- `@Table` - 指定對應的資料庫表格名稱
- `@Id` - 標記主鍵欄位
- `@GeneratedValue` - 自動產生主鍵值（通常是 AUTO_INCREMENT）
- `@Column` - 定義資料庫欄位屬性
- `@ManyToOne` / `@OneToMany` - 定義表格之間的關聯

### Spring 註解
- `@RestController` - 標記這是一個 REST API 控制器
- `@Controller` - 標記這是一個 MVC 控制器（回傳 HTML 頁面）
- `@Service` - 標記這是一個業務邏輯服務
- `@Repository` - 標記這是一個資料存取層
- `@Component` - 標記這是一個 Spring 元件
- `@Transactional` - 標記這個方法需要資料庫交易支援

### 驗證註解
- `@NotBlank` - 欄位不能為空白
- `@NotNull` - 欄位不能為 null
- `@Min` / `@Max` - 數值範圍限制
- `@Size` - 集合或字串長度限制

---

## 💡 重要設計模式

### 1. **Repository Pattern（資料存取模式）**
不直接在 Service 中寫 SQL，而是透過 Repository 介面操作資料庫。

### 2. **DTO Pattern（資料傳輸物件模式）**
API 的輸入輸出使用專門的 DTO，不直接暴露 Entity。

### 3. **Service Layer Pattern（服務層模式）**
業務邏輯集中在 Service，Controller 只負責接收請求和回傳回應。

### 4. **Global Exception Handling（全域例外處理）**
使用 `@RestControllerAdvice` 統一處理所有例外。

---

## 📝 開發流程範例

假設要新增一個「查詢序號狀態」的 API：

1. **定義 DTO**
   - 建立 `SerialStatusRequest.java` （輸入）
   - 建立 `SerialStatusResponse.java` （輸出）

2. **在 Repository 新增方法**
   ```java
   Optional<SerialDetail> findByContent(String content);
   ```

3. **在 Service 實作業務邏輯**
   ```java
   public SerialStatusResponse checkStatus(SerialStatusRequest req) {
       SerialDetail detail = repo.findByContent(req.getContent())
           .orElseThrow(() -> new BusinessException("序號不存在"));
       return new SerialStatusResponse(detail.getStatus());
   }
   ```

4. **在 Controller 新增端點**
   ```java
   @PostMapping("/serials_status")
   public ResponseEntity<ApiResponse<SerialStatusResponse>> checkStatus(
       @Valid @RequestBody SerialStatusRequest req) {
       SerialStatusResponse data = service.checkStatus(req);
       return ResponseEntity.ok(ApiResponse.success("查詢成功", data));
   }
   ```

---

## 🎓 學習建議

1. **從 Entity 開始理解** - 先看懂資料庫結構
2. **再看 Repository** - 了解如何存取資料
3. **接著看 Service** - 理解業務邏輯
4. **最後看 Controller** - 了解 API 如何運作

---

## 📚 延伸閱讀

- Spring Boot 官方文件：https://spring.io/projects/spring-boot
- Spring Data JPA 文件：https://spring.io/projects/spring-data-jpa
- Bean Validation 規範：https://beanvalidation.org/

---

**祝學習順利！** 🚀
