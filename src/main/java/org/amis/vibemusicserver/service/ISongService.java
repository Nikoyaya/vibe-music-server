package org.amis.vibemusicserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.model.dto.SongAddDTO;
import org.amis.vibemusicserver.model.dto.SongAndArtistDTO;
import org.amis.vibemusicserver.model.dto.SongDTO;
import org.amis.vibemusicserver.model.dto.SongUpdateDTO;
import org.amis.vibemusicserver.model.entity.Song;
import org.amis.vibemusicserver.model.vo.SongAdminVO;
import org.amis.vibemusicserver.model.vo.SongDetailVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;

import java.util.List;

/**
 * @author : KwokChichung
 * @description : 歌曲服务接口
 * @createDate : 2026/1/9 22:49
 */
public interface ISongService extends IService<Song> {

    // 获取所有歌曲
    Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request);

    // 获取所有歌曲
    Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO);

    // 获取推荐歌曲
    Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request);

    // 根据id获取歌曲详情
    Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request);

    // 获取所有歌曲数量
    Result<Long> getAllSongsCount(String style);

    // 添加歌曲信息
    Result addSong(SongAddDTO songAddDTO);

    // 更新歌曲信息
    Result updateSong(SongUpdateDTO songUpdateDTO);

    // 更新歌曲封面
    Result updateSongCover(Long songId, String coverUrl);

    // 更新歌曲音频
    Result updateSongAudio(Long songId, String audioUrl, String duration);

    // 删除歌曲
    Result deleteSong(Long songId);

    // 批量删除歌曲
    Result deleteSongs(List<Long> songIds);
}
