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
            // 单点登录控制：清理旧会话的所有token
            String userTokenKey = "user_token:" + user.getId();
            String oldRefreshToken = stringRedisTemplate.opsForValue().get(userTokenKey);
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
                    stringRedisTemplate.delete(userTokenKey);
                    stringRedisTemplate.delete("access_token:" + oldRefreshToken);

                    log.info("已完全清理旧会话Token，user ID: {}", user.getId());
                } catch (Exception e) {
                    log.error("清理旧Token失败，执行强制清理，user ID: {}", user.getId(), e);
                    // 强制清理所有可能的残留token
                    stringRedisTemplate.delete(userTokenKey);
                    stringRedisTemplate.delete(oldRefreshToken);
                }
            }

            // 创建JWT的claims（声明）
            HashMap<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());

            // 生成双Token
            String accessToken = JwtUtil.generateAccessToken(claims);
            String refreshToken = JwtUtil.generateRefreshToken(claims);
            log.info("双Token生成完成，user ID: {}", user.getId());

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
            stringRedisTemplate.opsForValue().set(userTokenKey, refreshToken, 15, TimeUnit.DAYS);

            log.info("双Token存储到Redis完成，使用refresh_token控制单点登录");
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS,
                new TokenDTO(accessToken, refreshToken));
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

        // 获取当前用户信息用于比较
        User currentUser = userMapper.selectById(userId);

        // 检查用户名是否已被其他用户使用（只有当用户名发生改变时检查）
        if (!userDTO.getUsername().equals(currentUser.getUsername())) {
            User userByUsername = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("username", userDTO.getUsername()));
            if (userByUsername != null && !userByUsername.getId().equals(userId)) {
                log.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 检查手机号是否已被其他用户使用（只有当手机号不为null且发生改变时检查）
        if (userDTO.getPhone() != null && !userDTO.getPhone().equals(currentUser.getPhone())) {
            User userByPhone = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("phone", userDTO.getPhone()));
            if (userByPhone != null && !userByPhone.getId().equals(userId)) {
                log.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
                return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 检查邮箱是否已被其他用户使用（只有当邮箱不为null且发生改变时检查）
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(currentUser.getEmail())) {
            User userByEmail = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("email", userDTO.getEmail()));
            if (userByEmail != null && !userByEmail.getId().equals(userId)) {
                log.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 直接使用MyBatis-Plus的更新方法，避免手动创建对象和设置id
        User user = new User();
        user.setUpdateTime(LocalDateTime.now());

        // 只复制非null字段到更新对象
        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getIntroduction() != null) {
            user.setIntroduction(userDTO.getIntroduction());
        }

        // 执行更新操作，检查是否成功
        int updateCount = userMapper.update(user, new QueryWrapper<User>().eq("id", userId));
        if (updateCount == 0) {
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

            // 从Redis中删除token及SSO控制记录
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = ((Integer) claims.get(JwtClaimsConstant.USER_ID)).longValue();
            stringRedisTemplate.delete("user_token:" + userId);
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

    /**
     * 添加用户
     *
     * @param userAddDTO 用户添加DTO对象
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result addUser(UserAddDTO userAddDTO) {
        // 构建查询条件，检查用户名、邮箱、手机号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAddDTO.getUsername())
                .or()
                .eq("email", userAddDTO.getEmail())
                .or()
                .eq("phone", userAddDTO.getPhone());

        // 查询已存在的用户
        List<User> existingUsers = userMapper.selectList(queryWrapper);
        if (!existingUsers.isEmpty()) {
            for (User user : existingUsers) {
                // 检查用户名是否重复
                if (user.getUsername().equals(userAddDTO.getUsername())) {
                    return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                }
                // 检查邮箱是否重复
                if (user.getEmail().equals(userAddDTO.getEmail())) {
                    return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                }
                // 检查手机号是否重复
                if (user.getPhone().equals(userAddDTO.getPhone())) {
                    return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
                }
            }
        }
        // 对密码进行MD5加密
        String passwordMD5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        // 创建用户对象并设置属性
        User user = new User();
        user.setUsername(userAddDTO.getUsername())
                .setPassword(passwordMD5)
                .setPhone(userAddDTO.getPhone())
                .setEmail(userAddDTO.getEmail())
                .setIntroduction(userAddDTO.getIntroduction())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        // 前端传递的用户状态（1：启用，0：禁用）需反转
        if (userAddDTO.getUserStatus().getCode() == 1) {
            user.setUserStatus(UserStatusEnum.ENABLE);  // 数据库：0-启用
        } else if (userAddDTO.getUserStatus().getCode() == 0) {
            user.setUserStatus(UserStatusEnum.DISABLE); // 数据库：1-禁用
        }

        // 插入用户数据到数据库
        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息DTO对象
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUser(UserDTO userDTO) {
        // 获取用户ID和用户名
        Long userId = userDTO.getId();
        String username = userDTO.getUsername();

        // 获取当前用户信息用于比较
        User currentUser = userMapper.selectById(userId);

        // 检查用户名是否与其他用户重复（用户名必须提供，并且只有当用户名发生改变时检查）
        if (!username.equals(currentUser.getUsername())) {
            User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if (userByUsername != null && !userByUsername.getId().equals(userId)) {
                return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 检查手机号是否与其他用户重复（只有当手机号不为null且发生改变时检查）
        if (userDTO.getPhone() != null && !userDTO.getPhone().equals(currentUser.getPhone())) {
            User userByPhone = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userDTO.getPhone()));
            if (userByPhone != null && !userByPhone.getId().equals(userId)) {
                return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 检查邮箱是否与其他用户重复（只有当邮箱不为null且发生改变时检查）
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(currentUser.getEmail())) {
            User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userDTO.getEmail()));
            if (userByEmail != null && !userByEmail.getId().equals(userId)) {
                return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 创建用户对象并复制属性
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 手动设置id，避免BeanUtils.copyProperties将null值复制过来
        user.setId(userId);
        user.setUpdateTime(LocalDateTime.now());

        // 执行更新操作并检查结果
        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户ID
     * @param userStatus 用户状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserStatus(Long userId, Integer userStatus) {
        // 校验用户状态参数的有效性
        UserStatusEnum statusEnum;
        if (userStatus == 0) {
            statusEnum = UserStatusEnum.ENABLE;    // 0表示启用状态
        } else if (userStatus == 1) {
            statusEnum = UserStatusEnum.DISABLE;   // 1表示禁用状态
        } else {
            return Result.error(MessageConstant.USER_STATUS_INVALID);  // 状态值无效
        }

        // 构建更新对象并设置更新时间
        User user = new User();
        user.setUserStatus(statusEnum)
                .setUpdateTime(LocalDateTime.now());

        // 执行更新操作，检查是否成功更新记录
        int updateRows = userMapper.update(user, new QueryWrapper<User>().eq("id", userId));
        if (updateRows == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);  // 更新失败
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);   // 更新成功
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUser(Long userId) {
        // 调用userMapper删除指定ID的用户
        int row = userMapper.deleteById(userId);
        // 如果删除行数为0，说明用户不存在或删除失败，返回错误信息
        if (row == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        // 删除成功，返回成功信息
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUsers(List<Long> userIds) {
        // 调用mapper批量删除用户
        int rows = userMapper.deleteByIds(userIds);
        // 如果删除行数为0，返回失败结果
        if (rows == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        // 返回成功结果
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
}

