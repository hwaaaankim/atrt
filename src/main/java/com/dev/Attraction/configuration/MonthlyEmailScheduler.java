package com.dev.Attraction.configuration;


import org.springframework.stereotype.Component;

import com.dev.Attraction.service.MonthlyEmailBroadcastService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyEmailScheduler {

    private final MonthlyEmailBroadcastService broadcastService;

    /**
     * 매월 1일 09:00 (Asia/Seoul)
     * - 초 분 시 일 월 요일
     */
    // @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
//    public void runMonthlyEmail() {
//        log.info("[MONTHLY-EMAIL] start");
//        broadcastService.sendToAllRecipients();
//        log.info("[MONTHLY-EMAIL] end");
//    }
}