package org.amis.vibemusicserver.controller;

import jakarta.validation.Valid;
import org.amis.vibemusicserver.model.dto.PlaylistDTO;
import org.amis.vibemusicserver.model.dto.SongDTO;
import org.amis.vibemusicserver.model.vo.PlaylistVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IUserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/18 10:28
 */
@RestController
@RequestMapping("/favorite")
public class UserFavoriteController {

    @Autowired
    private IUserFavoriteService userFavoriteService;

    /**
     * 获取用户收藏的歌曲列表
     *
     * @return 用户收藏的歌曲列表
     */
    @PostMapping("/getFavoriteSongs")
    public Result<PageResult<SongVO>> getUserFavoriteSongs(@RequestBody @Valid SongDTO songDTO) {
        return userFavoriteService.getUserFavoriteSongs(songDTO);
    }

    /**
     * 收藏歌曲
     *
     * @param songId 歌曲id
     * @return 收藏结果
     */
    @PostMapping("/collectSong")
    public Result collectSong(@RequestParam Long songId) {
        return userFavoriteService.collectSong(songId);
    }

    /**
     * 取消收藏歌曲
     *
     * @param songId 歌曲id
     * @return 取消收藏结果
     */
    @DeleteMapping("/cancelCollectSong")
    public Result cancelCollectSong(@RequestParam Long songId) {
        return userFavoriteService.cancelCollectSong(songId);
    }

    /**
     * 获取用户收藏的歌单列表
     *
     * @return 用户收藏的歌单列表
     */
    @PostMapping("/getFavoritePlaylists")
    public Result<PageResult<PlaylistVO>> getFavoritePlaylists(@RequestBody @Valid PlaylistDTO playlistDTO) {
        return userFavoriteService.getUserFavoritePlaylists(playlistDTO);
    }

    /**
     * 收藏歌单
     *
     * @param playlistId 歌单id
     * @return 收藏结果
     */
    @PostMapping("/collectPlaylist")
    public Result collectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.collectPlaylist(playlistId);
    }

    /**
     * 取消收藏歌单
     *
     * @param playlistId 歌单id
     * @return 取消收藏结果
     */
    @DeleteMapping("/cancelCollectPlaylist")
    public Result cancelCollectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.cancelCollectPlaylist(playlistId);
    }
}

