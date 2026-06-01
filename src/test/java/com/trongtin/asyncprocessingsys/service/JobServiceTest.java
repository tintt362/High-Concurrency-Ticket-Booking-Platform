package com.trongtin.asyncprocessingsys.service;


import com.trongtin.asyncprocessingsys.ai.JobClassifierAI;
import com.trongtin.asyncprocessingsys.dto.request.CreateJobRequest;
import com.trongtin.asyncprocessingsys.dto.response.JobResponse;
import com.trongtin.asyncprocessingsys.model.enums.JobPriority;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/test.properties")
public class JobServiceTest {

    @Mock
    JobRepository jobRepository;
    @InjectMocks
    JobService jobService;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    JobClassifierAI jobClassifierAI;

    private CreateJobRequest request;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu cho mỗi test case
        request = new CreateJobRequest();
        request.setType(JobType.EMAIL);
        request.setPayload("{\"to\": \"test@gmail.com\"}");
        request.setCallbackUrl("https://webhook.site/test");
    }

    @Test
    void shouldCreateEmailJobSuccessfully() {
        //given
        UUID mockId = UUID.randomUUID();
        Job savedJob = Job.builder()
                .id(mockId)
                .type(request.getType())
                .status(JobStatus.PENDING)
                .priority(JobPriority.HIGH)
                .build();

        //when
        when(jobClassifierAI.classify(any(), any())).thenReturn(JobPriority.HIGH);
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.add(any())).thenReturn(RecordId.of("12345-0"));

        JobResponse jobResponse = jobService.createJob(request);
        assertThat(savedJob.getType()).isEqualTo(jobResponse.getType());
        assertThat(savedJob.getStatus()).isEqualTo(jobResponse.getStatus());

//        verify(jobClassifierAI, times(1))
//                .classify("Send interview email", JobType.EMAIL);

        verify(jobRepository, times(1))
                .save(any(Job.class));

        verify(streamOperations, times(1))
                .add(any(MapRecord.class));
    }

    @Test
    void shouldTestGetStatusJob() {
        UUID mockId = UUID.randomUUID();
        Job savedJob = Job.builder()
                .id(mockId)
                .type(request.getType())
                .status(JobStatus.PENDING)
                .priority(JobPriority.HIGH)
                .build();
        when(jobRepository.findById(mockId)).thenReturn(Optional.ofNullable(savedJob));
        JobResponse jobResponse = jobService.getJobStatus(mockId);

        assertThat(savedJob.getStatus()).isEqualTo(jobResponse.getStatus());    }
}
