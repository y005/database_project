package com.example.database.service;

import com.example.database.domain.Data;
import com.example.database.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("default")
@Slf4j
public class DefaultService implements TestService {
    private final TestRepository testRepository;

    @Override
    public Data get(String id) {
        log.info("default service");
        return testRepository.findByUserId(id);
    }
}
