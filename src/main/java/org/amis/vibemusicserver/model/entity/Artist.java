package org.amis.vibemusicserver.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description : 歌手实体类
 * @createDate : 2026/1/3 22:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_artist")
public class Artist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long artistId;

    /**
     * 歌手姓名
     */
    @TableField("name")
    private String artistName;

    /**
     * 歌手头像 url
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 歌手简介
     */
    @TableField("description")
    private String description;

    /**
     * 性别: 0-女，1-男
     */
    @TableField("gender")
    private Integer gender;
}