package com.trongtin.asyncprocessingsys.service;


import com.trongtin.asyncprocessingsys.dto.request.EmailPayload;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromAddress;

    public void send(EmailPayload payload) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(payload.getTo());
        helper.setSubject(payload.getSubject());

        String htmlBody = buildHtmlTemplate(payload);
        helper.setText(htmlBody, true);

        mailSender.send(message);
        log.info("[EmailService] Sent | to={} | subject={}",
                payload.getTo(), payload.getSubject());
    }

    private String buildHtmlTemplate(EmailPayload payload) {
        String greeting = payload.getRecipientName() != null
                ? "Xin chào <strong>" + payload.getRecipientName() + "</strong>,"
                : "Xin chào,";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background:#f4f4f4; 
                         font-family: Arial, sans-serif;">

                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center" style="padding: 40px 0;">

                            <table width="600" cellpadding="0" cellspacing="0"
                                   style="background:#ffffff; border-radius:8px;
                                          box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <tr>
                                    <td style="background:#2c3e50; padding:24px 32px;
                                               border-radius:8px 8px 0 0;">
                                        <h2 style="margin:0; color:#ffffff; font-size:20px;">
                                            Async Job System
                                        </h2>
                                    </td>
                                </tr>

                                <!-- Body -->
                                <tr>
                                    <td style="padding: 32px;">
                                        <p style="margin:0 0 16px; color:#333; font-size:15px;">
                                            %s
                                        </p>
                                        <div style="color:#555; font-size:14px; 
                                                    line-height:1.7; margin-bottom:24px;">
                                            %s
                                        </div>
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="padding:16px 32px; background:#f8f9fa;
                                               border-radius:0 0 8px 8px;
                                               border-top:1px solid #eee;">
                                        <p style="margin:0; color:#999; font-size:12px;">
                                            Email này được gửi tự động. Vui lòng không reply.
                                        </p>
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>

            </body>
            </html>
            """.formatted(greeting, payload.getBody());
    }
}