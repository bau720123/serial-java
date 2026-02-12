# 序號管理系統 — Spring Boot 4.0.2 (No Lombok)

完整 Java 25 相容版本，**不使用 Lombok**，所有程式碼手動實作。

## 技術棧

| 項目 | 版本 / 技術 |
|------|------------|
| 框架 | **Spring Boot 4.0.2** |
| Java | **Java 25** (完整支援) |
| Web 容器 | Tomcat / Servlet 6.1 (Jakarta EE 11) |
| ORM | Spring Data JPA + Hibernate (JPA 3.2) |
| 驗證 | Bean Validation 3.1 (jakarta.validation) |
| JSON | Jackson 3 |
| 資料庫 | SQL Server (mssql-jdbc) |
| 測試 | JUnit Jupiter 6 |
| 並發 | Virtual Threads (Java 25 原生支援) |
| **依賴管理** | **無 Lombok** ✅ |

---

## 為何移除 Lombok？

1. **Java 25 相容性**：Lombok 1.18.36 尚未完全支援 Java 25 編譯器 API
2. **編譯速度**：手動實作 getter/setter 編譯速度更快
3. **IDE 整合**：完全標準 Java 程式碼，IDE 自動完成更精準
4. **除錯友善**：Stack trace 完全可讀，無需 delombok
5. **長期維護**：無需擔心 Lombok 版本更新延遲

---

## 快速開始

### 環境需求
- Java 25+ ✅
- Maven 3.9+
- SQL Server 2019+

### 1. 建立資料庫
```sql
CREATE DATABASE SerialDB;
GO
USE SerialDB;
-- 執行 src/main/resources/schema.sql
```

### 2. 修改連線設定
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SerialDB;...
spring.datasource.username=sa
spring.datasource.password=YourPassword123
```

### 3. 編譯與啟動
```powershell
mvn clean compile
mvn spring-boot:run
```

---

## API 端點

| 方法 | 路徑 | 說明 | 狀態碼 |
|------|------|------|--------|
| POST | `/api/serials_insert` | 批次新增序號 | 201 |
| POST | `/api/serials_additional_insert` | 批次追加序號 | 201 |
| POST | `/api/serials_redeem` | 核銷序號 | 200 |
| POST | `/api/serials_cancel` | 批次註銷序號 | 200 |

---

## 專案結構

```
src/main/java/com/serial/
├── SerialManagementApplication.java
├── config/
│   └── JacksonConfig.java
├── controller/
│   └── SerialController.java           ← 4 個 REST 端點
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
```

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

## 測試

```powershell
mvn test
```

---

## 程式碼統計

| 檔案數 | 行數 (估計) |
|--------|------------|
| Entity | 3 個 × 120 行 = 360 行 |
| DTO | 8 個 × 50 行 = 400 行 |
| Service | 1 個 × 260 行 = 260 行 |
| 其他 | ~500 行 |
| **總計** | **~1,520 行** |

相較 Lombok 版本增加約 30% 程式碼量，但**零外部依賴**。
