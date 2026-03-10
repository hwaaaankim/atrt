package com.dev.Attraction.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dev.Attraction.configuration.EmailSendable;
import com.dev.Attraction.model.EmailRecipient;
import com.dev.Attraction.model.EmailTemplate;
import com.dev.Attraction.repository.EmailRecipientRepository;
import com.dev.Attraction.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyEmailBroadcastService {

	private static final long TEMPLATE_ID = 1L;
	private static final String NOTIFY_EMAIL = "contact@atrt.co.kr";

	// 한 번에 너무 많이 넣지 않도록 작은 배치 사용
	private static final int FETCH_SIZE = 500;
	private static final int SEND_CHUNK_SIZE = 80;
	private static final long SLEEP_MILLIS_BETWEEN_CHUNKS = 1200L;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final EmailRecipientRepository recipientRepository;
	private final EmailTemplateRepository templateRepository;
	private final EmailSendable emailSendService;

	private final AtomicBoolean running = new AtomicBoolean(false);

	public String requestManualSend() {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("이미 메일 발송 작업이 진행 중입니다.");
		}

		String jobId = UUID.randomUUID().toString();

		CompletableFuture.runAsync(() -> executeManualSend(jobId));

		return jobId;
	}

	public void sendTest(String subject, String html) {
		String finalSubject = (subject == null || subject.trim().isEmpty()) ? "[TEST] 공지 메일"
				: "[TEST] " + subject.trim();

		emailSendService.sendHtml(new String[] { NOTIFY_EMAIL }, finalSubject, html == null ? "" : html);
	}

	private void executeManualSend(String jobId) {
		long totalDbRows = recipientRepository.count();
		long sentCount = 0L;
		long skippedInvalidCount = 0L;
		long failedCount = 0L;

		boolean completedAll = false;
		String stoppedReason = null;

		Long cursor = null;
		Set<String> processedEmails = new HashSet<>();

		try {
			EmailTemplate template = templateRepository.findById(TEMPLATE_ID)
					.orElse(EmailTemplate.builder().id(TEMPLATE_ID).subject("공지 메일").html("").build());

			String subject = (template.getSubject() == null || template.getSubject().trim().isEmpty()) ? "공지 메일"
					: template.getSubject().trim();

			String html = template.getHtml() == null ? "" : template.getHtml();

			while (true) {
				List<EmailRecipient> page = recipientRepository.findNextAsc(cursor, PageRequest.of(0, FETCH_SIZE));
				if (page.isEmpty()) {
					completedAll = true;
					break;
				}

				List<String> pageEmails = new ArrayList<>();

				for (EmailRecipient recipient : page) {
					String email = recipient.getEmail();

					if (email == null) {
						skippedInvalidCount++;
						continue;
					}

					email = email.trim().toLowerCase(Locale.ROOT);

					if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
						skippedInvalidCount++;
						continue;
					}

					if (!processedEmails.add(email)) {
						continue;
					}

					pageEmails.add(email);
				}

				for (int i = 0; i < pageEmails.size(); i += SEND_CHUNK_SIZE) {
					int end = Math.min(i + SEND_CHUNK_SIZE, pageEmails.size());
					List<String> chunk = pageEmails.subList(i, end);

					try {
						emailSendService.sendHtmlBcc(chunk.toArray(new String[0]), subject, html);
						sentCount += chunk.size();

					} catch (Exception e) {
						failedCount += chunk.size();
						stoppedReason = e.getMessage();
						log.error("[MAIL][{}] 발송 중단. sentCount={}, failedChunkSize={}", jobId, sentCount, chunk.size(),
								e);
						throw e;
					}

					sleepQuietly(SLEEP_MILLIS_BETWEEN_CHUNKS);
				}

				cursor = page.get(page.size() - 1).getId();
			}

		} catch (Exception e) {
			if (stoppedReason == null) {
				stoppedReason = e.getMessage();
			}
		} finally {
			try {
				sendSummaryMail(jobId, totalDbRows, sentCount, skippedInvalidCount, failedCount, completedAll,
						stoppedReason);
			} catch (Exception notifyEx) {
				log.error("[MAIL][{}] 결과 알림 메일 발송 실패", jobId, notifyEx);
			}
			running.set(false);
		}
	}

	private void sendSummaryMail(String jobId, long totalDbRows, long sentCount, long skippedInvalidCount,
			long failedCount, boolean completedAll, String stoppedReason) {

		String subject = completedAll ? "[이메일 발송 완료] " + jobId : "[이메일 발송 중단/부분완료] " + jobId;

		StringBuilder html = new StringBuilder();
		html.append("<div style='font-family:Arial,sans-serif;line-height:1.6;'>");
		html.append("<h3>이메일 발송 결과</h3>");
		html.append("<ul>");
		html.append("<li><strong>jobId</strong>: ").append(escape(jobId)).append("</li>");
		html.append("<li><strong>DB 총 대상 row 수</strong>: ").append(totalDbRows).append("</li>");
		html.append("<li><strong>실제 발송 성공 수</strong>: ").append(sentCount).append("</li>");
		html.append("<li><strong>유효하지 않아 스킵된 수</strong>: ").append(skippedInvalidCount).append("</li>");
		html.append("<li><strong>실패 처리된 수</strong>: ").append(failedCount).append("</li>");
		html.append("<li><strong>전체 완료 여부</strong>: ").append(completedAll ? "Y" : "N").append("</li>");
		html.append("</ul>");

		if (!completedAll && stoppedReason != null) {
			html.append("<p><strong>중단 사유</strong>: ").append(escape(stoppedReason)).append("</p>");
		}

		html.append("</div>");

		emailSendService.sendHtml(new String[] { NOTIFY_EMAIL }, subject, html.toString());
	}

	private void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private String escape(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}