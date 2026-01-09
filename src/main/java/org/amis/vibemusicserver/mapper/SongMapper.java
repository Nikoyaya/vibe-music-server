package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.amis.vibemusicserver.model.entity.Song;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author : KwokChichung
 * @description :  Mapper 接口
 * @createDate : 2026/1/9 2:13
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /**
     * 根据歌曲名称、艺术家和专辑获取带有艺术家的歌曲列表
     * @param page 分页对象
     * @param songName 歌曲名称
     * @param artistName 艺术家名称
     * @param album 专辑名称
     * @return 带有艺术家的歌曲列表分页对象
     */
    @Select("SELECT s.id AS songId, s.name AS songName, s.album, s.duration, s.cover_url AS coverUrl, " +
            "s.audio_url AS audioUrl, s.release_time AS releaseTime, a.name AS artistName " +
            "FROM tb_song s LEFT JOIN tb_artist a ON s.artist_id = a.id " +
            "WHERE (#{songName} IS NULL OR s.name LIKE CONCAT('%',#{songName},'%')) " +
            "AND (#{artistName} IS NULL OR a.name LIKE CONCAT('%',#{artistName},'%')) " +
            "AND (#{album} IS NULL OR s.album LIKE CONCAT('%',#{album},'%'))")
    IPage<SongVO> getSongsWithArtist(Page<SongVO> page,
                                     @Param("songName") String songName,
                                     @Param("artistName") String artistName,
                                     @Param("album") String album);
}

