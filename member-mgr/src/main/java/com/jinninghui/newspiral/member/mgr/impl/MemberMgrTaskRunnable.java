package com.jinninghui.newspiral.member.mgr.impl;


import com.jinninghui.newspiral.common.entity.task.Task;
import com.jinninghui.newspiral.common.entity.task.TaskStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lida
 * @date 2019/9/10 20:38
 * 成员管理相关的后台异步任务执行器
 * 执行例如 本节点加入某个通道的执行结果查询等异步任务
 */
@Slf4j
public class MemberMgrTaskRunnable implements Runnable {
    private MemberMgrImpl memberMgr;
    private  ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();

    public MemberMgrTaskRunnable(MemberMgrImpl memberMgr) {
        this.memberMgr = memberMgr;
        // 启动的时候从数据库加载所有某个类型的某个状态的记录并加入待执行任务列表中
        List<Task> tasks=memberMgr.getTaskListByStatus();
        for (Task task:tasks) {
            addTask(task);
        }
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
        while (true) {
            Task task = taskQueue.poll();
            if (task != null) {
                log.info("task.start.in");
                TaskStatus oldStatus = task.getStatus();
                //log.info("task.start.executeTask befor:{}",task);
                //执行任务，将其他通道的消息写入到本地节点
                executeTask(task);
                //log.info("task.start.executeTask after:{}",task);
                if(System.currentTimeMillis()>task.getExecuteEndTime())
                {
                    //超时
                    task.setStatus(TaskStatus.OVERTIME);
                }
                if(oldStatus!=task.getStatus()) {//状态发生变化才更新，减少持久化压力
                    memberMgr.updateTask(task);
                }
                if(task.getStatus().isFianlStatus()==false)
                {//不是终态，加入队列继续执行
                    taskQueue.add(task);
                }
                //TODO 这里的sleep数值也是随便写的，最好应该用wait/notify机制来更新
                sleep(2000L);
            } else {
                //TODO 这里的sleep数值也是随便写的，最好应该用wait/notify机制来更新
                sleep(2000L);
            }
        }
    }

    /**
     * 执行任务，执行结果会更新入参task的状态字段
     * @param task
     */
    private void executeTask(Task task) {
        switch (task.getType()) {
            case LOCAL_PEER_ADD_TO_CHANNEL_RESULT_QUERY:
                memberMgr.executeAddChannelResultTask(task);
                break;
            default:
                break;
        }
    }



    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
