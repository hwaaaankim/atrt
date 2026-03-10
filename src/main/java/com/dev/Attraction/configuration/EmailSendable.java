package com.dev.Attraction.configuration;


public interface EmailSendable {
    void sendText(String[] to, String subject, String message);
    void sendHtml(String[] to, String subject, String html);

    // ✅ 추가
    void sendHtmlBcc(String[] bcc, String subject, String html);
}