package org.amis.vibemusicserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 角色权限管理器
 * @createDate : 2026/1/7 5:38
 */
@Component
public class RolePermissionManager {

    private final RolePathPermissionsConfig rolePathPermissionsConfig;

    @Autowired
    public RolePermissionManager(RolePathPermissionsConfig rolePathPermissionsConfig) {
        this.rolePathPermissionsConfig = rolePathPermissionsConfig;
    }

    // 判断当前角色是否有权限访问请求的路径
    public boolean hasPermission(String role, String requestURI) {
        Map<String, List<String>> permissions = rolePathPermissionsConfig.getPermissions();
        List<String> allowedPaths = permissions.get(role);
        if (allowedPaths != null) {
            for (String path : allowedPaths) {
                if (requestURI.startsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }
}

