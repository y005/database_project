package com.example.database.service;

import com.example.database.domain.Data;
import com.example.database.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("cache")
@Slf4j
public class CacheService implements TestService {
    private final TestRepository testRepository;

    @Override
    @Cacheable("test")
    public Data get(String id) {
        log.info("cache service");
        return testRepository.findByUserId(id);
    }
}
