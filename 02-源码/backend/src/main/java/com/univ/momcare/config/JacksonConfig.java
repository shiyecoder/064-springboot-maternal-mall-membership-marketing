package com.univ.momcare.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间格式全局统一：日期时间 yyyy-MM-dd HH:mm:ss，日期 yyyy-MM-dd，时间 HH:mm:ss
 * 前端无需再各自格式化，全系统一套口径（需求文档 7.1）
 */
@Configuration
public class JacksonConfig {
    public static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return b -> {
            b.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DT));
            b.serializerByType(LocalDate.class, new LocalDateSerializer(D));
            b.serializerByType(LocalTime.class, new LocalTimeSerializer(T));
            b.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DT));
            b.deserializerByType(LocalDate.class, new LocalDateDeserializer(D));
            b.deserializerByType(LocalTime.class, new LocalTimeDeserializer(T));
        };
    }
}
