package org.amis.vibemusicserver.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : KwokChichung
 * @description : iOS设备信息
 * @createDate : 2026/1/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_ios_device_info")
public class IosDeviceInfo extends DeviceInfo {

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统版本
     */
    private String systemVersion;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 本地设备型号
     */
    private String localizedModel;

    /**
     * 标识符
     */
    private String identifier;
}