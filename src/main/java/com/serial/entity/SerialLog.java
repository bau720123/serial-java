package com.serial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * API 請求/回應日誌記錄表 Entity，對應資料庫表 {@code serial_log}。
 *
 * <p>每次呼叫 {@code /api/*} 端點時，{@link com.serial.middleware.ApiLoggerFilter}
 * 會自動建立一筆日誌，記錄完整的請求與回應內容，方便事後追蹤與稽核。</p>
 *
 * <p>索引說明：</p>
 * <ul>
 *   <li>{@code request_at}：方便按時間範圍查詢日誌</li>
 *   <li>{@code api_name}：方便按 API 名稱篩選特定操作的日誌</li>
 * </ul>
 */
@Entity
@Table(
    name = "serial_log",
    indexes = {
        @Index(name = "IX_serial_log_request_at", columnList = "request_at"),
        @Index(name = "IX_serial_log_api_name", columnList = "api_name")
    }
)
public class SerialLog {

    /** 自動遞增主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** API 中文名稱，例如：「核銷序號」、「批次新增序號」（由 ApiLoggerFilter 解析 URI 後填入） */
    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName;

    /** 呼叫端 IP 位址（支援 X-Forwarded-For / X-Real-IP Header，透過反向代理時也能取得真實 IP） */
    @Column(name = "host", nullable = false, length = 50)
    private String host;

    /** 完整的請求 URL，例如：{@code http://localhost:8080/api/serials_redeem} */
    @Column(name = "api", nullable = false, length = 255)
    private String api;

    /** 請求 Body 的 JSON 內容（壓縮格式，移除多餘空白） */
    @Column(name = "request", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String request;

    /** 收到請求的時間點（進入 Filter 時記錄） */
    @Column(name = "request_at", nullable = false)
    private LocalDateTime requestAt;

    /** 回應 Body 的 JSON 內容（壓縮格式）；若回應為空則儲存 {@code {}} */
    @Column(name = "response", columnDefinition = "NVARCHAR(MAX)")
    private String response;

    /** 回傳回應的時間點（業務處理完成後記錄，可計算處理時間） */
    @Column(name = "response_at")
    private LocalDateTime responseAt;

    /** 日誌記錄建立時間，由 Hibernate 自動設定 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 無參建構函式（JPA 規範要求）
    public SerialLog() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public LocalDateTime getRequestAt() { return requestAt; }
    public void setRequestAt(LocalDateTime requestAt) { this.requestAt = requestAt; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public LocalDateTime getResponseAt() { return responseAt; }
    public void setResponseAt(LocalDateTime responseAt) { this.responseAt = responseAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 以 id 作為實體相等性的依據（JPA 最佳實踐）。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
