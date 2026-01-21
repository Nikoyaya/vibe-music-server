package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.AndroidDeviceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : KwokChichung
 * @description : Android设备信息Mapper
 * @createDate : 2026/1/19
 */
@Mapper
public interface AndroidDeviceInfoMapper extends BaseMapper<AndroidDeviceInfo> {
}