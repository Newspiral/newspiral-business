package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.task.*;
import lombok.Data;

/**
 * @version V1.0
 * @Title: PersistTaskModel
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.domain
 * @Description:
 * @author: xuxm
 * @date: 2019/10/17 15:23
 */
@Data
public class PersistTaskModel {

    /**
     * 主键
     */
    private Long id;

    private String taskId;
    /**
     * 任务类型
     */
    private  String type ;
    /**
     * 当前时间
     */
    private Long createTime = 0L;
    /**
     * 执行截止时间，这个截止时间是指：在这个截止时间前，需要按照频率定期尝试，
     * 截止时间之后，如果尝试过一次及以上，仍然未能变为成功，则将任务状态设置为失败；
     * 注意将任务状态设置为失败，一定要保证至少尝试过一次
     */
    private Long executeEndTime = 0L;

    /**
     * 参数列表JSON格式字符串
     */
    private String paramsStr;

    /**
     * 任务状态
     */
    private String status ;


    public static PersistTaskModel createInstance(Task task)
    {
        PersistTaskModel persistTaskModel=new PersistTaskModel();
        persistTaskModel.setTaskId(task.getTaskId());
        persistTaskModel.setCreateTime(task.getCreateTime());
        persistTaskModel.setExecuteEndTime(task.getExecuteEndTime());
        persistTaskModel.setParamsStr(JSON.toJSONString(task.getParamsList()));
        persistTaskModel.setStatus(task.getStatus().getCode());
        persistTaskModel.setType(task.getType().getCode());
        return  persistTaskModel;
    }

    public Task toTask()
    {
        Task task=new Task();
        task.setTaskId(this.taskId);
        task.setExecuteEndTime(this.executeEndTime);
        task.setCreateTime(this.createTime);
        //ArrayList<String> paramList = new ArrayList<>();
        //JSONArray jsonArray = JSONArray.parseArray(this.paramsStr);
        //JSONObject jsonObject=jsonArray.getJSONObject(0);
        //paramList.add(JSON.toJavaObject(jsonObject,String.class));
        task.setParamsList(JSON.parseArray(this.paramsStr,String.class));
        task.setStatus(EnumUtil.getByCode(this.status,TaskStatus.class));
        task.setType(EnumUtil.getByCode(this.type,TaskTypeEnum.class));
        return task;
    }


}
