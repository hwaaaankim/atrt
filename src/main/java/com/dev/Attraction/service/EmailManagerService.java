package com.dev.Attraction.service;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dev.Attraction.dto.RecipientItemRes;
import com.dev.Attraction.dto.RecipientScrollRes;
import com.dev.Attraction.dto.TemplateRes;
import com.dev.Attraction.dto.TemplateSaveReq;
import com.dev.Attraction.dto.UploadImageRes;
import com.dev.Attraction.model.EmailRecipient;
import com.dev.Attraction.model.EmailTemplate;
import com.dev.Attraction.repository.EmailRecipientRepository;
import com.dev.Attraction.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailManagerService {

    private static final long TEMPLATE_ID = 1L;

    private static final int LOOKUP_CHUNK_SIZE = 1000;
    private static final int INSERT_CHUNK_SIZE = 1000;
    private static final int DELETE_CHUNK_SIZE = 1000;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern TMP_IMG_URL_PATTERN =
            Pattern.compile("(https?://[^\\s\"'>]+)?/upload/email/tmp/([0-9a-fA-F\\-]{8,})/([^\"\\s>]+)");

    private final EmailRecipientRepository recipientRepository;
    private final EmailTemplateRepository templateRepository;
    private final EmailUploadPaths paths;
    private final EntityManager em;

    @Transactional
    public int importRecipientsFromExcel(MultipartFile excelFile) {
        if (excelFile == null || excelFile.isEmpty()) {
            throw new IllegalArgumentException("엑셀 파일이 비어있습니다.");
        }

        try {
            LinkedHashSet<String> excelEmails = extractValidUniqueEmails(excelFile);

            if (excelEmails.isEmpty()) {
                return 0;
            }

            log.info("엑셀 유효 이메일 추출 완료. count={}", excelEmails.size());

            Set<String> existingEmails = findExistingEmailsInChunks(excelEmails);

            log.info("DB 기존 이메일 조회 완료. existingCount={}", existingEmails.size());

            excelEmails.removeAll(existingEmails);

            if (excelEmails.isEmpty()) {
                log.info("신규 이메일 없음");
                return 0;
            }

            log.info("신규 이메일 저장 시작. insertCount={}", excelEmails.size());

            int inserted = batchInsertRecipients(excelEmails);

            log.info("신규 이메일 저장 완료. inserted={}", inserted);
            return inserted;

        } catch (Exception e) {
            log.error("엑셀 파싱 실패", e);
            throw new IllegalStateException("엑셀 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private LinkedHashSet<String> extractValidUniqueEmails(MultipartFile excelFile) throws IOException {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream in = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return result;
            }

            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell == null) continue;

                String email = formatter.formatCellValue(cell);
                if (email == null) continue;

                email = email.trim();
                if (email.isEmpty()) continue;

                if (!EMAIL_PATTERN.matcher(email).matches()) continue;

                email = email.toLowerCase(Locale.ROOT);
                result.add(email);
            }
        }

        return result;
    }

    private Set<String> findExistingEmailsInChunks(Set<String> emails) {
        List<String> all = new ArrayList<>(emails);
        Set<String> existing = new HashSet<>();

        for (int i = 0; i < all.size(); i += LOOKUP_CHUNK_SIZE) {
            int end = Math.min(i + LOOKUP_CHUNK_SIZE, all.size());
            List<String> chunk = all.subList(i, end);

            List<String> found = recipientRepository.findExistingEmails(chunk);
            existing.addAll(found);
        }

        return existing;
    }

    private int batchInsertRecipients(Set<String> emails) {
        List<EmailRecipient> batch = new ArrayList<>(INSERT_CHUNK_SIZE);
        int inserted = 0;

        for (String email : emails) {
            batch.add(EmailRecipient.builder()
                    .email(email)
                    .build());

            if (batch.size() >= INSERT_CHUNK_SIZE) {
                recipientRepository.saveAll(batch);
                recipientRepository.flush();
                em.clear();

                inserted += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            recipientRepository.saveAll(batch);
            recipientRepository.flush();
            em.clear();

            inserted += batch.size();
        }

        return inserted;
    }

    @Transactional(readOnly = true)
    public RecipientScrollRes getRecipients(Long cursor, int size) {
        int pageSize = Math.max(1, Math.min(size, 200));
        List<EmailRecipient> list = recipientRepository.findNext(cursor, PageRequest.of(0, pageSize));

        List<RecipientItemRes> items = list.stream()
                .map(r -> RecipientItemRes.builder()
                        .id(r.getId())
                        .email(r.getEmail())
                        .build())
                .collect(Collectors.toList());

        Long nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getId();
        boolean hasMore = items.size() == pageSize;

        return RecipientScrollRes.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    @Transactional
    public void deleteRecipient(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id가 필요합니다.");
        }

        EmailRecipient r = recipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다. id=" + id));

        recipientRepository.delete(r);
    }

    @Transactional
    public int deleteRecipients(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<Long> targetIds = ids.stream()
                .filter(v -> v != null && v > 0)
                .distinct()
                .collect(Collectors.toList());

        if (targetIds.isEmpty()) {
            return 0;
        }

        int deleted = 0;

        for (int i = 0; i < targetIds.size(); i += DELETE_CHUNK_SIZE) {
            int end = Math.min(i + DELETE_CHUNK_SIZE, targetIds.size());
            List<Long> chunk = targetIds.subList(i, end);

            deleted += recipientRepository.deleteByIdInBulk(chunk);
            recipientRepository.flush();
            em.clear();
        }

        return deleted;
    }

    @Transactional
    public int deleteAllRecipients() {
        int deleted = recipientRepository.deleteAllInBulk();
        recipientRepository.flush();
        em.clear();
        return deleted;
    }

    @Transactional(readOnly = true)
    public TemplateRes getTemplate() {
        Optional<EmailTemplate> opt = templateRepository.findById(TEMPLATE_ID);

        EmailTemplate t = opt.orElseGet(() ->
            EmailTemplate.builder()
                .id(TEMPLATE_ID)
                .subject("공지 메일")
                .html("")
                .build()
        );

        return TemplateRes.builder()
                .subject(t.getSubject())
                .html(t.getHtml())
                .build();
    }

    @Transactional
    public void saveTemplate(TemplateSaveReq req) {
        String subject = normalizeSubject(req == null ? null : req.getSubject());
        String html = (req == null || req.getHtml() == null) ? "" : req.getHtml();

        String migrated = migrateTmpImagesToContent(html);

        EmailTemplate template = templateRepository.findById(TEMPLATE_ID)
                .orElse(EmailTemplate.builder()
                        .id(TEMPLATE_ID)
                        .subject("공지 메일")
                        .html("")
                        .build());

        template.setSubject(subject);
        template.setHtml(migrated);
        templateRepository.save(template);
    }

    public String normalizeSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "공지 메일";
        }
        return subject.trim();
    }

    /**
     * 테스트 발송 등 "현재 에디터 내용"을 바로 보낼 때 사용
     * tmp 이미지가 있으면 content로 이동 후 치환
     */
    public String prepareHtmlForSend(String html) {
        return migrateTmpImagesToContent(html == null ? "" : html);
    }

    private String migrateTmpImagesToContent(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        Matcher m = TMP_IMG_URL_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();

        LocalDate today = LocalDate.now();
        Path contentDir = paths.contentDir(today);

        try {
            Files.createDirectories(contentDir);
        } catch (Exception e) {
            throw new IllegalStateException("content 디렉토리 생성 실패: " + e.getMessage(), e);
        }

        // 같은 tmp 이미지가 html에 여러 번 등장할 때 동일 URL로 치환
        Map<String, String> migratedCache = new HashMap<>();

        while (m.find()) {
            String uuid = m.group(2);
            String filename = m.group(3);
            String cacheKey = uuid + "/" + filename;

            if (migratedCache.containsKey(cacheKey)) {
                m.appendReplacement(sb, Matcher.quoteReplacement(migratedCache.get(cacheKey)));
                continue;
            }

            Path tmpFile = paths.tmpDir(uuid).resolve(filename).normalize();

            if (!Files.exists(tmpFile)) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String safeName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "_" + filename;
            Path newFile = contentDir.resolve(safeName).normalize();

            try {
                Files.move(tmpFile, newFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                log.error("tmp->content 이동 실패: {}", tmpFile, e);
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String newUrl = paths.publicUrlOf(newFile);
            migratedCache.put(cacheKey, newUrl);
            m.appendReplacement(sb, Matcher.quoteReplacement(newUrl));
        }

        m.appendTail(sb);
        return sb.toString();
    }

    public UploadImageRes uploadEditorImageToTmp(MultipartFile file, String uuid) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어있습니다.");
        }
        if (uuid == null || uuid.isEmpty()) {
            throw new IllegalArgumentException("uuid가 필요합니다.");
        }

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = "";
        int idx = original.lastIndexOf(".");
        if (idx > -1) {
            ext = original.substring(idx);
        }

        String stored = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + ext;

        Path dir = paths.tmpDir(uuid);

        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(stored).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = paths.publicUrlOf(target);
            return UploadImageRes.builder().url(url).build();

        } catch (Exception e) {
            log.error("에디터 tmp 업로드 실패", e);
            throw new IllegalStateException("에디터 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }
}