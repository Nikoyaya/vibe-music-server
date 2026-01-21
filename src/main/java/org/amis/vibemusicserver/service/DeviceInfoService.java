package org.amis.vibemusicserver.service;

import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.result.Result;

import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 设备信息服务接口
 * @createDate : 2026/1/21
 */
public interface DeviceInfoService {

    /**
     * 保存设备信息
     *
     * @param clientType 客户端类型
     * @param ip IP地址
     * @param userId 用户ID
     * @param username 用户名
     * @param deviceInfo 设备信息
     */
    void saveDeviceInfo(String clientType, String ip, Long userId, String username, Map<String, String> deviceInfo);

    /**
     * 截断字符串到指定长度
     *
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    String truncateString(String str, int maxLength);

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest对象
     * @return 客户端真实IP地址
     */
    String getClientIPAddress(HttpServletRequest request);

    /**
     * 记录设备信息日志
     *
     * @param deviceInfo 设备信息
     */
    void logDeviceInfo(Map<String, String> deviceInfo);

    /**
     * 处理客户端IP和设备信息请求
     *
     * @param request HttpServletRequest对象
     * @param requestData 请求数据
     * @return 处理结果
     */
    Result processClientInfoRequest(HttpServletRequest request, Map<String, Object> requestData);
}