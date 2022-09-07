package com.example.database.controller;

import com.example.database.domain.Data;
import com.example.database.dto.Response;
import com.example.database.service.DefaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final DefaultService testService;

    @GetMapping
    public Response get(@RequestParam String id) {
        long start = System.currentTimeMillis();
        Data data = testService.get(id);
        return Response.ok(data, start);
    }
}
