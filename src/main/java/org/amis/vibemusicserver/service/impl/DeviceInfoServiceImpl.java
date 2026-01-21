package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.mapper.AndroidDeviceInfoMapper;
import org.amis.vibemusicserver.mapper.IosDeviceInfoMapper;
import org.amis.vibemusicserver.mapper.UserMapper;
import org.amis.vibemusicserver.mapper.WebDeviceInfoMapper;
import org.amis.vibemusicserver.model.entity.AndroidDeviceInfo;
import org.amis.vibemusicserver.model.entity.IosDeviceInfo;
import org.amis.vibemusicserver.model.entity.User;
import org.amis.vibemusicserver.model.entity.WebDeviceInfo;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.DeviceInfoService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.TypeConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 设备信息服务实现
 * @createDate : 2026/1/21
 */
@Slf4j
@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Autowired
    private AndroidDeviceInfoMapper androidDeviceInfoMapper;

    @Autowired
    private IosDeviceInfoMapper iosDeviceInfoMapper;

    @Autowired
    private WebDeviceInfoMapper webDeviceInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void saveDeviceInfo(String clientType, String ip, Long userId, String username, Map<String, String> deviceInfo) {
        LocalDateTime now = LocalDateTime.now();

        switch (clientType.toLowerCase()) {
            case "android":
                saveAndroidDeviceInfo(userId, username, ip, deviceInfo, now);
                break;

            case "ios":
                saveIosDeviceInfo(userId, username, ip, deviceInfo, now);
                break;

            case "web":
                saveWebDeviceInfo(userId, username, ip, deviceInfo, now);
                break;

            default:
                log.debug("未知客户端类型: {}", clientType);
        }
    }

    /**
     * 保存Android设备信息
     */
    private void saveAndroidDeviceInfo(Long userId, String username, String ip, Map<String, String> deviceInfo, LocalDateTime now) {
        AndroidDeviceInfo existingAndroid = androidDeviceInfoMapper.selectOne(
                new LambdaQueryWrapper<AndroidDeviceInfo>()
                        .eq(AndroidDeviceInfo::getUserId, userId)
                        .eq(AndroidDeviceInfo::getDeviceType, "android")
        );

        AndroidDeviceInfo androidInfo = new AndroidDeviceInfo();
        androidInfo.setUserId(userId);
        androidInfo.setUsername(username);
        androidInfo.setIpAddress(ip);
        androidInfo.setDeviceType("android");
        androidInfo.setCreateTime(now);

        // 设置Android特有字段
        if (deviceInfo != null) {
            androidInfo.setModel(deviceInfo.get("设备型号"));
            androidInfo.setBrand(deviceInfo.get("品牌"));
            androidInfo.setDevice(deviceInfo.get("设备"));
            androidInfo.setAndroidVersion(deviceInfo.get("Android 版本"));
            androidInfo.setDeviceId(deviceInfo.get("设备ID"));
            androidInfo.setSdkVersion(deviceInfo.get("SDK 版本"));
            androidInfo.setIsPhysicalDevice(deviceInfo.get("是否物理设备"));
        }

        if (existingAndroid != null) {
            androidInfo.setId(existingAndroid.getId());
            androidDeviceInfoMapper.updateById(androidInfo);
            log.debug("更新Android设备信息: userId={}, username={}", userId, username);
        } else {
            androidDeviceInfoMapper.insert(androidInfo);
            log.debug("新增Android设备信息: userId={}, username={}", userId, username);
        }
    }

    /**
     * 保存iOS设备信息
     */
    private void saveIosDeviceInfo(Long userId, String username, String ip, Map<String, String> deviceInfo, LocalDateTime now) {
        IosDeviceInfo existingIos = iosDeviceInfoMapper.selectOne(
                new LambdaQueryWrapper<IosDeviceInfo>()
                        .eq(IosDeviceInfo::getUserId, userId)
                        .eq(IosDeviceInfo::getDeviceType, "ios")
        );

        IosDeviceInfo iosInfo = new IosDeviceInfo();
        iosInfo.setUserId(userId);
        iosInfo.setUsername(username);
        iosInfo.setIpAddress(ip);
        iosInfo.setDeviceType("ios");
        iosInfo.setCreateTime(now);

        // 设置iOS特有字段
        if (deviceInfo != null) {
            iosInfo.setDeviceName(deviceInfo.get("设备名称"));
            iosInfo.setSystemName(deviceInfo.get("系统名称"));
            iosInfo.setSystemVersion(deviceInfo.get("系统版本"));
            iosInfo.setModel(deviceInfo.get("设备型号"));
            iosInfo.setLocalizedModel(deviceInfo.get("本地设备型号"));
            iosInfo.setIdentifier(deviceInfo.get("标识"));
        }

        if (existingIos != null) {
            iosInfo.setId(existingIos.getId());
            iosDeviceInfoMapper.updateById(iosInfo);
            log.debug("更新iOS设备信息: userId={}, username={}", userId, username);
        } else {
            iosDeviceInfoMapper.insert(iosInfo);
            log.debug("新增iOS设备信息: userId={}, username={}", userId, username);
        }
    }

    /**
     * 保存Web设备信息
     */
    private void saveWebDeviceInfo(Long userId, String username, String ip, Map<String, String> deviceInfo, LocalDateTime now) {
        WebDeviceInfo existingWeb = webDeviceInfoMapper.selectOne(
                new LambdaQueryWrapper<WebDeviceInfo>()
                        .eq(WebDeviceInfo::getUserId, userId)
                        .eq(WebDeviceInfo::getDeviceType, "web")
        );

        WebDeviceInfo webInfo = new WebDeviceInfo();
        webInfo.setUserId(userId);
        webInfo.setUsername(username);
        webInfo.setIpAddress(ip);
        webInfo.setDeviceType("web");
        webInfo.setCreateTime(now);

        // 设置Web特有字段，添加长度验证防止数据截断
        if (deviceInfo != null) {
            webInfo.setBrowserName(truncateString(deviceInfo.get("浏览器"), 200));
            webInfo.setUserAgent(truncateString(deviceInfo.get("用户代理"), 1000));
            webInfo.setAppVersion(truncateString(deviceInfo.get("应用版本"), 500));
            webInfo.setAppCodeName(truncateString(deviceInfo.get("应用代码"), 200));
            webInfo.setPlatform(truncateString(deviceInfo.get("平台"), 200));
            webInfo.setVendor(truncateString(deviceInfo.get("供应商"), 200));
            webInfo.setLanguage(truncateString(deviceInfo.get("语言"), 100));
        }

        if (existingWeb != null) {
            webInfo.setId(existingWeb.getId());
            webDeviceInfoMapper.updateById(webInfo);
            log.debug("更新Web设备信息: userId={}, username={}", userId, username);
        } else {
            webDeviceInfoMapper.insert(webInfo);
            log.debug("新增Web设备信息: userId={}, username={}", userId, username);
        }
    }

    @Override
    public String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        log.warn("字符串被截断: 原长度={}, 截断长度={}", str.length(), maxLength);
        return str.substring(0, maxLength);
    }

    @Override
    public String getClientIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP才是真实IP
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
            return ip;
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        return ip;
    }

    @Override
    public void logDeviceInfo(Map<String, String> deviceInfo) {
        log.info("===== 设备信息 =====");
        deviceInfo.forEach((key, value) ->
                log.info("{}: {}", key, value));
        log.info("==================");
    }

    @Override
    public Result processClientInfoRequest(HttpServletRequest request, Map<String, Object> requestData) {
        log.debug("==== 处理客户端信息请求开始 ====");

        String ip = getClientIPAddress(request);
        log.debug("获取到客户端IP: {}", ip);

        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 解析token获取用户信息
        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            try {
                map = JwtUtil.parseToken(token);
            } catch (Exception e) {
                return Result.error(MessageConstant.TOKEN + MessageConstant.ERROR);
            }
        }

        if (map == null) {
            return Result.error(MessageConstant.TOKEN + MessageConstant.INVALID);
        }

        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        User user = userMapper.selectById(userId);
        String username = user != null ? user.getUsername() : "未知用户";

        String clientType = (String) requestData.get("clientType");
        Map<String, String> deviceInfo = (Map<String, String>) requestData.get("deviceInfo");

        if (username == null || username.trim().isEmpty()) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_LOGIN);
        }

        // 保存设备信息
        saveDeviceInfo(clientType, ip, userId, username, deviceInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("ip", ip);
        result.put("username", username);
        result.put("clientType", clientType);

        if (deviceInfo != null && !deviceInfo.isEmpty()) {
            result.put("deviceInfo", deviceInfo);
            logDeviceInfo(deviceInfo);
        }

        log.debug("==== 处理客户端信息请求完成 ====");
        return Result.success(result);
    }
}