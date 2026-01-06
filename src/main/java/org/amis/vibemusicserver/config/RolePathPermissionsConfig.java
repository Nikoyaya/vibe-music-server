package org.amis.vibemusicserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 配置类，用于读取角色路径权限的配置信息
 * @createDate : 2026/1/7 5:39
 */
@Component
@ConfigurationProperties(prefix = "role-path-permissions")
public class RolePathPermissionsConfig {

    private Map<String, List<String>> permissions;

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }
}

