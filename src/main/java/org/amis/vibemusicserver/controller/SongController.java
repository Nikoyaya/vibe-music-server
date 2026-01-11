package org.amis.vibemusicserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.model.dto.SongAndArtistDTO;
import org.amis.vibemusicserver.model.dto.SongDTO;
import org.amis.vibemusicserver.model.vo.SongAdminVO;
import org.amis.vibemusicserver.model.vo.SongDetailVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.ISongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/9 23:53
 */
@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private ISongService songService;

    /**
     * 获取所有歌曲
     *
     * @param songDTO 歌曲查询条件
     * @param request 请求对象
     * @return 歌曲分页结果
     */
    @PostMapping("/getAllSongs")
    public Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request) {
        return songService.getAllSongs(songDTO, request);

    }

    /**
     * 获取推荐歌曲
     * 推荐歌曲的数量为 20
     *
     * @param request 请求
     * @return 推荐歌曲列表
     */
    @GetMapping("/getRecommendedSongs")
    public Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request) {
        return songService.getRecommendedSongs(request);
    }

    /**
     * 获取歌曲详情
     *
     * @param songId 歌曲id
     * @return 歌曲详情
     */
    @GetMapping("/getSongDetail/{id}")
    public Result<SongDetailVO> getSongDetail(@PathVariable("id") Long songId, HttpServletRequest request) {
        return songService.getSongDetail(songId, request);
    }
}

