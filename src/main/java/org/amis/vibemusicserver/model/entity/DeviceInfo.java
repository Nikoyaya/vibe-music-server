package org.amis.vibemusicserver.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author : KwokChichung
 * @description : 设备信息基类
 * @createDate : 2026/1/19
 */
@Data
public class DeviceInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（唯一）
     */
    private Long userId;

    /**
     * 用户名（必须）
     */
    private String username;

    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 设备类型：android/ios/web
     */
    private String deviceType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}