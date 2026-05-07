package com.trongtin.asyncprocessingsys.service;

import com.trongtin.asyncprocessingsys.dto.request.EmailPayload;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@TestPropertySource("/test.properties")
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private EmailPayload payload;

    @BeforeEach
    void initData() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "test@system.com");

        payload = new EmailPayload();
        payload.setTo("candidate@example.com");
        payload.setSubject("Interview Invitation");
        payload.setBody("Hello Tin...");
        payload.setRecipientName("Tin");
    }

    @Test
    void shouldTestSendEmail_Successfully() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.send(payload);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

