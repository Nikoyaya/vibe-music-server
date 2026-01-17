package org.amis.vibemusicserver.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/17 11:14
 */
@Data
public class ArtistNameVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    private Long artistId;

    /**
     * 歌手姓名
     */
    private String artistName;

}
