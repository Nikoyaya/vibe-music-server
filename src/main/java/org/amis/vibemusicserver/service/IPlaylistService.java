package org.amis.vibemusicserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.model.dto.PlaylistAddDTO;
import org.amis.vibemusicserver.model.dto.PlaylistDTO;
import org.amis.vibemusicserver.model.dto.PlaylistUpdateDTO;
import org.amis.vibemusicserver.model.entity.Playlist;
import org.amis.vibemusicserver.model.vo.PlaylistDetailVO;
import org.amis.vibemusicserver.model.vo.PlaylistVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;

import java.util.List;

/**
 *
 * @author : KwokChichung
 * @description : playlist service
 */
public interface IPlaylistService extends IService<Playlist> {

    // 获取所有歌单
    Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistDTO playlistDTO);

    // 获取所有歌单
    Result<PageResult<Playlist>> getAllPlaylistsInfo(PlaylistDTO playlistDTO);

    // 获取推荐歌单
    Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request);

    // 根据id获取歌单详情
    Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, HttpServletRequest request);

    // 获取所有歌单数量
    Result<Long> getAllPlaylistsCount(String style);

    // 添加歌单
    Result addPlaylist(PlaylistAddDTO playlistAddDTO);

    // 更新歌单
    Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO);

    // 更新歌单封面
    Result updatePlaylistCover(Long playlistId, String coverUrl);

    // 删除歌单
    Result deletePlaylist(Long playlistId);

    // 批量删除歌单
    Result deletePlaylists(List<Long> playlistIds);

}
