package com.collabflow.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String from;

    @Async
    public void sendInvitation(String to, String workspaceName, String inviteLink) {
        String subject = String.format("[CollabFlow] '%s' 워크스페이스 초대", workspaceName);
        String body = String.format("""
                CollabFlow 워크스페이스 '%s'에 초대되었습니다.

                아래 링크를 클릭해 초대를 수락하세요 (72시간 내 유효):
                %s

                이 메일을 요청하지 않으셨다면 무시하셔도 됩니다.
                """, workspaceName, inviteLink);

        sendMail(to, subject, body);
    }

    private void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Mail sent to {}", to);
        } catch (MailException e) {
            // 메일 서버 미설정 시 로그로 폴백 (dev 환경 대응)
            log.warn("Failed to send mail to {} — {}", to, e.getMessage());
            log.debug("Mail body:\nTo: {}\nSubject: {}\n{}", to, subject, body);
        }
    }
}
