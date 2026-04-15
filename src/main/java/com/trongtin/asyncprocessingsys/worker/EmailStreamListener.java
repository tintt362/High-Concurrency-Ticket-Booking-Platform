package com.trongtin.asyncprocessingsys.worker;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import com.trongtin.asyncprocessingsys.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

// StreamListener<K, V>:
// K = kiểu key của stream (String)
// V = kiểu message (MapRecord<String, String, String>)
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailStreamListener
        implements StreamListener<String, MapRecord<String, String, String>> {

    private final RedisTemplate<String, String> redisTemplate;
    private final JobRepository jobRepository;
    private final EmailWorker emailJobProcessor;
    private final WebhookService webhookService;

    // Constant tên group — phải khớp với StreamConfig
    private static final String EMAIL_GROUP = "email-workers";

    // ─────────────────────────────────────────────────────
    // Method này được gọi TỰ ĐỘNG khi có message mới
    // Không cần @Scheduled, không cần poll thủ công
    //
    // record.getId()     = message ID (timestamp-sequence)
    // record.getStream() = tên stream (stream:email:high)
    // record.getValue()  = Map { "jobId": "...", "type": "EMAIL" }
    // ─────────────────────────────────────────────────────
    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String streamKey = record.getStream();
        String messageId = record.getId().getValue();

        log.info("[EmailStreamListener] Received | stream={} | msgId={}",
                streamKey, messageId);

        try {
            // Lấy jobId từ message payload
            String jobIdStr = record.getValue().get("jobId");

            if (jobIdStr == null || jobIdStr.isBlank()) {
                log.error("[EmailStreamListener] Missing jobId | msgId={}", messageId);
                // ACK để không bị stuck trong pending list
                ackMessage(streamKey, messageId);
                return;
            }

            // Xử lý job — có @Retryable bên trong
            emailJobProcessor.processJob(jobIdStr);

            // ── ACK sau khi xử lý thành công ──────────────
            // Chỉ ACK khi job xử lý XONG — không ACK sớm
            // Nếu crash trước khi ACK → message còn trong PEL
            // → PendingMessageRecovery sẽ re-deliver
            ackMessage(streamKey, messageId);
            log.info("[EmailStreamListener] ACK | msgId={} | jobId={}",
                    messageId, jobIdStr);

        } catch (Exception e) {
            log.error("[EmailStreamListener] Processing failed | msgId={} | error={}",
                    messageId, e.getMessage());

            try {
                // Lấy thông tin pending của chính message này
                PendingMessages pendingMessages = redisTemplate.opsForStream()
                        .pending(
                                streamKey,
                                EMAIL_GROUP,
                                Range.closed(messageId, messageId),
                                1
                        );

                if (!pendingMessages.isEmpty()) {
                    PendingMessage pending = pendingMessages.iterator().next();

                    long deliveryCount = pending.getTotalDeliveryCount();
                    long idleTimeMs = pending.getElapsedTimeSinceLastDelivery().toMillis();

                    log.warn("[EmailStreamListener] Retry info | msgId={} | deliveryCount={} | idleTime={}ms",
                            messageId, deliveryCount, idleTimeMs);
                }

            } catch (Exception ex) {
                log.error("[EmailStreamListener] Failed to fetch retry info | msgId={} | error={}",
                        messageId, ex.getMessage());
            }

            // KHÔNG ACK → để retry
        }
    }

    // Helper: ACK message để xóa khỏi Pending Entry List
    private void ackMessage(String streamKey, String messageId) {
        redisTemplate.opsForStream().acknowledge(
                streamKey,
                EMAIL_GROUP,
                messageId
        );
    }
}