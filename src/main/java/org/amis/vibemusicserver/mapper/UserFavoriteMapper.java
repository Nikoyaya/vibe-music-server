package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
