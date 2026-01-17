package org.amis.vibemusicserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.model.dto.ArtistAddDTO;
import org.amis.vibemusicserver.model.dto.ArtistDTO;
import org.amis.vibemusicserver.model.dto.ArtistUpdateDTO;
import org.amis.vibemusicserver.model.entity.Artist;
import org.amis.vibemusicserver.model.vo.ArtistDetailVO;
import org.amis.vibemusicserver.model.vo.ArtistNameVO;
import org.amis.vibemusicserver.model.vo.ArtistVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;

import java.util.List;

/**
 * @author KwokChichung
 * @description 歌手服务类
 */
public interface IArtistService extends IService<Artist> {

    // 获取所有歌手
    Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO);

    // 获取所有歌手
    Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO);

    // 获取所有歌手id和名字
    Result<List<ArtistNameVO>> getAllArtistNames();

    // 获取随机歌手
    Result<List<ArtistVO>> getRandomArtists();

    // 根据id获取歌手详情
    Result<ArtistDetailVO> getArtistDetail(Long artistId, HttpServletRequest request);

    // 获取所有歌手数量
    Result<Long> getAllArtistsCount(Integer gender, String area);

    // 添加歌手
    Result addArtist(ArtistAddDTO artistAddDTO);

    // 更新歌手
    Result updateArtist(ArtistUpdateDTO artistUpdateDTO);

    // 更新歌手头像
    Result updateArtistAvatar(Long artistId, String avatar);

    // 删除歌手
    Result deleteArtist(Long ArtistId);

    // 批量删除歌手
    Result deleteArtists(List<Long> artistIds);

}
