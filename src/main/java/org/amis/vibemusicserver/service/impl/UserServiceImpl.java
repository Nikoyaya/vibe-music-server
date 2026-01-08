package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.enumeration.RoleEnum;
import org.amis.vibemusicserver.enumeration.UserStatusEnum;
import org.amis.vibemusicserver.mapper.UserMapper;
import org.amis.vibemusicserver.model.dto.*;
import org.amis.vibemusicserver.model.entity.User;
import org.amis.vibemusicserver.model.vo.UserManagementVO;
import org.amis.vibemusicserver.model.vo.UserVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IUserService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.ThreadLocalUtil;
import org.amis.vibemusicserver.utils.TypeConversionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : KwokChichung
 * @description : 用户服务实现类
 * @createDate : 2026/1/5 1:08
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MinioServiceImpl minioService;


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

        // 创建用户对象并设置相关属性
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername())
                .setPassword(passwordMD5)
                .setEmail(userRegisterDTO.getEmail())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.ENABLE);

        // 插入用户数据到数据库
        if (userMapper.insert(user) == 0) {
            log.warn("用户注册验证失败: {}", userRegisterDTO.getUsername());
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        log.info("用户注册成功: {}", userRegisterDTO.getUsername());
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 结果
     */
    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        // 根据邮箱查询用户
        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("email", userLoginDTO.getEmail()));

        // 检查用户是否存在
        if (user == null) {
            log.warn("用户不存在: {}", userLoginDTO.getEmail());
            return Result.error(MessageConstant.LOGIN + MessageConstant.ERROR);
        }

        // 检查用户状态是否被禁用
        if (user.getUserStatus() != UserStatusEnum.ENABLE) {
            log.warn("用户: {}，已被禁用", userLoginDTO.getEmail());
            return Result.error(MessageConstant.LOGIN + MessageConstant.ERROR + "," + MessageConstant.ACCOUNT_LOCKED);
        }

        // 验证密码（MD5加密比较）
        boolean passwordEquals = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword());
        if (passwordEquals) {
            log.info("用户登录成功: {}", userLoginDTO.getEmail());
            // 单点登录控制：先查找并删除旧的token
            String userTokenKey = "user_token:" + user.getId();
            String oldToken = stringRedisTemplate.opsForValue().get(userTokenKey);
            if (oldToken != null) {
                stringRedisTemplate.delete(oldToken); // 删除旧的token记录
                log.info("删除旧的token，实现单点登录控制，user ID: {}", user.getId());
            }

            // 创建JWT的claims（声明）
            HashMap<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());

            // 生成JWT token
            String token = JwtUtil.generateToken(claims);
            log.info("token生成，user ID: {}", user.getId());

            // 将token存入Redis，设置6小时过期
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);
            // 存储用户ID与token的映射关系，用于单点登录控制
            stringRedisTemplate.opsForValue().set(userTokenKey, token, 6, TimeUnit.HOURS);
            log.info("Token stored in Redis with 6 hours expiration");
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }
        // 密码错误返回错误信息
        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 用户信息
     *
     * @return 结果
     */
    @Override
    public Result<UserVO> userInfo() {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> map = ThreadLocalUtil.get();
        // 从JWT claims中提取用户ID
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        // 将用户ID转换为Long类型
        Long userId = TypeConversionUtil.toLong(userIdObj);
        // 根据用户ID查询用户信息
        User user = userMapper.selectById(userId);
        // 创建用户VO对象
        UserVO userVO = new UserVO();
        // 将用户实体属性复制到VO对象中
        BeanUtils.copyProperties(user, userVO);

        // 返回成功响应，包含用户信息
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息DTO对象
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserInfo(UserDTO userDTO) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> map = ThreadLocalUtil.get();
        // 从JWT claims中提取用户ID
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        // 将用户ID转换为Long类型
        Long userId = TypeConversionUtil.toLong(userIdObj);

        // 检查用户名是否已被其他用户使用
        User userByUsername = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", userDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getId().equals(userId)) {
            log.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        // 检查手机号是否已被其他用户使用
        User userByPhone = userMapper.selectOne(new QueryWrapper<User>()
                .eq("phone", userDTO.getPhone()));
        if (userByPhone != null && !userByPhone.getId().equals(userId)) {
            log.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
        }

        // 检查邮箱是否已被其他用户使用
        User userByEmail = userMapper.selectOne(new QueryWrapper<User>()
                .eq("email", userDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getId().equals(userId)) {
            log.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        // 创建用户实体对象并复制DTO属性
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 更新更新时间
        user.setUpdateTime(LocalDateTime.now());

        // 执行更新操作，检查是否成功
        if (userMapper.updateById(user) == 0) {
            log.error(MessageConstant.UPDATE + MessageConstant.FAILED);
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        // 记录成功日志并返回成功结果
        log.info(MessageConstant.UPDATE + MessageConstant.SUCCESS);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户头像
     *
     * @param avatarUrl 用户头像URL地址
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserAvatar(String avatarUrl) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> map = ThreadLocalUtil.get();
        // 从JWT claims中提取用户ID
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        // 将用户ID转换为Long类型
        Long userId = TypeConversionUtil.toLong(userIdObj);

        // 根据用户ID查询用户信息
        User user = userMapper.selectById(userId);
        // 获取用户当前头像URL
        String userAvatar = user.getUserAvatar();
        // 如果用户已有头像，则删除旧头像文件
        if (userAvatar != null && !userAvatar.isEmpty()) {
            minioService.deleteFile(userAvatar);
        }

        // 更新用户头像和更新时间
        int id = userMapper.update(new User().setUserAvatar(avatarUrl).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId));
        // 检查更新是否成功
        if (id == 0) {
            log.error(MessageConstant.UPDATE + MessageConstant.FAILED);
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        // 记录成功日志并返回成功结果
        log.info(MessageConstant.UPDATE + MessageConstant.SUCCESS);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户密码信息
     * @param token           JWT token
     * @return 结果
     */
    @Override
    public Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token) {
        // 解析token获取用户信息
        Map<String, Object> claims = JwtUtil.parseToken(token);
        Integer userIdInt = (Integer) claims.get(JwtClaimsConstant.USER_ID);
        Long userId = userIdInt.longValue();
        User user = userMapper.selectById(userId);

        // 验证旧密码是否正确
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getOldPassword().getBytes()))) {
            log.error(MessageConstant.OLD_PASSWORD_ERROR);
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 验证新密码不能与旧密码相同
        if (user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes()))) {
            log.error(MessageConstant.NEW_PASSWORD_ERROR);
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }

        // 验证确认密码与新密码是否一致
        if (!userPasswordDTO.getRepeatPassword().equals(userPasswordDTO.getNewPassword())) {
            log.error(MessageConstant.PASSWORD_NOT_MATCH);
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        // 更新用户密码
        if (userMapper.update(new User()
                        .setPassword(DigestUtils
                                .md5DigestAsHex(userPasswordDTO.getNewPassword()
                                        .getBytes()))
                        .setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId)) == 0) {
            log.error(MessageConstant.UPDATE + MessageConstant.FAILED);
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        // 注销当前token，强制用户重新登录
        stringRedisTemplate.delete(token);

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 重置用户密码
     *
     * @param userResetPasswordDTO 用户密码信息
     * @return 结果
     */
    @Override
    public Result resetUserPassword(UserResetPasswordDTO userResetPasswordDTO) {
        // 删除Redis中的验证码，避免重复使用
        stringRedisTemplate.delete("verificationCode:" + userResetPasswordDTO.getEmail());

        // 根据邮箱查询用户
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("email", userResetPasswordDTO.getEmail()));

        // 检查用户是否存在
        if (user == null) {
            log.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
            return Result.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
        }

        // 验证确认密码与新密码是否一致
        if (!userResetPasswordDTO.getRepeatPassword().equals(userResetPasswordDTO.getNewPassword())) {
            log.error(MessageConstant.PASSWORD_NOT_MATCH);
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        // 更新用户密码，使用MD5加密
        if (userMapper.update(new User()
                        .setPassword(DigestUtils
                                .md5DigestAsHex(userResetPasswordDTO
                                        .getNewPassword().getBytes()))
                        .setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", user.getId())) == 0) {
            log.error(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.FAILED);
            return Result.error(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.FAILED);
        }

        // 记录密码重置成功日志
        log.info(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.SUCCESS);
        return Result.success(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.SUCCESS);
    }

    /**
     * 用户登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result logout(String token) {
        log.info("token: {}", token);

        try {

            // 从Redis中删除token
            Boolean deleteResult = stringRedisTemplate.delete(token);
            // 如果Redis删除成功，则记录成功日志并返回成功结果
            if (deleteResult) {
                log.info("用户登出成功: {}", token);
                return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
            } else {
                // 如果Redis删除失败，则记录警告日志并返回失败结果
                log.warn("用户登出失败: {}", token);
                return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
            }
        } catch (Exception e) {
            log.error("解析token失败，登出失败", e);
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

    /**
     * 注销账户
     *
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteAccount() {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> map = ThreadLocalUtil.get();
        // 从JWT claims中提取用户ID
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        // 将用户ID转换为Long类型
        Long userId = TypeConversionUtil.toLong(userIdObj);

        // 根据用户ID查询用户信息
        User user = userMapper.selectById(userId);
        // 检查用户是否存在
        if (user == null) {
            log.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }

        // 获取用户头像URL
        String userAvatar = user.getUserAvatar();
        // 如果用户头像存在，则删除头像文件
        if (userAvatar != null && !userAvatar.isEmpty()) {
            // 删除用户头像
            minioService.deleteFile(userAvatar);
        }

        // 执行用户删除操作
        if (userMapper.deleteById(userId) == 0) {
            log.error(MessageConstant.DELETE + MessageConstant.FAILED);
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        // 记录删除成功日志并返回成功结果
        log.info(MessageConstant.DELETE + MessageConstant.SUCCESS);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    //**********************************************************************************************/


    /**
     * 获取所有用户数量
     *
     * @return 结果
     */
    @Override
    public Result<Long> getAllUsersCount() {
        Long count = userMapper.selectCount(new QueryWrapper<>());
        return Result.success(count);
    }

    /**
     * 分页查询所有用户
     *
     * @param userSearchDTO 用户搜索条件
     * @return 分页信息的结果
     */
    @Override
    @Cacheable(cacheNames = "userCache", key = "#userSearchDTO.pageNum.toString() + '-' + #userSearchDTO.pageSize.toString() + '-' + (#userSearchDTO.username != null ? #userSearchDTO.username : 'null') + '-' + (#userSearchDTO.phone != null ? #userSearchDTO.phone : 'null') + '-' + (#userSearchDTO.userStatus != null ? #userSearchDTO.userStatus.toString() : 'null')")
    public Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO) {
        // 创建分页对象，设置当前页码和每页大小
        Page<User> page = new Page<>(userSearchDTO.getPageNum(), userSearchDTO.getPageSize());
        // 创建查询条件构造器
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        // 根据用户名条件构建模糊查询
        if (userSearchDTO.getUsername() != null && !userSearchDTO.getUsername().isEmpty()) {
            userQueryWrapper.like("username", userSearchDTO.getUsername());
        }
        // 根据手机号条件构建模糊查询
        if (userSearchDTO.getPhone() != null && !userSearchDTO.getPhone().isEmpty()) {
            userQueryWrapper.like("phone", userSearchDTO.getPhone());
        }
        // 根据用户状态条件构建精确查询
        if (userSearchDTO.getUserStatus() != null) {
            userQueryWrapper.eq("status", userSearchDTO.getUserStatus().getCode());
        }

        // 按创建时间降序排序
        userQueryWrapper.orderByDesc("create_time");

        // 执行分页查询
        IPage<User> userPage = userMapper.selectPage(page, userQueryWrapper);
        // 处理查询结果为空的情况
        if (userPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, Collections.emptyList()));
        }

        // 将User实体列表转换为UserManagementVO列表
        List<UserManagementVO> userVOList = userPage.getRecords().stream().map(
                user -> {
                    UserManagementVO userManagementVO = new UserManagementVO();
                    BeanUtils.copyProperties(user, userManagementVO);
                    return userManagementVO;
                }
        ).toList();
        // 返回分页结果
        return Result.success(new PageResult<>(userPage.getTotal(), userVOList));
    }

    @Override
    public Result addUser(UserAddDTO userAddDTO) {
        return null;
    }

    @Override
    public Result updateUser(UserDTO userDTO) {
        return null;
    }

    @Override
    public Result updateUserStatus(Long userId, Integer userStatus) {
        return null;
    }

    @Override
    public Result deleteUser(Long userId) {
        return null;
    }

    @Override
    public Result deleteUsers(List<Long> userIds) {
        return null;
    }
}

