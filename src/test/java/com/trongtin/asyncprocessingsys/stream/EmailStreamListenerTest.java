package com.trongtin.asyncprocessingsys.stream;

import com.trongtin.asyncprocessingsys.repository.JobRepository;
import com.trongtin.asyncprocessingsys.service.WebhookService;
import com.trongtin.asyncprocessingsys.worker.EmailStreamListener;
import com.trongtin.asyncprocessingsys.worker.EmailWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailStreamListenerTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    JobRepository jobRepository;

    @Mock
    EmailWorker emailWorker;

    @Mock
    WebhookService webhookService;

    @Mock
    StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    EmailStreamListener listener;

    @Test
    void shouldProcessAndAckMessageSuccessfully() {

        // given
        MapRecord<String, String, String> record =
                MapRecord.create(
                        "stream:email:high",
                        Map.of("jobId", "123e4567-e89b-12d3-a456-426614174000")
                );

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        // when
        listener.onMessage(record);

        // then
        verify(emailWorker).processJob("123e4567-e89b-12d3-a456-426614174000");

        verify(streamOperations).acknowledge(
                eq("stream:email:high"),
                eq("email-workers"),
                anyString()
        );
    }
}