package org.amis.vibemusicserver.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author : KwokChichung
 * @description : 管理员视图的歌曲VO
 * @createDate : 2026/1/9 1:31
 */
@Data
public class SongAdminVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲 id
     */
    private Long songId;

    /**
     * 歌手名称
     */
    private String artistName;

    /**
     * 歌名
     */
    private String songName;

    /**
     * 专辑
     */
    private String album;

    /**
     * 歌词
     */
    private String lyric;

    /**
     * 歌曲时长
     */
    private String duration;

    /**
     * 歌曲风格
     */
    private String style;

    /**
     * 歌曲封面 url
     */
    private String coverUrl;

    /**
     * 歌曲 url
     */
    private String audioUrl;

    /**
     * 歌曲发行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

}