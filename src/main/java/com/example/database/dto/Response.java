package com.example.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements Serializable {
    HttpStatus status;
    Object data;
    Long time;
    LocalDateTime now;

    public static Response ok(Object data, long start) {
        return Response.builder()
                .status(HttpStatus.OK)
                .data(data)
                .time(System.currentTimeMillis() - start)
                .now(LocalDateTime.now())
                .build();
    }
}
