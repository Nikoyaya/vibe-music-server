package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.enumeration.RoleEnum;
import org.amis.vibemusicserver.mapper.AdminMapper;
import org.amis.vibemusicserver.model.dto.AdminDTO;
import org.amis.vibemusicserver.model.entity.Admin;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IAdminService;
import org.amis.vibemusicserver.utils.JwtUtil;
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

    /**
     * 管理员注册
     *
     * @param adminDTO 管理员信息
     * @return 结果
     */
    @Override
    public Result register(AdminDTO adminDTO) {
        log.info("开始注册管理员: {}", adminDTO.getUsername());
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
        String passwordMD5 = DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes());
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
        if (DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            // 创建JWT的claims（声明）
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ADMIN_ID, admin.getAdminId());
            claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());

            // 生成JWT token
            String token = JwtUtil.generateToken(claims);
            log.info("token生成，admin ID: {}", admin.getAdminId());

            // 将token存入Redis，设置6小时过期
            // 注意：这里的key使用了用户名和adminId的组合，确保唯一性(这个key可以自己修改，只是我调试方便这样设计而已，也可以直接用token为key也行)
            stringRedisTemplate.opsForValue().set(admin.getUsername() + "(" + admin.getAdminId() + ")", token, 6, TimeUnit.HOURS);
            log.info("Token stored in Redis with 6 hours expiration");

            // 返回成功结果和token
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
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
        // 从Redis中删除token，返回删除操作的结果（true表示删除成功，false表示删除失败）
        Boolean deleteResult = stringRedisTemplate.delete(token);

        // 判断删除结果
        if (deleteResult) {
            // 如果删除成功，记录成功日志并返回成功结果
            log.info("token从Redis删除成功，登出成功");
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            // 如果删除失败，记录失败日志并返回失败结果
            log.warn("token从Redis删除失败，登出失败");
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

}

