package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : KwokChichung
 * @description : Mapper 接口
 * @createDate : 2026/1/3 17:48
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}

