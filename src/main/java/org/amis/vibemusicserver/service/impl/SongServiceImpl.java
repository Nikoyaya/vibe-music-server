package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.enumeration.LikeStatusEnum;
import org.amis.vibemusicserver.enumeration.RoleEnum;
import org.amis.vibemusicserver.mapper.SongMapper;
import org.amis.vibemusicserver.mapper.UserFavoriteMapper;
import org.amis.vibemusicserver.model.dto.SongAddDTO;
import org.amis.vibemusicserver.model.dto.SongAndArtistDTO;
import org.amis.vibemusicserver.model.dto.SongDTO;
import org.amis.vibemusicserver.model.dto.SongUpdateDTO;
import org.amis.vibemusicserver.model.entity.Song;
import org.amis.vibemusicserver.model.entity.UserFavorite;
import org.amis.vibemusicserver.model.vo.SongAdminVO;
import org.amis.vibemusicserver.model.vo.SongDetailVO;
import org.amis.vibemusicserver.model.vo.SongVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.ISongService;
import org.amis.vibemusicserver.service.MinioService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.amis.vibemusicserver.utils.TypeConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : KwokChichung
 * @description : 歌曲服务实现类
 * @createDate : 2026/1/9 2:11
 */
@Service
@CacheConfig(cacheNames = "songCache")
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取所有歌曲
     *
     * @param songDTO songDTO
     * @return 歌曲列表
     */
    @Override
    @Cacheable(cacheNames = "songCache", key = "#songDTO.pageNum.toString() + '-' + #songDTO.pageSize.toString() + '-' + #songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album")
    public Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 分页查询歌曲，并附带艺术家信息
        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(page, songDTO.getSongName(), songDTO.getArtistName(), songDTO.getAlbum());
        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 设置默认的状态
        List<SongVO> songVOList = songPage.getRecords()
                .stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getCode())) //默认状态为：0
                .toList();

        if (map != null) {
            String role = (String) map.get(JwtClaimsConstant.ROLE);

            if (role.equals(RoleEnum.USER.getRole())) {
                Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
                Long userId = TypeConversionUtil.toLong(userIdObj);

                // 获取用户收藏的音乐
                List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("type", 0));

                Set<Long> favoriteSongIds = favoriteSongs.stream()
                        .map(UserFavorite::getSongId)
                        .collect(Collectors.toSet());

                // 更新歌曲列表，标记用户喜欢的音乐
                for (SongVO songVO : songVOList) {
                    if (favoriteSongIds.contains(songVO.getSongId())) {
                        songVO.setLikeStatus(LikeStatusEnum.LIKE.getCode()); // 用户喜欢的音乐标记为：1
                    }
                }
            }
        }
        return Result.success(new PageResult<>(page.getTotal(), songVOList));
    }

    @Override
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO) {
        return null;
    }

    @Override
    public Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<Long> getAllSongsCount(String style) {
        return null;
    }

    @Override
    public Result addSong(SongAddDTO songAddDTO) {
        return null;
    }

    @Override
    public Result updateSong(SongUpdateDTO songUpdateDTO) {
        return null;
    }

    @Override
    public Result updateSongCover(Long songId, String coverUrl) {
        return null;
    }

    @Override
    public Result updateSongAudio(Long songId, String audioUrl, String duration) {
        return null;
    }

    @Override
    public Result deleteSong(Long songId) {
        return null;
    }

    @Override
    public Result deleteSongs(List<Long> songIds) {
        return null;
    }
}

