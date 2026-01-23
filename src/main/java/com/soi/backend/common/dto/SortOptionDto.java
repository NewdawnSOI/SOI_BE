package com.soi.backend.common.dto;

import com.soi.backend.common.entity.SortDirection;
import com.soi.backend.common.entity.SortField;
import lombok.Getter;

@Getter

public class SortOptionDto {
    private SortField sortField;
    private SortDirection direction;
}
