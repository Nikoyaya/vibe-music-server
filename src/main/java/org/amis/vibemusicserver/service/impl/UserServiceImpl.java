package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.enumeration.UserStatusEnum;
import org.amis.vibemusicserver.mapper.UserMapper;
import org.amis.vibemusicserver.model.dto.UserRegisterDTO;
import org.amis.vibemusicserver.model.entity.User;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author : KwokChichung
 * @description : 用户服务实现类
 * @createDate : 2026/1/5 1:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserMapper userMapper;


    /**
     * 发送验证码
     *
     * @param email 用户邮箱
     * @return 结果
     */
    @Override
    public Result sendVerificationCode(String email) {
        // 调用邮件服务发送验证码邮件
        String verificationCodeEmail = emailService.sendVerificationCodeEmail(email);
        // 如果发送失败，返回错误结果
        if (verificationCodeEmail == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }

        // 将验证码存储到Redis中，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set("verificationCode:" + email, verificationCodeEmail, 5, TimeUnit.MINUTES);
        // 返回发送成功结果
        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    /**
     * 验证验证码
     *
     * @param email            用户邮箱
     * @param verificationCode 验证码
     * @return 验证结果
     */
    @Override
    public Boolean verifyVerificationCode(String email, String verificationCode) {
        // 从Redis中获取存储的验证码
        String storedVerificationCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        // 验证码存在且与输入的验证码匹配时返回true，否则返回false
        return storedVerificationCode != null && storedVerificationCode.equals(verificationCode);
    }


    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result register(UserRegisterDTO userRegisterDTO) {
        // 删除Redis中的验证码
        stringRedisTemplate.delete("verificationCode:" + userRegisterDTO.getEmail());

        // 检查用户名是否已存在
        User username = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", userRegisterDTO.getUsername()));
        if (username != null) {
            log.warn("用户名已存在用户注册失败: {}", userRegisterDTO.getUsername());
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        User email = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("email", userRegisterDTO.getEmail()));
        if (email != null) {
            log.warn("邮箱已存在用户注册失败: {}", userRegisterDTO.getEmail());
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        // 对密码进行MD5加密
        String passwordMD5 = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        User user = new User();
        // 设置用户信息
        user.setUsername(userRegisterDTO.getUsername())
                .setEmail(userRegisterDTO.getEmail())
                .setPassword(passwordMD5)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.Enable);

        // 插入用户数据到数据库
        if (userMapper.insert(user) == 0) {
            log.warn("用户注册验证失败: {}", userRegisterDTO.getUsername());
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        log.info("用户注册成功: {}", userRegisterDTO.getUsername());
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }
}

