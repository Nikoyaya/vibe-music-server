package org.amis.vibemusicserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.mapper.PlaylistMapper;
import org.amis.vibemusicserver.model.dto.PlaylistAddDTO;
import org.amis.vibemusicserver.model.dto.PlaylistDTO;
import org.amis.vibemusicserver.model.dto.PlaylistUpdateDTO;
import org.amis.vibemusicserver.model.entity.Playlist;
import org.amis.vibemusicserver.model.vo.PlaylistDetailVO;
import org.amis.vibemusicserver.model.vo.PlaylistVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IPlaylistService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : KwokChichung
 * @description : 歌单服务实现类
 * @createDate : 2026/1/28 19:28
 */
@Service
@CacheConfig(cacheNames = "playlistCache")
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements IPlaylistService {

    @Autowired
    private PlaylistMapper playlistMapper;

    @Override
    @Cacheable(key = "#playlistDTO.pageNum + '-' + #playlistDTO.pageSize + '-' + #playlistDTO.title + '-' + #playlistDTO.style")
    public Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistDTO playlistDTO) {
        // 创建分页对象
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        QueryWrapper<Playlist> playlistQueryWrapper = new QueryWrapper<>();
        // 根据标题条件构建模糊查询
        if (playlistDTO.getTitle() != null) {
            playlistQueryWrapper.like("title", playlistDTO.getTitle());
        }
        // 根据风格条件构建精确查询
        if (playlistDTO.getStyle() != null) {
            playlistQueryWrapper.eq("style", playlistDTO.getStyle());
        }

        // 执行分页查询
        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, playlistQueryWrapper);
        // 处理查询结果为空的情况
        if (playlistPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 将实体对象转换为VO对象
        List<PlaylistVO> playlistVOList = playlistPage.getRecords().stream()
                .map(playlist -> {
                    PlaylistVO playlistVO = new PlaylistVO();
                    BeanUtils.copyProperties(playlist, playlistVO);
                    return playlistVO;
                }).toList();

        // 返回分页结果
        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistVOList));
    }

    @Override
    public Result<PageResult<Playlist>> getAllPlaylistsInfo(PlaylistDTO playlistDTO) {
        return null;
    }

    @Override
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<Long> getAllPlaylistsCount(String style) {
        return null;
    }

    @Override
    public Result addPlaylist(PlaylistAddDTO playlistAddDTO) {
        return null;
    }

    @Override
    public Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO) {
        return null;
    }

    @Override
    public Result updatePlaylistCover(Long playlistId, String coverUrl) {
        return null;
    }

    @Override
    public Result deletePlaylist(Long playlistId) {
        return null;
    }

    @Override
    public Result deletePlaylists(List<Long> playlistIds) {
        return null;
    }
}

