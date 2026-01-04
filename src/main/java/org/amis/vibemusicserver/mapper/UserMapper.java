package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : KwokChichung
 * @description : Mapper 接口
 * @createDate : 2026/1/5 01:12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
