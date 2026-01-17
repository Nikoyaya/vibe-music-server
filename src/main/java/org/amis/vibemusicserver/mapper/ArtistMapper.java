package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.Artist;
import org.amis.vibemusicserver.model.vo.ArtistDetailVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author : KwokChichung
 * @description : 歌手 Mapper 接口
 * @createDate : 2026/1/3 22:47
 */
@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {

    // 根据id查询歌手详情
    ArtistDetailVO getArtistDetailById(Long artistId);
}