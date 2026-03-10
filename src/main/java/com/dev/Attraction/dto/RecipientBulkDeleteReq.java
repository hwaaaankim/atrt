package com.dev.Attraction.dto;

import java.util.List;

import lombok.Data;

@Data
public class RecipientBulkDeleteReq {
    private List<Long> ids;
}