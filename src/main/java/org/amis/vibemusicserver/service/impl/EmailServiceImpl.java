package org.amis.vibemusicserver.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.service.IEmailService;
import org.amis.vibemusicserver.utils.RandomCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/5 23:03
 */
@Slf4j
@Service
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.username}")
    private String from;


    /**
     * 发送邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果，包含是否成功
     */
    @Override
    public boolean sendEmail(String to, String subject, String content) {
        // 验证邮箱格式
        if (to == null || !to.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            log.error("无效的邮箱地址: {}", to);
            return false;
        }

        // 创建MIME消息对象
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            // 创建MIME消息辅助对象，支持HTML内容
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            // 设置发件人地址
            helper.setFrom(from);
            // 设置收件人地址
            helper.setTo(to);
            // 设置邮件主题
            helper.setSubject(subject);
            // 设置邮件内容，启用HTML格式
            helper.setText(content, true);
            // 发送邮件
            mailSender.send(mimeMessage);
            // 发送成功返回true
            log.info(MessageConstant.EMAIL_SEND_SUCCESS);
            return true;
        } catch (MessagingException e) {
            // 记录邮件发送失败日志
            log.error(MessageConstant.EMAIL_SEND_FAILED, e);
            // 发送失败返回false
            return false;
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param email 收件人地址
     * @return 发送结果，包含是否成功和验证码
     */
    @Override
    public String sendVerificationCodeEmail(String email) {
        if (email == null || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            log.error("无效的邮箱地址: {}", email);
            return null;
        }

        String verificationCode = RandomCodeUtil.generateRandomCode();
        String subject = "【Vibe Music】验证码";
        String content = "您的验证码为：" + verificationCode;
        boolean success = sendEmail(email, subject, content);
        if (success) {
            log.info("验证码邮件发送成功，验证码为：{}", verificationCode);
            return verificationCode;
        } else {
            log.error("验证码邮件发送失败");
            return null;
        }
    }
}

