package com.serial.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serial.entity.SerialLog;
import com.serial.repository.SerialLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * API 請求/回應日誌過濾器。
 *
 * <p>攔截所有 {@code /api/*} 路徑的 HTTP 請求，在業務處理完成後，
 * 將完整的請求與回應資訊寫入 {@link SerialLog} 資料庫表。</p>
 *
 * <p>設計重點：</p>
 * <ul>
 *   <li>繼承 {@code OncePerRequestFilter}：確保每個請求只執行一次（避免 Forward 時重複執行）</li>
 *   <li>{@code @Order(1)}：設為最高優先權的 Filter，最先攔截請求</li>
 *   <li>使用 {@code ContentCachingRequestWrapper} 與 {@code ContentCachingResponseWrapper}
 *       讀取 Body 內容，避免 InputStream 只能讀取一次的問題</li>
 *   <li>JSON 自動壓縮：移除多餘空白，節省資料庫儲存空間</li>
 *   <li>日誌寫入失敗時只 log error，不影響主要 API 回應</li>
 * </ul>
 *
 * <p>只對 {@code /api/*} 路徑生效，後台 {@code /admin/*} 頁面不記錄。</p>
 */
@Component
@Order(1)
public class ApiLoggerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggerFilter.class);

    /** 日誌資料存取層，用於將日誌記錄寫入資料庫 */
    private final SerialLogRepository serialLogRepository;

    /** JSON 工具，用於壓縮請求/回應的 JSON 字串 */
    private final ObjectMapper objectMapper;

    public ApiLoggerFilter(SerialLogRepository serialLogRepository, ObjectMapper objectMapper) {
        this.serialLogRepository = serialLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Filter 核心執行邏輯。
     *
     * <p>執行順序：</p>
     * <ol>
     *   <li>用 Wrapper 包裝請求/回應（使 Body 可多次讀取）</li>
     *   <li>記錄請求進入時間</li>
     *   <li>繼續執行後續 Filter 與 Controller（{@code filterChain.doFilter}）</li>
     *   <li>業務處理完成後，記錄回應時間</li>
     *   <li>讀取請求/回應 Body 並寫入資料庫日誌</li>
     *   <li>將回應 Body 複製回真實的 Response Stream（必要步驟，否則客戶端收不到回應）</li>
     * </ol>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 包裝請求與回應，使 Body 可以被多次讀取（原始 Stream 只能讀一次）
        // 請求 Body 上限設 10,000 bytes，防止超大請求佔用記憶體
        ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(request, 10_000);
        ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(response);

        // 記錄請求進入時間（在業務處理之前）
        LocalDateTime requestAt = LocalDateTime.now();

        // 繼續執行 Filter Chain（包含 Controller 業務邏輯）
        filterChain.doFilter(wrappedReq, wrappedRes);

        // 記錄回應產生時間（在業務處理之後）
        LocalDateTime responseAt = LocalDateTime.now();

        // 從 Wrapper 讀取請求/回應 Body（此時 Controller 已執行完畢）
        String reqBody = new String(wrappedReq.getContentAsByteArray(), StandardCharsets.UTF_8);
        String resBody = new String(wrappedRes.getContentAsByteArray(), StandardCharsets.UTF_8);

        // 關鍵：將回應 Body 寫回真實的 Response，否則客戶端收不到資料
        wrappedRes.copyBodyToResponse();

        // 非同步寫入日誌（寫入失敗不影響主要 API 回應）
        try {
            SerialLog logEntry = new SerialLog();
            logEntry.setApiName(resolveApiName(request.getRequestURI()));  // URI 轉中文名稱
            logEntry.setHost(resolveClientIp(request));                     // 解析真實客戶端 IP
            logEntry.setApi(buildFullUrl(request));                         // 建立完整 URL 字串
            logEntry.setRequest(compactJson(reqBody));                      // 壓縮 JSON 格式
            logEntry.setRequestAt(requestAt);
            logEntry.setResponse(compactJson(resBody));                     // 壓縮 JSON 格式
            logEntry.setResponseAt(responseAt);
            serialLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("[ApiLoggerFilter] 日誌寫入失敗: {}", e.getMessage());
        }
    }

    /**
     * 決定此 Filter 是否略過某個請求。
     *
     * @param request HTTP 請求
     * @return true 表示略過（不執行 Filter），僅處理 /api/* 路徑
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只記錄 /api/ 開頭的請求，/admin/* 等其他路徑不記錄
        return !request.getRequestURI().startsWith("/api/");
    }

    /**
     * 解析客戶端真實 IP 位址。
     *
     * <p>優先順序：X-Forwarded-For → X-Real-IP → RemoteAddr。
     * 支援反向代理（Nginx/CDN）場景，並處理 IPv6 本機位址轉換。</p>
     *
     * @param request HTTP 請求
     * @return 客戶端 IP 字串
     */
    private String resolveClientIp(HttpServletRequest request) {
        // X-Forwarded-For：反向代理（Nginx/CDN）會加上此 Header，格式為 "client, proxy1, proxy2"
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();

        // X-Real-IP：部分反向代理使用此 Header 傳遞真實 IP
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri;

        // IPv6 本機位址轉換為 IPv4 格式（本地開發常見）
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }

    /**
     * 組合完整的請求 URL（含協定、主機名稱、Port、路徑）。
     *
     * <p>例如：{@code http://localhost:8080/api/serials_redeem}。
     * 標準 Port（http:80、https:443）省略不顯示。</p>
     *
     * @param request HTTP 請求
     * @return 完整 URL 字串
     */
    private String buildFullUrl(HttpServletRequest request) {
        String scheme = request.getScheme();      // http 或 https
        String serverName = request.getServerName(); // 主機名稱（如 localhost）
        int serverPort = request.getServerPort();    // Port 號（如 8080）
        String uri = request.getRequestURI();        // 路徑（如 /api/serials_redeem）

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // 只有非標準 Port 才顯示 Port 號
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(uri);
        return url.toString();
    }

    /**
     * 將 JSON 字串壓縮為單行格式（移除換行與多餘空白）。
     *
     * <p>做法：解析 JSON → 重新序列化為緊湊格式。
     * 若輸入不是合法 JSON，直接回傳 trim 後的原始字串。
     * 空字串或 null 回傳 {@code {}}。</p>
     *
     * @param json 原始 JSON 字串
     * @return 壓縮後的 JSON 字串
     */
    private String compactJson(String json) {
        if (json == null || json.isBlank()) {
            return "{}";
        }

        try {
            // 解析 JSON 再重新序列化為緊湊格式（移除換行與縮排）
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // 若不是合法 JSON（例如純文字錯誤訊息），直接返回 trim 後的原始內容
            return json.trim();
        }
    }

    /**
     * 將 API 路徑（URI）轉換為中文名稱，方便日誌查閱。
     *
     * @param uri 請求路徑（例如 /api/serials_redeem）
     * @return 對應的中文名稱，未知路徑直接回傳 URI
     */
    private String resolveApiName(String uri) {
        return switch (uri) {
            case "/api/serials_insert" -> "批次新增序號";
            case "/api/serials_additional_insert" -> "批次追加序號";
            case "/api/serials_redeem" -> "核銷序號";
            case "/api/serials_cancel" -> "批次註銷序號";
            default -> uri;
        };
    }
}
