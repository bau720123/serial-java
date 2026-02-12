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

@Component
@Order(1)
public class ApiLoggerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggerFilter.class);
    private final SerialLogRepository serialLogRepository;
    private final ObjectMapper objectMapper;

    public ApiLoggerFilter(SerialLogRepository serialLogRepository, ObjectMapper objectMapper) {
        this.serialLogRepository = serialLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(request, 10_000);
        ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(response);

        LocalDateTime requestAt = LocalDateTime.now();

        filterChain.doFilter(wrappedReq, wrappedRes);

        LocalDateTime responseAt = LocalDateTime.now();

        String reqBody = new String(wrappedReq.getContentAsByteArray(), StandardCharsets.UTF_8);
        String resBody = new String(wrappedRes.getContentAsByteArray(), StandardCharsets.UTF_8);

        wrappedRes.copyBodyToResponse();

        try {
            SerialLog logEntry = new SerialLog();
            logEntry.setApiName(resolveApiName(request.getRequestURI()));
            logEntry.setHost(resolveClientIp(request));
            logEntry.setApi(buildFullUrl(request));  // ✅ 修正：完整 URL
            logEntry.setRequest(compactJson(reqBody));  // ✅ 修正：壓縮 JSON
            logEntry.setRequestAt(requestAt);
            logEntry.setResponse(compactJson(resBody));  // ✅ 修正：壓縮 JSON
            logEntry.setResponseAt(responseAt);
            serialLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("[ApiLoggerFilter] 日誌寫入失敗: {}", e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri;
        
        // ✅ 修正：IPv6 localhost 轉換為 IPv4
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }

    // ✅ 新增：建立完整 URL
    private String buildFullUrl(HttpServletRequest request) {
        String scheme = request.getScheme();  // http 或 https
        String serverName = request.getServerName();  // localhost
        int serverPort = request.getServerPort();  // 8080
        String uri = request.getRequestURI();  // /api/serials_redeem
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        // 只有非標準 port 才加上 port number
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        url.append(uri);
        return url.toString();
    }

    // ✅ 新增：壓縮 JSON（移除換行和多餘空白）
    private String compactJson(String json) {
        if (json == null || json.isBlank()) {
            return "{}";
        }
        
        try {
            // 解析 JSON 再重新序列化為緊湊格式
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // 如果不是有效的 JSON，直接返回原始內容
            return json.trim();
        }
    }

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
