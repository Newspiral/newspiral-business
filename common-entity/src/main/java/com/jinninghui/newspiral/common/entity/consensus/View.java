package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/18 14:38
 * HotStuaff中的View，一个View至多完成一个区块的共识
 */
@ApiModel(description = "视图")
@Data
public class View {
    /**
     * 初始化的LockedQC的View值，因为实际有效的View号从0开始，因此从未执行过的不存在的LockedQC的ViewNo为-1
     */
    @ApiModelProperty(value = "初始化的LockedQC的View值")
    public final static long INIT_LOCKED_QC_VIEW_NO = -1L;
    /**
     * 创建默认的View对象，适用于一个新节点创建一条新链
     * @return
     */
    public static View createDefaultView(long defaultViewTimeOutMs)
    {
        return View.createView(4L,defaultViewTimeOutMs);
    }

    public  static View createView(long viewNo, long defaultViewTimeOutMs)
    {
        View view = new View();
        view.setNo(viewNo);
        view.setStartTimestamp(System.currentTimeMillis());
        view.setExpiredTimestamp(view.getStartTimestamp()+defaultViewTimeOutMs);
        return view;
    }

    /**
     * 从0开始，64位Long型足够大，2的10次方是1000,60次方是10的18次方，足够大了。
     */
    @ApiModelProperty(value = "视图序号")
    private Long no;
    /**
     * 当前round的开始时刻点
     */
    @ApiModelProperty(value = "当前view的开始时间戳")
    private Long startTimestamp;

    /**
     * 当前round的超时时刻点，也即当前round最长持续到哪个时刻点
     */
    @ApiModelProperty(value = "当前view超时时间戳")
    private Long expiredTimestamp;

    /**
     * 本View是否已经超时
     * @return
     */
    public boolean expired()
    {
        return System.currentTimeMillis()>=expiredTimestamp;
    }


    /**
     * 是否是当前view的合法New-View消息
     * @param msg
     * @return
     */
    boolean isValidMsg(NewViewMsg msg)
    {
        throw new NewspiralException(NewSpiralErrorEnum.UN_IMPLEMENTED);
    }

    /**
     *
     * @param expiredMs: 该view的超时时间，单位毫秒
     */
    public void enterNewView(long expiredMs) {
        this.no+=1L;
        this.startTimestamp=System.currentTimeMillis();
        this.expiredTimestamp=this.startTimestamp+expiredMs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"no\":")
                .append(no);
        sb.append('}');
        return sb.toString();
    }
}
