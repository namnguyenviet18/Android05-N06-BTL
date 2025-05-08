package com.group06.music_app.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String receiver,
            String username,
            EmailTemplateName emailTemplateName,
            String otp,
            String subject
    ) throws MessagingException {
        String templateName = "";
        if(emailTemplateName != null) {
            templateName = emailTemplateName.getName();
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("activation_code", otp);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("musicapp@gmail.com");
        helper.setTo(receiver);
        helper.setSubject(subject);

        if(!templateName.isBlank()) {
            String template = templateEngine.process(templateName, context);
            helper.setText(template, true);
        }
        mailSender.send(mimeMessage);
    }
}
