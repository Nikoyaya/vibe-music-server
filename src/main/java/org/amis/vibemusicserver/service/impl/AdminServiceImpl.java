package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.enumeration.RoleEnum;
import org.amis.vibemusicserver.mapper.AdminMapper;
import org.amis.vibemusicserver.model.dto.AdminDTO;
import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.entity.Admin;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IAdminService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.RsaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : KwokChichung
 * @description : Admin服务实现类
 * @createDate : 2026/1/3 17:44
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RsaUtil rsaUtil;

    /**
     * 管理员注册
     *
     * @param adminDTO 管理员信息
     * @return 结果
     */
    @Override
    public Result register(AdminDTO adminDTO) {
        log.info("开始注册管理员: {}", adminDTO.getUsername());

        // 解密前端传来的加密密码
        String decryptedPassword;
        try {
            decryptedPassword = rsaUtil.decrypt(adminDTO.getPassword());
        } catch (Exception e) {
            log.error("密码解密失败", e);
            return Result.error("密码处理失败");
        }

        // 根据用户名查询管理员，检查是否已存在
        Admin admin = adminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .eq("username", adminDTO.getUsername()));

        // 如果用户名已存在，返回错误信息
        if (admin != null) {
            log.warn("用户名已存在: {}", adminDTO.getUsername());
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        // 对密码进行MD5加密处理
        String passwordMD5 = DigestUtils.md5DigestAsHex(decryptedPassword.getBytes());
        // 创建新的管理员对象并设置用户名和加密后的密码
        Admin adminRegister = new Admin();
        adminRegister.setUsername(adminDTO.getUsername()).setPassword(passwordMD5);

        // 尝试插入新的管理员信息，如果插入失败，返回错误信息
        if (adminMapper.insert(adminRegister) == 0) {
            log.error("Admin registration failed for username: {}", adminDTO.getUsername());
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }

        log.info("管理员注册成功，用户名: {}", adminDTO.getUsername());
        // 注册成功，返回成功信息
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }


    /**
     * 管理员登录
     *
     * @param adminDTO 管理员信息
     * @return 结果
     */
    @Override
    public Result login(AdminDTO adminDTO) {
        // 解密前端传来的加密密码
        String decryptedPassword;
        try {
            decryptedPassword = rsaUtil.decrypt(adminDTO.getPassword());
        } catch (Exception e) {
            log.error("密码解密失败", e);
            return Result.error("密码处理失败");
        }

        // 根据用户名查询管理员信息
        Admin admin = adminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .eq("username", adminDTO.getUsername()));

        // 如果管理员不存在，返回错误信息
        if (admin == null) {
            log.warn("管理员不存在，username: {}", adminDTO.getUsername());
            return Result.error(MessageConstant.LOGIN + MessageConstant.ERROR);
        }

        // 验证密码是否正确
        if (DigestUtils.md5DigestAsHex(decryptedPassword.getBytes()).equals(admin.getPassword())) {
            // 单点登录控制：清理旧会话的所有token
            String adminTokenKey = "admin_token:" + admin.getAdminId();
            String oldRefreshToken = stringRedisTemplate.opsForValue().get(adminTokenKey);
            if (oldRefreshToken != null) {
                try {
                    // 1. 解析旧refresh_token获取关联的access_token
                    Map<String, Object> oldClaims = JwtUtil.parseToken(oldRefreshToken);
                    String oldAccessToken = (String) oldClaims.get("linked_access_token");

                    // 2. 双重保险：从Redis获取可能关联的access_token
                    String redisAccessToken = stringRedisTemplate.opsForValue().get("access_token:" + oldRefreshToken);

                    // 3. 清理所有可能存在的旧token
                    if (oldAccessToken != null) {
                        stringRedisTemplate.delete(oldAccessToken);
                        log.info("已删除claims中的旧access_token: {}", oldAccessToken);
                    }
                    if (redisAccessToken != null && !redisAccessToken.equals(oldAccessToken)) {
                        stringRedisTemplate.delete(redisAccessToken);
                        log.info("已删除Redis中关联的旧access_token: {}", redisAccessToken);
                    }

                    // 4. 删除旧的refresh_token
                    stringRedisTemplate.delete(oldRefreshToken);
                    log.info("已删除旧refresh_token: {}", oldRefreshToken);

                    // 5. 清理SSO控制记录和关联键
                    stringRedisTemplate.delete(adminTokenKey);
                    stringRedisTemplate.delete("access_token:" + oldRefreshToken);

                    log.info("已完全清理旧会话Token，admin ID: {}", admin.getAdminId());
                } catch (Exception e) {
                    log.error("清理旧Token失败，执行强制清理，admin ID: {}", admin.getAdminId(), e);
                    // 强制清理所有可能的残留token
                    stringRedisTemplate.delete(adminTokenKey);
                    stringRedisTemplate.delete(oldRefreshToken);
                }
            }

            // 创建JWT的claims（声明）
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ADMIN_ID, admin.getAdminId());
            claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());

            // 生成双Token
            String accessToken = JwtUtil.generateAccessToken(claims);
            String refreshToken = JwtUtil.generateRefreshToken(claims);
            log.info("双Token生成完成，admin ID: {}", admin.getAdminId());

            // 存储双Token到Redis并建立双重关联
            stringRedisTemplate.opsForValue().set(accessToken, accessToken, 6, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(refreshToken, refreshToken, 15, TimeUnit.DAYS);

            // 建立双向关联：1.在claims中记录
            claims.put("linked_access_token", accessToken);
            // 2.在Redis中建立反向映射
            stringRedisTemplate.opsForValue().set(
                "access_token:" + refreshToken,
                accessToken,
                15, TimeUnit.DAYS
            );

            // 使用refresh_token控制单点登录
            stringRedisTemplate.opsForValue().set(adminTokenKey, refreshToken, 15, TimeUnit.DAYS);

            log.info("双Token存储到Redis完成，使用refresh_token控制单点登录");

            // 返回成功结果和双Token
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS,
                new TokenDTO(accessToken, refreshToken));
        }

        // 密码验证失败，返回错误信息
        log.warn("Password verification failed for admin: {}", adminDTO.getUsername());
        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }


    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    public Result logout(String token) {
        log.info("token: {}", token);
        // 注销token及SSO控制记录
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Integer adminId = (Integer) claims.get(JwtClaimsConstant.ADMIN_ID);
        stringRedisTemplate.delete("admin_token:" + adminId);
        Boolean result = stringRedisTemplate.delete(token);
        if (result) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }


}
