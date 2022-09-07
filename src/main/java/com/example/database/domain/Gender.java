package com.example.database.domain;

public enum Gender {
    MEN("men"),
    WOMEN("women");

    private final String type;

    Gender(String type) {
        this.type = type;
    }
}
