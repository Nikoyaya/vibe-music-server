package org.amis.vibemusicserver.model.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description : 歌曲查询条件
 * @createDate : 2026/1/9 1:32
 */
@Data
public class SongDTO implements Serializable {

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
     * 歌曲名
     */
    private String songName;

    /**
     * 歌手
     */
    private String artistName;

    /**
     * 专辑
     */
    private String album;

}

