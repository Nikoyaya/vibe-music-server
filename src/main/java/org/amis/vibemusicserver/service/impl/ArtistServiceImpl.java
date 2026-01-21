package org.amis.vibemusicserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.amis.vibemusicserver.mapper.ArtistMapper;
import org.amis.vibemusicserver.model.dto.ArtistAddDTO;
import org.amis.vibemusicserver.model.dto.ArtistDTO;
import org.amis.vibemusicserver.model.dto.ArtistUpdateDTO;
import org.amis.vibemusicserver.model.entity.Artist;
import org.amis.vibemusicserver.model.vo.ArtistDetailVO;
import org.amis.vibemusicserver.model.vo.ArtistNameVO;
import org.amis.vibemusicserver.model.vo.ArtistVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IArtistService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/17 11:32
 */
@Service
@CacheConfig(cacheNames = "artistCache")
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements IArtistService {

    @Override
    public Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO) {
        return null;
    }

    @Override
    public Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO) {
        return null;
    }

    @Override
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        return null;
    }

    @Override
    public Result<List<ArtistVO>> getRandomArtists() {
        return null;
    }

    @Override
    public Result<ArtistDetailVO> getArtistDetail(Long artistId, HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<Long> getAllArtistsCount(Integer gender, String area) {
        return null;
    }

    @Override
    public Result addArtist(ArtistAddDTO artistAddDTO) {
        return null;
    }

    @Override
    public Result updateArtist(ArtistUpdateDTO artistUpdateDTO) {
        return null;
    }

    @Override
    public Result updateArtistAvatar(Long artistId, String avatar) {
        return null;
    }

    @Override
    public Result deleteArtist(Long ArtistId) {
        return null;
    }

    @Override
    public Result deleteArtists(List<Long> artistIds) {
        return null;
    }
}

