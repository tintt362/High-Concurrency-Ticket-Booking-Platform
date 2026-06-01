package com.trongtin.asyncprocessingsys.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.asyncprocessingsys.ai.EmailComposerAI;
import com.trongtin.asyncprocessingsys.dto.request.EmailPayload;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class
)
public class EmailWorkerTest {

    @InjectMocks
    EmailWorker emailWorker;

    @Mock
    EmailService emailService;

    @Mock
    JobRepository jobRepository;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    EmailComposerAI emailComposerAI;

    @Mock
    WebhookService webhookService;

    @Mock
    ObjectMapper objectMapper;

    private Job job;
    private UUID jobId;


    @BeforeEach
    void setUp() {
        jobId = UUID.randomUUID();

        job = Job.builder()
                .id(jobId)
                .payload("{\"to\":\"trongtin@example.com\"}")
                .status(JobStatus.PENDING)
                .retryCount(0)
                .build();
    }
    @Test
    void shouldProcessJobSuccessfully() throws Exception {

        // given
        EmailPayload payload = new EmailPayload();
        payload.setTo("trongtin@example.com");
        payload.setSubject("Interview");
        payload.setBody("Hello");

        when(jobRepository.findById(jobId))
                .thenReturn(Optional.of(job));

        when(objectMapper.readValue(
                job.getPayload(),
                EmailPayload.class
        )).thenReturn(payload);

        // when
        emailWorker.processJob(jobId.toString());

        // then

        verify(jobRepository, atLeast(2)).save(any(Job.class));

        verify(emailService, times(1))
                .send(payload);

        verify(webhookService, times(1))
                .deliver(job);

        assertEquals(JobStatus.DONE, job.getStatus());
    }
}
