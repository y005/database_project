package com.example.database.repository;

import com.example.database.domain.Data;
import org.apache.ibatis.annotations.Param;

public interface TestRepository {
    Data findByUserId(@Param("user_id") String user_id);
}
