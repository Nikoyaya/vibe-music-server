package org.amis.vibemusicserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.mapper.UserMapper;
import org.amis.vibemusicserver.model.entity.User;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.DeviceInfoService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.TypeConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : KwokChichung
 * @description : 通用控制器，提供公共接口
 * @createDate : 2026/1/19
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeviceInfoService deviceInfoService;

    /**
     * 获取客户端IP和设备信息
     * 支持获取移动端和Web端的真实IP地址和设备信息
     *
     * @param request     HttpServletRequest对象
     * @param requestData 请求数据（包含设备信息和客户端类型）
     * @return 包含客户端IP和设备信息的结果
     */
    @PostMapping("/getClientIp")
    public Result<Map<String, Object>> getClientIp(
            HttpServletRequest request,
            @RequestBody Map<String, Object> requestData) {

        log.info("==== /common/getClientIp 请求开始 ====");
        log.info("请求头: {}", Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", ")));
        log.info("请求体: {}", requestData);

        // 直接调用Service层的完整处理方法
        return deviceInfoService.processClientInfoRequest(request, requestData);
    }
}