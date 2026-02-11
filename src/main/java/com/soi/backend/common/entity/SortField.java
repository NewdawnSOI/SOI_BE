package com.soi.backend.common.entity;

import lombok.Getter;

@Getter
public enum SortField {
    CREATED_AT("createdAt"),
    PROCESSED_AT("processedAt");

    private final String field;

    SortField(String field) {
        this.field = field;
    }
}
