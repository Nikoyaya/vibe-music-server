package org.amis.vibemusicserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.model.dto.SongAndArtistDTO;
import org.amis.vibemusicserver.model.dto.SongDTO;
import org.amis.vibemusicserver.model.vo.SongAdminVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.ISongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

