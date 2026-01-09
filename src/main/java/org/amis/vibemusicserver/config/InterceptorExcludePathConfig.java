package org.amis.vibemusicserver.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @author : KwokChichung
 * @description : 拦截器排除路径配置类
 * @createDate : 2026/1/8
 */
@Configuration
public class InterceptorExcludePathConfig {

    /**
     * 获取所有不需要拦截的路径列表
     */
    public static List<String> getExcludePaths() {
        return Arrays.asList(
                // 管理员相关
                "/admin/login", "/admin/logout", "/admin/register",

                // 用户认证相关
                "/user/login", "/user/logout", "/user/register",
                "/user/sendVerificationCode", "/user/resetUserPassword",

                //token
                "/token/refresh",

                // 公共内容相关
                "/banner/getBannerList",

                // 歌单相关
                "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists",
                "/playlist/getPlaylistDetail/**",

                // 艺术家相关
                "/artist/getAllArtists", "/artist/getArtistDetail/**",

                // 歌曲相关
                "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**"
        );
    }

    /**
     * 获取管理员相关排除路径
     */
    public static List<String> getAdminExcludePaths() {
        return Arrays.asList(
                "/admin/login", "/admin/logout", "/admin/register"
        );
    }

    /**
     * 获取用户相关排除路径
     */
    public static List<String> getUserExcludePaths() {
        return Arrays.asList(
                "/user/login", "/user/logout", "/user/register",
                "/user/sendVerificationCode", "/user/resetUserPassword"
        );
    }

    /**
     * 获取公共内容排除路径
     */
    public static List<String> getPublicContentExcludePaths() {
        return Arrays.asList(
                "/banner/getBannerList",
                "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists",
                "/playlist/getPlaylistDetail/**",
                "/artist/getAllArtists", "/artist/getArtistDetail/**",
                "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**"
        );
    }
}