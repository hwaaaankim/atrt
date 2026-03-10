package com.dev.Attraction.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dev.Attraction.configuration.EmailSendable;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EmailSendService implements EmailSendable {

    private final JavaMailSender javaMailSender;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailSendService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    private String[] sanitizeEmails(String[] emails) {
        if (emails == null || emails.length == 0) {
            return new String[0];
        }

        Set<String> set = new LinkedHashSet<>();
        for (String e : emails) {
            if (e == null) continue;

            String v = e.trim();
            if (v.isEmpty()) continue;

            if (!EMAIL_PATTERN.matcher(v).matches()) {
                log.warn("[MAIL] invalid email skipped: {}", v);
                continue;
            }

            set.add(v);
        }

        return set.toArray(new String[0]);
    }

    @Override
    public void sendText(String[] to, String subject, String message) {
        String[] safeTo = sanitizeEmails(to);
        if (safeTo.length == 0) {
            log.warn("[MAIL] no valid recipients (TEXT). skip send. original={}", Arrays.toString(to));
            return;
        }

        try {
            MimeMessage mime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(safeTo);
            helper.setSubject(subject);
            helper.setText(message, false);

            javaMailSender.send(mime);
            log.info("Sent TEXT email! toSize={}", safeTo.length);

        } catch (Exception e) {
            throw new IllegalStateException("메일 발송 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String[] to, String subject, String html) {
        String[] safeTo = sanitizeEmails(to);
        if (safeTo.length == 0) {
            log.warn("[MAIL] no valid recipients (HTML). skip send. original={}", Arrays.toString(to));
            return;
        }

        try {
            MimeMessage mime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(safeTo);
            helper.setSubject(subject);
            helper.setText(html, true);

            javaMailSender.send(mime);
            log.info("Sent HTML email! toSize={}", safeTo.length);

        } catch (Exception e) {
            throw new IllegalStateException("메일 발송 실패(HTML): " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlBcc(String[] bcc, String subject, String html) {
        String[] safeBcc = sanitizeEmails(bcc);
        if (safeBcc.length == 0) {
            log.warn("[MAIL] no valid recipients (BCC). skip send. original={}", Arrays.toString(bcc));
            return;
        }

        try {
            MimeMessage mime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setTo("contact@atrt.co.kr");
            helper.setBcc(safeBcc);
            helper.setSubject(subject);
            helper.setText(html, true);

            javaMailSender.send(mime);
            log.info("Sent HTML email (BCC), size={}", safeBcc.length);

        } catch (Exception e) {
            throw new IllegalStateException("메일 발송 실패(BCC): " + e.getMessage(), e);
        }
    }
}