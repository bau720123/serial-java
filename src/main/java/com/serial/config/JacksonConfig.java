package com.serial.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson JSON 序列化/反序列化全域設定。
 *
 * <p>主要目的：統一 API 中日期時間（LocalDateTime）的格式為
 * {@code "yyyy-MM-dd HH:mm:ss"}，取代 Jackson 預設的 ISO-8601 格式。</p>
 *
 * <p>{@code @Configuration}：告訴 Spring 這個類別包含 Bean 定義，
 * 啟動時會自動載入其中的 {@code @Bean} 方法。</p>
 */
@Configuration
public class JacksonConfig {

    /** 統一日期時間格式：例如 "2025-06-01 12:00:00" */
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 建立並設定全域 ObjectMapper Bean。
     *
     * <p>{@code @Primary}：當容器中有多個 ObjectMapper 時，優先使用這個。</p>
     *
     * <p>設定說明：</p>
     * <ol>
     *   <li>先註冊 JavaTimeModule（處理 Java 8 日期類型的基礎支援）</li>
     *   <li>再用自訂 Module 覆蓋 LocalDateTime 的序列化/反序列化格式</li>
     *   <li>停用時間戳輸出（不使用 Unix timestamp 數字）</li>
     *   <li>忽略 JSON 中多餘的未知欄位（避免反序列化失敗）</li>
     * </ol>
     *
     * @return 設定完成的 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 先註冊標準的 JavaTimeModule（處理 ISO 格式）
        mapper.registerModule(new JavaTimeModule());

        // 再註冊自訂格式（會覆蓋預設行為），使用 "yyyy-MM-dd HH:mm:ss"
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FMT));
        customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FMT));
        mapper.registerModule(customModule);

        // 禁止將日期輸出為毫秒數字（改為字串格式）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略 JSON 中存在但 Java 類別沒有對應欄位的屬性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    /**
     * 建立 HTTP 訊息轉換器 Bean，套用上方的 ObjectMapper 設定。
     *
     * <p>Spring MVC 使用此轉換器將 Controller 回傳的物件序列化為 JSON 回應，
     * 或將請求 Body 的 JSON 反序列化為 Java 物件。</p>
     *
     * @return 套用自訂 ObjectMapper 的 HTTP 訊息轉換器
     */
    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }
}
