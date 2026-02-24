# 序號管理系統 — Spring Boot 4.0.2

完整 Java 25 相容版本

## 技術棧

| 項目 | 版本 / 技術 |
|------|------------|
| 框架 | **Spring Boot 4.0.2** |
| Java | **Java 25**（完整支援）|
| Web 容器 | Tomcat / Servlet 6.1（Jakarta EE 11）|
| ORM | Spring Data JPA + Hibernate（JPA 3.2）|
| 模板引擎 | Thymeleaf（後台介面）|
| 驗證 | Bean Validation 3.1（jakarta.validation）|
| JSON | Jackson 3 |
| 資料庫 | SQL Server（mssql-jdbc）|
| 測試 | JUnit Jupiter 6（暫時沒用到） |
| 並發 | Virtual Threads（Java 25 原生支援）|

---

## 🚀 快速開始

### 環境需求
- Java 25+ ✅
- Maven 3.9+
- SQL Server 2019+

### 1. Clone 專案
```bash
git clone <YOUR_REPOSITORY_URL>
cd serial-java
```

### 2. 設定資料庫連線
```bash
# 複製範例配置檔案
cp application.properties.example src/main/resources/application.properties

# 編輯 application.properties，填入實際的資料庫資訊
# - spring.datasource.url
# - spring.datasource.username
# - spring.datasource.password
```

### 3. 建立資料庫
```sql
CREATE DATABASE SerialDB;
GO
USE SerialDB;
-- 執行 src/main/resources/schema.sql
```

### 4. 編譯與啟動
```powershell
mvn clean spring-boot:run
```

---

## 📍 訪問網址

| 功能 | URL | 說明 |
|------|-----|------|
| 後台管理 | `http://localhost:8080/admin/serials` | 序號列表查詢與匯出 |
| API 文件 | 見下方 API 端點 | REST API 介面 |

---

## API 端點

| 方法 | 路徑 | 說明 | 狀態碼 |
|------|------|------|--------|
| POST | `/api/serials_insert` | 批次新增序號 | 201 |
| POST | `/api/serials_additional_insert` | 批次追加序號 | 201 |
| POST | `/api/serials_redeem` | 核銷序號 | 200 |
| POST | `/api/serials_cancel` | 批次註銷序號 | 200 |

---

## 📂 專案結構

```
src/main/java/com/serial/
├── SerialManagementApplication.java    ← 跟目錄檔案，類似Laravel的index.php
├── config/
│   └── JacksonConfig.java
├── controller/
│   ├── SerialController.java           ← 4 個 REST API
│   └── admin/
│       └── SerialAdminController.java  ← 後台 Controller
├── service/
│   └── SerialService.java              ← 核心業務邏輯
├── entity/
│   ├── SerialActivity.java             ← 手動 getter/setter
│   ├── SerialDetail.java               ← 手動 getter/setter
│   └── SerialLog.java                  ← 手動 getter/setter
├── repository/
│   ├── SerialActivityRepository.java
│   ├── SerialDetailRepository.java
│   └── SerialLogRepository.java
├── dto/
│   ├── request/  (4 個 Request DTO，手動實作)
│   └── response/ (4 個 Response DTO，手動實作)
├── exception/
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
└── middleware/
    └── ApiLoggerFilter.java            ← Laravel api.logger 等價

src/main/resources/
├── templates/
│   └── admin/
│       └── serials/
│           └── index.html              ← Thymeleaf 模板
├── application.properties.example      ← 配置範例
└── schema.sql                          ← DDL
```

---

## ⚙️ 重要提醒

### 🔒 安全性
- **application.properties** 包含資料庫密碼，已加入 `.gitignore`
- 團隊成員需自行複製 `application.properties.example` 並設定
- 切勿將 `application.properties` 提交到 Git

### 📦 Maven 建置
- **target/** 目錄已加入 `.gitignore`（Maven 編譯輸出）
- 首次 clone 後需執行 `mvn clean install`

### 🎨 IDE 設定
- IntelliJ IDEA：`.idea/` 已忽略
- Eclipse：`.project`, `.classpath` 已忽略
- VS Code：`.vscode/` 已忽略

---

## Java 25 特性

### 虛擬執行緒 (Virtual Threads)
```properties
# application.properties
spring.threads.virtual.enabled=true
```
啟用後，所有 HTTP 請求與資料庫連線自動使用虛擬執行緒，大幅提升高併發效能。

### 悲觀鎖防重複核銷
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SerialDetail s WHERE s.content = :content")
Optional<SerialDetail> findByContentWithLock(@Param("content") String content);
```

---

## 後台功能

### 列表查詢
- 活動名稱（模糊搜尋）
- 序號內容（精確搜尋）
- 狀態篩選（未使用/已使用/已註銷）
- 建立日期範圍
- 分頁顯示（預設每頁 10 筆）

### CSV 匯出
- UTF-8 BOM 編碼（Excel 中文相容）
- 包含所有搜尋條件的資料
- 檔名格式：`serials_yyyyMMdd_HHmmss.csv`

---

## 🧪 測試

```powershell
mvn test
```

---

## 📝 程式碼統計

| 檔案數 | 行數 (估計) |
|--------|------------|
| Entity | 3 個 × 120 行 = 360 行 |
| DTO | 8 個 × 50 行 = 400 行 |
| Service | 1 個 × 260 行 = 260 行 |
| Controller | 2 個 × 150 行 = 300 行 |
| 其他 | ~700 行 |
| **總計** | **~2,020 行** |

相較 Lombok 版本增加約 30% 程式碼量，但**零外部依賴**。

---

## 🔄 從 Laravel 移植

本專案從 Laravel 10 完整移植而來，功能 100% 對等：
- ✅ 4 個 REST API（新增、追加、核銷、註銷）
- ✅ 後台管理介面（列表查詢、CSV 匯出）
- ✅ API 日誌追蹤（等價於 Laravel api.logger middleware）
- ✅ 全域錯誤處理（422 驗證錯誤 / 400 業務邏輯錯誤）

---

## 📄 License

MIT
