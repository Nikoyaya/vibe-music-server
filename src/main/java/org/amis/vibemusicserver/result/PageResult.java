package org.amis.vibemusicserver.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : KwokChichung
 * @description : 分页结果
 * @createDate : 2026/1/8 21:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页面数据列表
     */
    private List<T> items;
}

