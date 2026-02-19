# 序號管理系統 - 已加註解版本

## 📝 本版本特色

此版本在原有程式碼基礎上，新增了以下學習資源：

### 1. **ANNOTATIONS_GUIDE.md** 
- 專案架構說明
- 開發流程範例
- 學習路徑建議

### 2. **CODE_COMMENTS_SUMMARY.md**
- 每個檔案的功能說明
- 關鍵程式碼片段註解
- 重要概念解釋（悲觀鎖、交易管理、Optional 等）
- 完整的學習路徑規劃

### 3. **程式碼內註解**
- 所有重要方法都有詳細的功能說明
- 關鍵邏輯有逐行註解
- 難懂的概念有額外補充說明

---

## 🎓 如何使用這個版本學習

### 步驟 1：先讀學習指南
```
1. 開啟 ANNOTATIONS_GUIDE.md（基礎架構）
2. 開啟 CODE_COMMENTS_SUMMARY.md（詳細說明）
```

### 步驟 2：從簡單的開始
```
1. 先看 Entity 類別（資料庫對應）
2. 再看 Repository（資料存取）
3. 接著看 Controller（API 端點）
4. 最後看 Service（業務邏輯）
```

### 步驟 3：動手實作
```
1. 啟動專案：mvn spring-boot:run
2. 用 Postman 測試 API
3. 觀察日誌輸出
4. 修改程式碼看效果（有 DevTools 會自動重啟）
```

---

## 📂 重點檔案導讀順序

| 順序 | 檔案 | 為什麼要先讀 |
|------|------|------------|
| 1 | `SerialDetail.java` | 最簡單的 Entity，了解 JPA 註解 |
| 2 | `SerialDetailRepository.java` | 看懂資料存取方法 |
| 3 | `SerialController.java` | 理解 API 如何接收請求 |
| 4 | `SerialService.java` | 核心業務邏輯（最重要） |
| 5 | `ApiLoggerFilter.java` | 理解 Filter 攔截機制 |
| 6 | `GlobalExceptionHandler.java` | 理解錯誤處理 |

---

## 💡 學習小技巧

### 1. 使用 IDE 的「跳轉功能」
```
在 IntelliJ IDEA 中：
- Ctrl + 點擊 → 跳到方法定義
- Ctrl + Alt + B → 查看方法被誰呼叫
- Ctrl + F12 → 查看類別結構
```

### 2. 加上自己的註解
```java
// 我的理解：這裡是在檢查序號是否已經被使用過
if (serial.getStatus() == SerialDetail.STATUS_USED) {
    throw new BusinessException("此序號已經被核銷使用");
}
```

### 3. 寫測試加深理解
```java
@Test
public void testRedeemSerial() {
    // 準備測試資料
    SerialRedeemRequest request = new SerialRedeemRequest();
    request.setContent("A1234567");
    
    // 執行核銷
    SerialRedeemResponseData response = service.redeemSerial(request);
    
    // 驗證結果
    assertEquals("A1234567", response.getSerialContent());
}
```

---

## 🎯 常見問題 FAQ

### Q1: 為什麼要用 DTO 而不直接用 Entity？
**A**: 分離 API 層和資料庫層，保護內部實作細節。例如：
- Entity 有 20 個欄位，但 API 只需要回傳 5 個
- Entity 欄位名稱可能變更，但 API 格式要保持穩定
- API 可能需要組合多個 Entity 的資料

### Q2: @Transactional 什麼時候會回滾？
**A**: 當方法拋出 **RuntimeException**（未檢查例外）時會自動回滾。
```java
@Transactional
public void test() {
    save1();  // 成功
    save2();  // 拋出 RuntimeException
    save3();  // 不會執行
    // save1() 會被回滾
}
```

### Q3: 為什麼核銷要用悲觀鎖？
**A**: 防止兩個人同時核銷同一個序號。詳見 CODE_COMMENTS_SUMMARY.md 的說明。

### Q4: Stream API 好難懂，一定要用嗎？
**A**: 不一定，可以用傳統 for 迴圈替代。但 Stream 更簡潔：
```java
// 傳統寫法
List<String> result = new ArrayList<>();
for (String s : list) {
    result.add(s.toUpperCase());
}

// Stream 寫法
List<String> result = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

---

## 🚀 下一步學習建議

1. **實作一個新 API**：例如「批次查詢序號狀態」
2. **優化現有程式碼**：例如加上快取機制
3. **寫單元測試**：使用 JUnit 和 Mockito
4. **學習 Spring Security**：加上登入認證功能

---

**開始您的學習之旅吧！** 🎓
