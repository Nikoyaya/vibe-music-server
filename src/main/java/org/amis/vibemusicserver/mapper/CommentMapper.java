package org.amis.vibemusicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.amis.vibemusicserver.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/11 23:53
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

