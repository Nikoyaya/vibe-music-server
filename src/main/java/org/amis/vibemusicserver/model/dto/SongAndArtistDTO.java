package org.amis.vibemusicserver.model.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/9 1:32
 */
@Data
public class SongAndArtistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull
    private Integer pageSize;

    /**
     * 歌手
     */
    private Long artistId;

    /**
     * 歌曲名
     */
    private String songName;

    /**
     * 专辑
     */
    private String album;

}

