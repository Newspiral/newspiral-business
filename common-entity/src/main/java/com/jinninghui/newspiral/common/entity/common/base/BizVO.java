package com.jinninghui.newspiral.common.entity.common.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "分页查询结果返回实体")
@Data
public class BizVO<T> {
    //返回总记录数
    @ApiModelProperty("总记录数")
    private Integer recordCount;

    //页数
    @ApiModelProperty("页码")
    private Integer pageNo;

    //每页记录数
    @ApiModelProperty("每页记录数")
    private Integer pageSize;

    //总页数
    @ApiModelProperty("总页数")
    private Integer pageCount;
    //列表
    @ApiModelProperty("返回值结果列表")
    private List<T> list;
}
