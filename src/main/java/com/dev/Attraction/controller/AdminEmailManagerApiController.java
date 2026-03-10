package com.dev.Attraction.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dev.Attraction.dto.EmailTestSendReq;
import com.dev.Attraction.dto.RecipientBulkDeleteReq;
import com.dev.Attraction.dto.RecipientScrollRes;
import com.dev.Attraction.dto.TemplateRes;
import com.dev.Attraction.dto.TemplateSaveReq;
import com.dev.Attraction.dto.UploadImageRes;
import com.dev.Attraction.service.EmailManagerService;
import com.dev.Attraction.service.MonthlyEmailBroadcastService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/emailManager")
@RequiredArgsConstructor
public class AdminEmailManagerApiController {

    private final EmailManagerService emailManagerService;
    private final MonthlyEmailBroadcastService monthlyEmailBroadcastService;

    @PostMapping(value = "/recipients/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadRecipientsExcel(@RequestPart("file") MultipartFile file) {
        int inserted = emailManagerService.importRecipientsFromExcel(file);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("inserted", inserted);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recipients")
    public RecipientScrollRes recipients(
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", defaultValue = "60") int size
    ) {
        return emailManagerService.getRecipients(cursor, size);
    }

    @DeleteMapping("/recipients/{id}")
    public ResponseEntity<?> deleteRecipient(@PathVariable("id") Long id) {
        emailManagerService.deleteRecipient(id);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/recipients/bulk-delete")
    public ResponseEntity<?> bulkDeleteRecipients(@RequestBody RecipientBulkDeleteReq req) {
        int deleted = emailManagerService.deleteRecipients(req == null ? null : req.getIds());

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("deleted", deleted);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/recipients/all")
    public ResponseEntity<?> deleteAllRecipients() {
        int deleted = emailManagerService.deleteAllRecipients();

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("deleted", deleted);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/template")
    public TemplateRes template() {
        return emailManagerService.getTemplate();
    }

    @PostMapping("/template")
    public ResponseEntity<?> saveTemplate(@RequestBody TemplateSaveReq req) {
        emailManagerService.saveTemplate(req);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNow() {
        try {
            String jobId = monthlyEmailBroadcastService.requestManualSend();

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("jobId", jobId);
            res.put("message", "발송이 시작되었습니다. 완료 또는 중단 결과는 contact@atrt.co.kr 로 전송됩니다.");
            return ResponseEntity.ok(res);

        } catch (IllegalStateException e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
        }
    }

    @PostMapping("/send/test")
    public ResponseEntity<?> sendTest(@RequestBody EmailTestSendReq req) {
        String subject = emailManagerService.normalizeSubject(req == null ? null : req.getSubject());
        String html = emailManagerService.prepareHtmlForSend(req == null ? "" : req.getHtml());

        monthlyEmailBroadcastService.sendTest(subject, html);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "contact@atrt.co.kr 로 테스트 메일을 발송했습니다.");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/editor/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadImageRes uploadEditorImage(
            @RequestPart("upload") MultipartFile upload,
            @RequestParam("uuid") String uuid
    ) {
        return emailManagerService.uploadEditorImageToTmp(upload, uuid);
    }

    @GetMapping("/uuid")
    public Map<String, String> uuid() {
        Map<String, String> res = new HashMap<>();
        res.put("uuid", UUID.randomUUID().toString());
        return res;
    }
}