package com.serial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * API 日誌記錄表 Entity
 * DDL: serial_log
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName;

    @Column(name = "host", nullable = false, length = 50)
    private String host;

    @Column(name = "api", nullable = false, length = 255)
    private String api;

    @Column(name = "request", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String request;

    @Column(name = "request_at", nullable = false)
    private LocalDateTime requestAt;

    @Column(name = "response", columnDefinition = "NVARCHAR(MAX)")
    private String response;

    @Column(name = "response_at")
    private LocalDateTime responseAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
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
