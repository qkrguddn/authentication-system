package com.sg.authentication.service;

import com.sg.authentication.entity.User;
import com.sg.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;

    private String authNum; //랜덤 인증 코드

    //랜덤 인증 코드 생성
    public void createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();
        for(int i=0;i<8;i++) {
            int index = random.nextInt(3);
            switch (index) {
                case 0 :
                    key.append((char) ((int)random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) ((int)random.nextInt(26) + 65));
                    break;
                case 2:
                    key.append(random.nextInt(9));
                    break;
            }
        }
        authNum = key.toString();
    }
    //메일 양식 작성

    public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findByEmail(email);

        createCode();
        String encodingPw = passwordEncoder.encode(authNum);
        user.updatePw(encodingPw);
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email); //보낼 이메일 설정
        message.setSubject("인증번호");
        message.setFrom("qkrdn2023@naver.com");
        message.setText((authNum), "utf-8", "html");

        return message;
    }

    //실제 메일 전송
    @CacheEvict(value = "alluser", allEntries = true)
    public String sendEmail(String toEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage emailForm = createEmailForm(toEmail);
        emailSender.send(emailForm);
        return authNum; //인증 코드 반환
    }
}
