package com.blog.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类
 * 用于配置日期时间的序列化和反序列化格式
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义LocalDateTime反序列化器
     * 支持多种格式，包括带时区的ISO 8601格式
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getText();

            try {
                // 尝试解析标准LocalDateTime格式
                return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e1) {
                try {
                    // 尝试解析带时区的格式（ISO_OFFSET_DATE_TIME）
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    return offsetDateTime.toLocalDateTime();
                } catch (Exception e2) {
                    try {
                        // 尝试解析带时区的格式（ISO_ZONED_DATE_TIME）
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                        return zonedDateTime.toLocalDateTime();
                    } catch (Exception e3) {
                        // 尝试自定义格式
                        try {
                            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e4) {
                            throw new IOException("无法解析日期时间: " + dateString, e4);
                        }
                    }
                }
            }
        }
    }

    /**
     * 配置ObjectMapper，支持多种日期时间格式
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 使用自定义的LocalDateTime反序列化器
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        // 配置LocalDateTime的序列化器，使用标准格式
        DateTimeFormatter serializer = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(serializer));

        // 注册JavaTimeModule
        objectMapper.registerModule(javaTimeModule);

        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
