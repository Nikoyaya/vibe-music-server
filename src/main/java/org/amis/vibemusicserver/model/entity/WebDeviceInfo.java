package org.amis.vibemusicserver.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : KwokChichung
 * @description : Web设备信息
 * @createDate : 2026/1/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_web_device_info")
public class WebDeviceInfo extends DeviceInfo {

    /**
     * 浏览器名称
     */
    private String browserName;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 应用代码
     */
    private String appCodeName;

    /**
     * 平台
     */
    private String platform;

    /**
     * 供应商
     */
    private String vendor;

    /**
     * 语言
     */
    private String language;
}