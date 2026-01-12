package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    @Cacheable(key = "#songDTO.pageNum.toString() + '-' + #songDTO.pageSize.toString() + '-' + #songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album")
    public Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request) {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 解析token获取用户信息
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

        // 设置默认的点赞状态为未点赞
        List<SongVO> songVOList = songPage.getRecords()
                .stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getCode())) //默认状态为：0
                .toList();

        // 如果用户已登录，处理个性化点赞状态
        if (map != null) {
            String role = (String) map.get(JwtClaimsConstant.ROLE);

            // 只处理普通用户的点赞状态
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

    /**
     * 获取歌手的所有歌曲
     *
     * @param songAndArtistDTO 歌手和歌曲DTO
     * @return 歌曲列表
     */
    @Override
    @Cacheable(key = "#songAndArtistDTO.pageNum.toString() + '-' + #songAndArtistDTO.pageSize.toString() + '-' + #songAndArtistDTO.songName + '-' + #songAndArtistDTO.album + '-' + #songAndArtistDTO.artistId.toString()")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songAndArtistDTO) {
        // 分页查询
        Page<SongAdminVO> page = new Page<>(songAndArtistDTO.getPageNum(), songAndArtistDTO.getPageSize());
        IPage<SongAdminVO> songPage = songMapper.getSongsWithArtistName(page, songAndArtistDTO.getArtistId(), songAndArtistDTO.getSongName(), songAndArtistDTO.getAlbum());

        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songPage.getRecords()));
    }


    /**
     * 获取推荐歌曲
     * 默认推荐数量：20
     *
     * @param request 请求对象，用来获取请求头中的token
     * @return 推荐歌曲列表
     */
    @Override
    public Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request) {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 解析token获取用户信息
        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 如果用户未登录，直接返回随机推荐歌曲
        if (map == null) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 获取用户 ID
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));

        // 查询用户收藏的歌曲 ID
        List<Long> favoriteSongIds = userFavoriteMapper.getFavoriteSongIdsByUserId(userId);
        if (favoriteSongIds.isEmpty()) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 获取用户喜欢的歌曲的风格id，并统计每个风格的出现次数
        List<Long> favoriteSongStylesId = songMapper.getFavoriteSongStyles(favoriteSongIds);
        Map<Long, Long> styleFrequency = favoriteSongStylesId.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 根据风格的出现次数进行排序，并获取前20个最受欢迎的风格id
        List<Long> sortedStyleId = styleFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        // 从Redis获取缓存的推荐歌曲
        String redisKey = "recommended_songs_" + userId;
        List<SongVO> cachedSongs = redisTemplate.opsForList().range(redisKey, 0, -1);

        // 如果没有缓存，从数据库获取推荐歌曲并缓存
        if (cachedSongs == null || cachedSongs.isEmpty()) {
            cachedSongs = songMapper.getRecommendedSongsByStyles(sortedStyleId, favoriteSongIds, 80);
            redisTemplate.opsForList().rightPushAll(redisKey, cachedSongs);
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
        }

        // 随机打乱缓存歌曲，取前20首
        Collections.shuffle(cachedSongs);
        List<SongVO> recommendedSongs = cachedSongs.subList(0, Math.min(20, cachedSongs.size()));

        // 如果推荐歌曲不足20首，补充随机歌曲
        if (recommendedSongs.size() < 20) {
            List<SongVO> randomSongs = songMapper.getRandomSongsWithArtist();
            Set<Long> addSongIds = recommendedSongs.stream()
                    .map(SongVO::getSongId)
                    .collect(Collectors.toSet());

            for (SongVO songVO : randomSongs) {
                if (randomSongs.size() >= 20) {
                    break;
                }
                if (!addSongIds.contains(songVO.getSongId())) {
                    recommendedSongs.add(songVO);
                }
            }
        }
        return Result.success(recommendedSongs);
    }


    /**
     * 获取歌曲详情
     *
     * @param songId  歌曲id
     * @param request HttpServletRequest，用于获取请求头中的 token
     * @return 歌曲详情
     */
    @Override
    @Cacheable(key = "#songId")
    public Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request) {
        // 根据歌曲ID获取歌曲详情
        SongDetailVO songDetailVO = songMapper.getSongDetailById(songId);

        // 获取请求头中的 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去掉 "Bearer " 前缀
        }

        // 解析JWT token获取用户信息
        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 如果 token 解析成功且用户为登录状态，进一步操作
        if (map != null) {
            // 获取用户角色
            String role = (String) map.get(JwtClaimsConstant.ROLE);
            // 只处理普通用户的点赞状态
            if (role.equals(RoleEnum.USER.getRole())) {
                // 转换用户ID
                Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
                Long userId = TypeConversionUtil.toLong(userIdObj);

                // 查询用户是否收藏了该歌曲
                UserFavorite favoriteSong = userFavoriteMapper.selectOne(new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("type", 0)
                        .eq("song_id", songId));
                // 如果已收藏，设置点赞状态
                if (favoriteSong != null) {
                    songDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getCode());
                }
            }
        }

        return Result.success(songDetailVO);
    }

    /**
     * 获取所有歌曲数量
     *
     * @param style 歌曲风格
     * @return 歌曲数量
     */
    @Override
    public Result<Long> getAllSongsCount(String style) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(style)) {
            queryWrapper.eq("style", style);
        }
        return Result.success(songMapper.selectCount(queryWrapper));
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

