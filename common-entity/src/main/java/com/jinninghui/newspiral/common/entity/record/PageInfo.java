package com.jinninghui.newspiral.common.entity.record;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页实体
 * @param <T>
 */
@Data
public class PageInfo<T> {

    /**
     * 总数，符合条件的记录总数
     */
    @ApiModelProperty("总数，符合条件的记录总数")
    private Long totalCount;

    /**
     * 总页数
     */
    @ApiModelProperty("总页数")
    private Long totalPage;

    /**
     * 当前页序号：从1开始
     */
    @ApiModelProperty("当前页序号：从1开始")
    private Long curPage;

    /**
     * 页大小
     */
    @ApiModelProperty("页大小")
    private Integer pageSize;

    /**
     * 当前页的所有调用记录，每条记录包括记录需求的所有属性
     */
    @ApiModelProperty("当前页的所有调用记录，每条记录包括记录需求的所有属性")
    private List<T> record;
}
