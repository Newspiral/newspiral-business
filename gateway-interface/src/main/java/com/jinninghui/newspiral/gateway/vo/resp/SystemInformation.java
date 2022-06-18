package com.jinninghui.newspiral.gateway.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: SystemInfo
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2020/7/29 9:46
 */
@Data
public class SystemInformation {
    //cpu核信息
    @ApiModelProperty(value = "cpu核信息")
    private List<Map<String,String>> cpuCoreInfo;
    //cpu核数
    @ApiModelProperty(value = "cpu核数")
    private String cpuCount;
    //cpu系统使用率
    @ApiModelProperty(value = "cpu系统使用率")
    private String cpuSysUse;
    //cpu用户使用率
    @ApiModelProperty(value = "cpu用户使用率")
    private String cpuUserUse;
    //cpu当前等待率
    @ApiModelProperty(value = "cpu当前等待率")
    private String cpuIOwait;
    //cpu当前空闲率
    @ApiModelProperty(value = "cpu当前空闲率")
    private String cpuIdle;
    //内存总量
    @ApiModelProperty(value = "内存总量")
    private String totalMemory;
    //已使用量
    @ApiModelProperty(value = "已使用量")
    private String usedMemory;
    //磁盘总空间
    @ApiModelProperty(value = "磁盘总空间")
    private String totalSpace;
    //已使用空间
    @ApiModelProperty(value = "已使用空间")
    private String usedSpace;
}
