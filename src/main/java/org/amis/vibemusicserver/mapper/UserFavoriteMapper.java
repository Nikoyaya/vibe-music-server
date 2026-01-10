package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    // 查询用户收藏的所有歌曲ID
    List<Long> getUserFavoriteSongIds(@Param("userId") Long userId);

    // 查询用户收藏的所有歌单ID
    List<Long> getUserFavoritePlaylistIds(@Param("userId") Long userId);

    // 查询用户收藏的所有歌曲ID
    List<Long> getFavoriteSongIdsByUserId(@Param("userId") Long userId);

    // 查询用户收藏的所有歌单ID
    List<Long> getFavoritePlaylistIdsByUserId(@Param("userId") Long userId);

    // 根据 style 查询对应的 id
    List<Long> getFavoriteIdsByStyle(List<String> favoriteStyles);
}
