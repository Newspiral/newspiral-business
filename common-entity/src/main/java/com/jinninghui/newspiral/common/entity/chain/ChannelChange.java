package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;

import static com.jinninghui.newspiral.common.entity.common.persist.PersistConstant.PersistTarget.*;

/**
 * @version V1.0
 * @Title: ChannelChange
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/9/7 9:24
 */
@ApiModel(description = "通道变更")
@Slf4j
public class ChannelChange<T> {

    /**
     * 变化枚举
     */
    @ApiModelProperty(value = "通道变更动作")
    private PersistConstant.PersistTarget actionTag;
    /**
     * 通道中节点的变化
     */
    @ApiModelProperty(value = "变更对象")
    private T actionData;


    @ApiModelProperty(value = "变更对象类型")
    private String classType;


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChannelChange)) {
            return false;
        }
        ChannelChange newChannelChange = (ChannelChange) obj;
        return newChannelChange.actionTag.equals(actionTag)
                && newChannelChange.actionData.equals(actionData);
    }

    public PersistConstant.PersistTarget getActionTag() {
        return actionTag;
    }

    public void setActionTag(PersistConstant.PersistTarget actionTag) {
        this.actionTag = actionTag;
        if (actionTag == roleAdd || actionTag == roleModify || actionTag == roleRemove) {
            setClassType(Role.class.getName());
        } else if (actionTag == peerAdd || actionTag == peerModify || actionTag == peerRemove) {
            setClassType(Peer.class.getName());
        } else if (actionTag == memberAdd || actionTag == memberModify) {
            setClassType(Member.class.getName());
        } else if (actionTag == contractAdd) {
            setClassType(SmartContract.class.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public T getActionData() {
        //由于交易同步过来的actionData是jsonObject类型，则需要在get的时候转换一下
        if (actionData instanceof JSONObject) {
            try {
                actionData = (T) JSONObject.parseObject(JSON.toJSONString((JSONObject) actionData), Class.forName(classType));
            } catch (ClassNotFoundException e) {
                log.error("actionData:{} transfer to {} occured error", actionData, classType, e);
            }
        }
        return actionData;
    }

    public void setActionData(T actionData) {
        this.actionData = actionData;
    }

    public String getClassType() {
        return classType;
    }

    private void setClassType(String classType) {
        this.classType = classType;
    }

    /*public static void main(String[] args) {
        ChannelChange change = new ChannelChange<>();
        change.setActionTag(roleAdd);
        change.setClassType(Role.class.getName());
        change.setActionData(JSONObject.parseObject("{\n" +
                "    \"actionData\": {\n" +
                "        \"auths\": [],\n" +
                "        \"memberDelStrategy\": \"MANAGER_AGREE\",\n" +
                "        \"roleId\": \"635E6D7520099EE615E764380064683D0F26645231387FAAA3B61CFBE20EAF761\",\n" +
                "        \"name\": \"测试name2\",\n" +
                "        \"shortName\": \"testName1\",\n" +
                "        \"memberAddStrategy\": \"MANAGER_AGREE\",\n" +
                "        \"channelId\": \"C3B0C5727E1B425294039F99853BCDF6\",\n" +
                "        \"extendedData\": {}\n" +
                "    },\n" +
                "    \"actionTag\": \"roleModify\"\n" +
                "}"));
        Object o = change.getActionData();
        System.out.println(o);

    }*/
}
