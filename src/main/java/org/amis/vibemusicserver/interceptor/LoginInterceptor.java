package org.amis.vibemusicserver.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.amis.vibemusicserver.config.RolePermissionManager;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.constant.PathConstant;
import org.amis.vibemusicserver.enumeration.ResultCodeEnum;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 拦截器，用于处理登录状态和权限验证
 * @createDate : 2026/1/7 5:37
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RolePermissionManager rolePermissionManager;
    @Autowired
    private Environment environment;

    /**
     * 发送错误响应
     *
     * @param response HTTP响应对象
     * @param status   HTTP状态码
     * @param message  错误消息
     * @throws IOException 写入响应时可能抛出IO异常
     */
    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        try {
            response.getWriter().write(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 预处理请求，进行登录状态和权限验证
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象
     * @return 是否继续执行后续的拦截器或处理器
     * @throws Exception 在处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 CORS 预检请求（OPTIONS 方法）直接通过
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }

        // 获取请求头中的token和请求路径
        String token = request.getHeader("Authorization");
        String path = request.getRequestURI();
        // 检查是否为开发或本地环境
        boolean isDevOrLocal = environment.acceptsProfiles(Profiles.of("local", "dev"));

        if (isDevOrLocal) {
            log.info("拦截器开始处理请求: {} {}", request.getMethod(), path);
        }

        // 处理token，去除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
            if (isDevOrLocal) {
                log.info("接收到token: {}...", token.length() > 20 ? token.substring(0, 20) + "..." : token);
            }
        } else {
            if (isDevOrLocal) {
                log.warn("Authorization头为空或格式不正确: {}", token);
            }
        }
        // 获取 Spring 的 PathMatcher 实例用于路径匹配
        PathMatcher pathMatcher = new AntPathMatcher();

        // 定义允许未登录用户访问的路径
        List<String> allowedPaths = Arrays.asList(
                PathConstant.PLAYLIST_DETAIL_PATH,
                PathConstant.ARTIST_DETAIL_PATH,
                PathConstant.SONG_LIST_PATH,
                PathConstant.SONG_DETAIL_PATH
        );

        // 检查当前路径是否为允许访问的路径
        boolean isAllowedPath = allowedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (isDevOrLocal) {
            log.info("路径 {} 是否为允许路径: {}", path, isAllowedPath);
        }

        // 处理无token情况
        if (token == null || token.isEmpty()) {
            if (isAllowedPath) {
                if (isDevOrLocal) {
                    log.info("允许未登录用户访问路径: {}", path);
                }
                return true; // 允许未登录用户访问这些路径
            }

            if (isDevOrLocal) {
                log.warn("缺少令牌，拒绝访问路径: {}", path);
            }
            sendErrorResponse(response, ResultCodeEnum.NOT_LOGIN.getCode(), MessageConstant.NOT_LOGIN); // 缺少令牌
            return false;
        }

        try {
            // 从redis中获取相同的token验证有效性
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);

            if (isDevOrLocal) {
                log.info("Redis中查找token结果: {}", redisToken != null ? "存在" : "不存在");
            }

            if (redisToken == null) {
                // token失效
                if (isDevOrLocal) {
                    log.warn("Token在Redis中不存在，已失效: {}", token);
                }
                throw new RuntimeException("Token not found in Redis");
            }

            // 解析token获取用户信息
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String role = (String) claims.get(JwtClaimsConstant.ROLE);

            // 获取用户ID，优先使用ADMIN_ID，如果没有则使用USER_ID
            Object userId = claims.get(JwtClaimsConstant.ADMIN_ID);
            if (userId == null) {
                userId = claims.get(JwtClaimsConstant.USER_ID);
            }

            if (isDevOrLocal) {
                log.info("解析token成功，角色: {}, 用户ID: {}", role, userId);
            }

            // 检查用户是否有权限访问该路径
            if (rolePermissionManager.hasPermission(role, path)) {
                // 把业务数据存储到ThreadLocal中
                ThreadLocalUtil.set(claims);
                if (isDevOrLocal) {
                    log.info("权限验证通过，设置ThreadLocal，允许访问路径: {}", path);
                }
                return true;
            } else {
                if (isDevOrLocal) {
                    log.warn("角色 {} 无权限访问路径: {}", role, path);
                }
                sendErrorResponse(response, ResultCodeEnum.NO_PERMISSION.getCode(), MessageConstant.NO_PERMISSION); // 无权限访问
                return false;
            }
        } catch (Exception e) {
            if (isDevOrLocal) {
                log.error("Token验证失败，路径: {}, 错误: {}", path, e.getMessage());
            }
            sendErrorResponse(response, ResultCodeEnum.TOKEN_INVALID.getCode(), MessageConstant.SESSION_EXPIRED); // 令牌无效
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}

