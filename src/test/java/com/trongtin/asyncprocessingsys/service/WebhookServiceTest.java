package com.trongtin.asyncprocessingsys.service;

import com.trongtin.asyncprocessingsys.dto.response.WebhookPayload;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.model.enums.WebhookStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/test.properties")
public class WebhookServiceTest {

    @Mock
    JobService jobService;

    @Mock
    JobRepository jobRepository;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    WebhookService webhookService;

    Job job;

    @BeforeEach
    void setUp() {
        UUID mockId = java.util.UUID.randomUUID();
        job = Job.builder()
                .id(mockId)
                .type(JobType.EMAIL)
                .status(JobStatus.DONE)
                .callbackUrl("http://localhost:8080/webhook")
                .build();
    }

    //case 1 : not callbackurl
    @Test
    void shouldTestDeliver__notCallBackUrl_failed() {
        // given
        job.setCallbackUrl("");

        // when
        webhookService.deliver(job);

        // then
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(jobRepository);
    }

    //case 2: http 200
    @Test
    void shouldTestDeliver_success() {
        // given
        ResponseEntity<String> response =
                ResponseEntity.ok("Success");

        when(restTemplate.postForEntity(
                eq(job.getCallbackUrl()),
                any(),
                eq(String.class)
        )).thenReturn(response);

        // when
        webhookService.deliver(job);

        // then
        assertEquals(WebhookStatus.DELIVERED, job.getWebhookStatus());

        verify(jobRepository, atLeastOnce()).save(job);
    }

    //case 3: gui fail het retry
    @Test
    void shouldTestDeliver_fail_retry() {
        // given
        when(restTemplate.postForEntity(
                eq(job.getCallbackUrl()),
                any(WebhookPayload.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection failed"));

        // when
        webhookService.deliver(job);

        // then
        verify(jobRepository, atLeastOnce()).save(job);

        assertTrue(job.getRetryCount() >= 1);
    }
}
