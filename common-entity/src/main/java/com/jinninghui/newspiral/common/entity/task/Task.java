package com.jinninghui.newspiral.common.entity.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lida
 * @date 2019/9/10 20:22
 * 需要持久化的任务
 */
@ToString
public class Task {

    @Getter @Setter
    String taskId ;

    @Getter @Setter
    TaskTypeEnum type = TaskTypeEnum.DEFAULT_TYPE;

    @Getter @Setter
    Long createTime = 0L;
    /**
     * 执行截止时间，这个截止时间是指：在这个截止时间前，需要按照频率定期尝试，
     * 截止时间之后，如果尝试过一次及以上，仍然未能变为成功，则将任务状态设置为失败；
     * 注意将任务状态设置为失败，一定要保证至少尝试过一次
     */
    @Getter @Setter
    Long executeEndTime = 0L;

    /**
     * 参数列表
     */
    @Getter @Setter
    List<String> paramsList = new ArrayList<>();

    /**
     * 任务状态
     */
    @Getter @Setter
    TaskStatus status = TaskStatus.WAIT_EXEXUTE;



}
