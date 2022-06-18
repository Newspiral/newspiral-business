package com.jinninghui.newspiral.common.entity.common.persist;

public class PersistConstant {


    public enum PersistTarget {

        /*节点动作*/
        peerAdd(ActionEnum.add), peerRemove(ActionEnum.remove), peerModify(ActionEnum.modify),
        /*角色动作*/
        roleAdd(ActionEnum.add), roleRemove(ActionEnum.remove), roleModify(ActionEnum.modify),
        /*成员动作,其中删除member是修改member的status状态*/
        memberAdd(ActionEnum.add), memberModify(ActionEnum.modify),
        /*合约动作*/
        contractAdd(ActionEnum.add),
        /*世界状态动作*/
        stateAdd(ActionEnum.add), stateRemove(ActionEnum.remove), stateModify(ActionEnum.modify), stateRest(ActionEnum.rest),
        /**通道动作*/
        channelBlockMaxSizeModify(ActionEnum.modify),
        /**节点冻结解冻*/
        peerFrozen(ActionEnum.add);

        public ActionEnum action;

        PersistTarget(ActionEnum actionEnum) {
        }

        public ActionEnum getAction() {
            return action;
        }
    }


    public enum ActionEnum {
        /*新增，删除，更新，剩下的*/
        add, remove, modify ,rest;
    }
}
