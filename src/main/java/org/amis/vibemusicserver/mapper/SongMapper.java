package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.amis.vibemusicserver.model.entity.Song;
import org.amis.vibemusicserver.model.vo.SongAdminVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : KwokChichung
 * @description :  Mapper 接口
 * @createDate : 2026/1/9 2:13
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /**
     * 根据歌曲名称、艺术家和专辑获取带有艺术家的歌曲列表
     *
     * @param page       分页对象
     * @param songName   歌曲名称
     * @param artistName 艺术家名称
     * @param album      专辑名称
     * @return 带有艺术家的歌曲列表分页对象
     */

    IPage<SongVO> getSongsWithArtist(Page<SongVO> page,
                                     @Param("songName") String songName,
                                     @Param("artistName") String artistName,
                                     @Param("album") String album);


    /**
     * 根据歌手ID、歌曲名称和专辑获取带有艺术家的歌曲列表
     *
     * @param page     page
     * @param artistId 歌手ID
     * @param songName 歌曲名称
     * @param album    专辑
     * @return 带有艺术家的歌曲列表分页对象
     */

    IPage<SongAdminVO> getSongsWithArtistName(Page<SongAdminVO> page,
                                              @Param("artistId") Long artistId,
                                              @Param("songName") String songName,
                                              @Param("album") String album);

    /**
     * 获取随机歌曲列表
     *
     * @return 随机歌曲列表
     */
    List<SongVO> getRandomSongsWithArtist();

    /**
     * 根据用户收藏的歌曲id列表获取歌曲风格ID
     *
     * @param favoriteSongIds 用户收藏的歌曲id列表
     * @return 歌曲风格ID列表
     */
    List<Long> getFavoriteSongStyles(@Param("favoriteSongIds") List<Long> favoriteSongIds);

    /**
     * 根据用户收藏的歌曲id列表获取歌曲列表
     *
     * @param sortedStyleIds  排序后的歌曲风格ID列表
     * @param favoriteSongIds 用户收藏的歌曲id列表
     * @param limit           限制返回的歌曲数量
     * @return 推荐的歌曲列表
     */
    List<SongVO> getRecommendedSongsByStyles(@Param("sortedStyleIds") List<Long> sortedStyleIds,
                                             @Param("favoriteSongIds") List<Long> favoriteSongIds,
                                             @Param("limit") int limit);

}

