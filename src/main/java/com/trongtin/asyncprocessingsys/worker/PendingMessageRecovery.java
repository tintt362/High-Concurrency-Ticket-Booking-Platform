package com.trongtin.asyncprocessingsys.worker;

import com.trongtin.asyncprocessingsys.config.StreamConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PendingMessageRecovery {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailStreamListener emailStreamListener;


    private static final long PENDING_TIMEOUT_MS = 30_000;

    // Max số lần retry — tránh loop vô hạn
    private static final int MAX_DELIVERY_COUNT = 5;

    private static final List<String> STREAMS = List.of(
            StreamConfig.EMAIL_STREAM_HIGH,
            StreamConfig.EMAIL_STREAM_MEDIUM,
            StreamConfig.EMAIL_STREAM_LOW
    );

    @Scheduled(fixedDelay = 30_000)
    public void recoverPendingMessages() {
        for (String streamKey : STREAMS) {
            recoverStream(streamKey);
        }
    }

    private void recoverStream(String streamKey) {
        try {

            PendingMessages pendingMessages = redisTemplate
                    .opsForStream()
                    .pending(
                            streamKey,
                            StreamConfig.EMAIL_GROUP,
                            Range.unbounded(),
                            10L  // tối đa 10 message mỗi lần
                    );

            if (pendingMessages.isEmpty()) return;

            log.debug("[Recovery] Found {} pending on '{}'",
                    pendingMessages.size(), streamKey);

            for (PendingMessage pending : pendingMessages) {
                processPending(streamKey, pending);
            }

        } catch (Exception e) {
            log.error("[Recovery] Error scanning '{}': {}",
                    streamKey, e.getMessage());
        }
    }

    private void processPending(String streamKey,
                                PendingMessage pending) {
        long pendingMs = pending.getElapsedTimeSinceLastDelivery()
                .toMillis();
        long deliveryCount = pending.getTotalDeliveryCount();
        String messageId = pending.getId().getValue();

        // Chưa đủ thời gian → bỏ qua, có thể worker vẫn đang xử lý
        if (pendingMs < PENDING_TIMEOUT_MS) return;

        log.warn("[Recovery] Stuck message | stream={} | msgId={} | pending={}ms | retries={}",
                streamKey, messageId, pendingMs, deliveryCount);

        if (deliveryCount >= MAX_DELIVERY_COUNT) {
            moveToDeadLetter(streamKey, messageId);
            return;
        }

        try {
            List<MapRecord<String, Object, Object>> claimed =
                    redisTemplate.opsForStream().claim(
                            streamKey,
                            StreamConfig.EMAIL_GROUP,
                            "recovery-worker",
                            // Thời gian tối thiểu message phải pending
                            // trước khi có thể XCLAIM
                            Duration.ofMillis(PENDING_TIMEOUT_MS),
                            RecordId.of(messageId)
                    );

            if (!claimed.isEmpty()) {
                log.info("[Recovery] Claimed message | msgId={}", messageId);
                MapRecord<String, Object, Object> record = claimed.get(0);
                String jobIdStr = (String) record.getValue().get("jobId");

                if (jobIdStr != null) {
                    MapRecord<String, String, String> typedRecord =
                            MapRecord.create(
                                    streamKey,
                                    record.getValue().entrySet().stream()
                                            .collect(java.util.stream.Collectors.toMap(
                                                    e -> e.getKey().toString(),
                                                    e -> e.getValue().toString()
                                            ))
                            ).withId(record.getId());

                    emailStreamListener.onMessage(typedRecord);
                }
            }

        } catch (Exception e) {
            log.error("[Recovery] Claim failed | msgId={} | error={}",
                    messageId, e.getMessage());
        }
    }

    // Đưa message đã retry quá nhiều vào Dead Letter Stream
    private void moveToDeadLetter(String originalStream, String messageId) {
        try {
            // Lấy message gốc từ stream
            List<MapRecord<String, Object, Object>> messages =
                    redisTemplate.opsForStream().range(
                            originalStream,
                            Range.closed(messageId, messageId)
                    );

            if (!messages.isEmpty()) {
                MapRecord<String, Object, Object> original = messages.get(0);

                // Thêm vào Dead Letter Stream với metadata
                redisTemplate.opsForStream().add(
                        MapRecord.create(
                                "stream:dead-letter",
                                java.util.Map.of(
                                        "originalStream", originalStream,
                                        "originalMessageId", messageId,
                                        "jobId", original.getValue()
                                                .getOrDefault("jobId", "unknown").toString(),
                                        "reason", "Max delivery count exceeded"
                                )
                        )
                );
            }

            // ACK để xóa khỏi PEL của stream gốc
            redisTemplate.opsForStream().acknowledge(
                    originalStream,
                    StreamConfig.EMAIL_GROUP,
                    messageId
            );

            log.error("[Recovery] Moved to DLS | msgId={} | stream={}",
                    messageId, originalStream);

        } catch (Exception e) {
            log.error("[Recovery] Failed to move DLS | msgId={} | error={}",
                    messageId, e.getMessage());
        }
    }
}