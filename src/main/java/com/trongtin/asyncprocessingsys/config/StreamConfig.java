package com.trongtin.asyncprocessingsys.config;

import com.trongtin.asyncprocessingsys.worker.EmailStreamListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class  StreamConfig {

    private final RedisTemplate<String, String> redisTemplate;
    private final StreamMessageListenerContainer<String,
                MapRecord<String, String, String>> container;
    private final EmailStreamListener emailStreamListener;

    // Tên các stream — quy ước: stream:{type}:{priority}
    public static final String EMAIL_STREAM_HIGH   = "stream:email:high";
    public static final String EMAIL_STREAM_MEDIUM = "stream:email:medium";
    public static final String EMAIL_STREAM_LOW    = "stream:email:low";

    // Tên consumer group — tất cả worker email vào cùng group này
    public static final String EMAIL_GROUP = "email-workers";

    @Value("${spring.application.name:async-job-system}")
    private String appName;

    // @PostConstruct: chạy sau khi Spring khởi tạo bean xong
    // Đảm bảo stream và group đã tồn tại trước khi worker lắng nghe
    @PostConstruct
    public void initStreamsAndSubscribe() {
        // Khởi tạo 3 stream email theo priority
        initStream(EMAIL_STREAM_HIGH,   EMAIL_GROUP);
        initStream(EMAIL_STREAM_MEDIUM, EMAIL_GROUP);
        initStream(EMAIL_STREAM_LOW,    EMAIL_GROUP);

        // Đăng ký listener cho từng stream
        // Mỗi stream có consumer riêng để track PEL độc lập
        subscribeToStream(EMAIL_STREAM_HIGH,   "worker-high-1");
        subscribeToStream(EMAIL_STREAM_MEDIUM, "worker-medium-1");
        subscribeToStream(EMAIL_STREAM_LOW,    "worker-low-1");

        log.info("[StreamConfig] All streams initialized and subscribed");
    }

    // Tạo stream và consumer group nếu chưa có
    // Nếu đã tồn tại thì không làm gì (idempotent)
    private void initStream(String streamKey, String groupName) {
        try {
            // Tạo consumer group với ReadOffset.latest()
            // latest() = chỉ đọc message MỚI từ bây giờ trở đi
            // Dùng ReadOffset.from("0") nếu muốn đọc lại từ đầu
            redisTemplate.opsForStream()
                    .createGroup(streamKey, ReadOffset.latest(), groupName);
            log.info("[StreamConfig] Created group '{}' on stream '{}'",
                    groupName, streamKey);

        } catch (Exception e) {
            // Group đã tồn tại → bỏ qua lỗi này
            // BUSYGROUP error là bình thường khi restart app
            if (e.getMessage() != null &&
                    e.getMessage().contains("BUSYGROUP")) {
                log.debug("[StreamConfig] Group '{}' already exists on '{}'",
                        groupName, streamKey);
            } else {
                // Lỗi khác → log để debug
                log.error("[StreamConfig] Error creating group '{}' on '{}': {}",
                        groupName, streamKey, e.getMessage());
            }
        }
    }

    // Đăng ký listener để tự động nhận message từ stream
    private void subscribeToStream(String streamKey, String consumerName) {
        Subscription subscription = container.receive(
                // Consumer: group + tên worker cụ thể
                Consumer.from(EMAIL_GROUP, consumerName),

                // StreamOffset: đọc từ đâu?
                // ReadOffset.lastConsumed() = đọc message chưa được ACK
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),

                // Listener: class xử lý message
                emailStreamListener
        );

        log.info("[StreamConfig] Subscribed '{}' to stream '{}'",
                consumerName, streamKey);
    }
}

