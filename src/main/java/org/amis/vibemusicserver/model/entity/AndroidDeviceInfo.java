package org.amis.vibemusicserver.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : KwokChichung
 * @description : Android设备信息
 * @createDate : 2026/1/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_android_device_info")
public class AndroidDeviceInfo extends DeviceInfo {

    /**
     * 设备型号
     */
    private String model;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 设备
     */
    private String device;

    /**
     * Android版本
     */
    private String androidVersion;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * SDK版本
     */
    private String sdkVersion;

    /**
     * 是否物理设备
     */
    private String isPhysicalDevice;  // 改为String类型以匹配原始数据
}