package com.dev.Attraction.dto;

import lombok.Data;

@Data
public class EmailTestSendReq {
    private String subject;
    private String html;
}