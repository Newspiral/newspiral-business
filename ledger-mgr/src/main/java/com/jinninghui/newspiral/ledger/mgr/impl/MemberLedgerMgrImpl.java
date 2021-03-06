package com.jinninghui.newspiral.ledger.mgr.impl;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.member.Auth;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.MemberStateEnum;
import com.jinninghui.newspiral.common.entity.member.QueryMemberListReq;
import com.jinninghui.newspiral.common.entity.member.QueryRoleListParams;
import com.jinninghui.newspiral.common.entity.member.QueryRoleParams;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.member.RoleStateEnum;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.BlockChangesMap;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.AuthModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberRoleModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.RoleModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.AuthMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MemberMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MemberRoleMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @Title: MemberLedgerMgrImpl
 * @Package com.jinninghui.newspiral.ledger.mgr.impl
 * @Description:
 * @author: xuxm
 * @date: 2020/2/3 18:49
 */
@Slf4j
@Component
public class MemberLedgerMgrImpl implements MemberLedgerMgr {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberRoleMapper memberRoleMapper;
    @Autowired
    private AuthMapper authMapper;

    private final static String AUTHS_MAP_KEY = "AUTHS_MAP_KEY";

    /**
     * ?????????????????? key???channelId
     */
    private ConcurrentHashMap<String, List<Role>> rolesConcurrentHashMap = new ConcurrentHashMap<>();

    /*    */

    @Override
    public List<Integer> getAuthIds() {
        List<Integer> authIds = authMapper.selectAuthIds();
        if (CollectionUtils.isEmpty(authIds)) {
            return Collections.emptyList();
        }
        return authIds;
    }

    @Override
    public List<String> getAuthIdByInterfaceInfo(String methodName) {
        String authIds = authMapper.selectIdByInterfaceInfo(methodName);
        if (StringUtils.isEmpty(authIds)) {
            return Collections.emptyList();
        }
        return Arrays.asList(authIds.split(","));
    }

    @Override
    public List<String> getAuthIdByMemberInfo(String channelId, String publicKey) {
        String authIds = authMapper.selectIdByMemberInfo(channelId, publicKey);
        if (StringUtils.isEmpty(authIds)) {
            return Collections.emptyList();
        }
        return Arrays.asList(authIds.split(","));
    }

    /**
     /*    *//**
     * ?????????????????? key???channelId
     *//*
    private ConcurrentHashMap<String, List<Member>> membersConcurrentHashMap = new ConcurrentHashMap<>();

    */

    /**
     * ??????????????????????????????????????????
     */
    private ConcurrentHashMap<String, List<AuthModel>> authsHashMap = new ConcurrentHashMap<>();

    @Override
    public List<Auth> getAuthList() {
        return authMapper.selectAll().stream().filter(Objects::nonNull)
                .map(AuthModel::toAuth).collect(Collectors.toList());
    }


    /**
     * ???????????? key???memberId
     *//*
    private ConcurrentHashMap<String, Member> memberConcurrentHashMap = new ConcurrentHashMap<>();*/
    @Override
    public void insertCustomRole(Role role) {
        //??????????????????,??????????????????????????????
        List<RoleModel> form = roleMapper.selectRoleByChannelIdandParams(role.getChannelId(), role.getName(), role.getShortName());
        if (!CollectionUtils.isEmpty(form)) {
            for (RoleModel r : form) {
                if (r.getState() != 2) {
                    log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.insertCustomRole,?????????Role????????????????????????????????????,parameter:{}", role);
                    return;
                }
            }
        }
        RoleModel roleModel = RoleModel.createInstance(role);
        //??????insertCustomRole?????????
        roleModel.setCreateTime(new Date());
        roleModel.setUpdateTime(null);
        int nRow = roleMapper.insert(roleModel);
        if (nRow > 0) {
            rolesConcurrentHashMap.remove(role.getChannelId());
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.insertCustomRole,?????????Role??????????????????{}???,", nRow);
        }

    }

    @Override
    public void updateCustomRole(Role role) {
        //??????roleid???????????????????????????????????????
        RoleModel roleModel = roleMapper.selectByPrimaryKey(role.getRoleId());
        //?????????????????????????????????
        if (null==roleModel) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.updateCustomRole,?????????Role??????????????????????????????????????????/??????,parameter:{}", role);
            return;
        }
        //???role???????????????roleModel
        RoleModel updateRole = RoleModel.createInstance(role);
        //??????updateRole?????????
        updateRole.setCreateTime(null);
        updateRole.setUpdateTime(new Date());
        //?????????????????????????????????state??????2??????????????????
        int nRow = 0;
        if(updateRole.getState()!=null && updateRole.getState() == 2){
            //??????
            //???????????????????????????????????????
            memberRoleMapper.delCustomRoleRelation(roleModel.getRoleId());
            //???????????????????????????
            nRow =roleMapper.deleteByPrimaryKey(roleModel.getRoleId());

        }else{
            //??????
            nRow = roleMapper.updateByPrimaryKeySelective(updateRole);
        }
        if (nRow > 0) {
            if (role.getChannelId() != null) {
                rolesConcurrentHashMap.remove(role.getChannelId());
            }
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.updateCustomRole,?????????Role??????????????????{}???,", nRow);

        }
    }

    /**
     * ????????????id????????????????????????????????????
     * (???????????????????????????????????????)
     *
     * @param channelId
     * @return
     * @wuhuaijiang
     */
    @Override
    public List<Role> getRoleList(String channelId) {
        List<Role> roleList = rolesConcurrentHashMap.get(channelId);
        if (null != roleList) return roleList;
        roleList = new ArrayList<Role>();

        List<RoleModel> roleModelList = roleMapper.selectSystemRolesAndCustomRolesByChannelId(channelId);
        for (RoleModel roleModel : roleModelList) {
            roleList.add(roleModel.toRole());
        }
        rolesConcurrentHashMap.put(channelId, roleList);
        return roleList;
    }
//******************************************start************************************************************************************

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param queryRoleListParams
     * @return
     * @author wuhuaijiang
     */
    @Override
    public List<Role> getRoleList(QueryRoleListParams queryRoleListParams) {
        String channelId = queryRoleListParams.getChannelId();
        List<RoleStateEnum> stateEnums = queryRoleListParams.getState();
        if (stateEnums == null) {
            stateEnums = new ArrayList<>();
        }
        //???roleStateEnum???????????????Integer??????
        List<Integer> state = stateEnums.stream().map(RoleStateEnum -> {
            return RoleStateEnum.getCode();
        }).collect(Collectors.toList());
        List<RoleModel> roleModels = new ArrayList<>();
        //?????????????????????????????????null,????????????channelid??????????????????????????????????????????????????????
        if (null == state || state.size() == 0) {
            roleModels = roleMapper.selectSystemRolesAndCustomRolesByChannelId(channelId);
        } else {
            roleModels = roleMapper.selectRoleListByChannelIdAndState(channelId, state);
        }
        //3 ????????????
        if (null == roleModels) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getRoleList,????????????");
            return null;
        }
        List<Role> roleList = new ArrayList<>();
        for (RoleModel roleModel : roleModels) {
            Role newRole = roleModel.toRole();
            String authId = roleModel.getAuthId();
            newRole.setAuths(exchangeToList(authId));
            roleList.add(newRole);
        }
        return roleList;
    }

    private List<Auth> exchangeToList(String authId) {
        //??????authid
        if (StringUtils.isEmpty(authId)) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.exchangeToList,authId??????");
            return new ArrayList<>();
        }
        List<Auth> authList = new ArrayList<>();
        String[] auths = authId.split(",");
        for (String au : auths) {
            //??????auth???
            AuthModel auth = null;
            try {
                auth = authMapper.selectAuthById(Integer.valueOf(au));
            } catch (NumberFormatException e) {
                log.error(ModuleClassification.LedM_MLI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.exchangeToList,??????auth???????????????{}", e);
            }
            authList.add(auth.toAuth());
        }
        return authList;
    }

    /**
     * ??????????????????????????????
     *
     * @param queryRoleParams
     * @return
     */
    public Role getRole(QueryRoleParams queryRoleParams) {
        String channelId = queryRoleParams.getChannelId();
        String roleId = queryRoleParams.getRoleId();
        String name = queryRoleParams.getName();
        String shortName = queryRoleParams.getShortName();
        RoleModel roleModel = roleMapper.selectByChannelIdAndRoleParams(channelId, roleId, name, shortName);
        if (null == roleModel) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getRole???????????????");
            return null;
        }
        Role newRole = roleModel.toRole();
        String authId = roleModel.getAuthId();
        newRole.setAuths(exchangeToList(authId));
        return newRole;
    }
//***********************************************end********************************************************************************

    @Override
    public Role getRole(String roleId) {
        RoleModel roleModel = roleMapper.selectByPrimaryKey(roleId);
        if (null == roleModel) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getRole,?????????Role???????????????,parameter:{}", roleId);
            return null;
        }
        return roleModel.toRole();
    }

    @Override
    public List<Role> getRoleByRoleFlag(int flag) {
        List<RoleModel> roleModels = roleMapper.selectByRoleFlag(flag);
        List<Role> roles = new ArrayList<>();
        for (RoleModel model : roleModels) {
            roles.add(model.toRole());
        }
        return roles;
    }

    @Override
    public Member getMember(Member form) {

        if (StringUtils.isEmpty(form.getId())
                && (StringUtils.isEmpty(form.getChannelId()) || StringUtils.isEmpty(form.getPublicKey()))) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMember,????????????,parameter:{}", form);
            return null;
        }
        Member member;
        MemberModel memberModel;
        //memberId?????????????????????
        if (!StringUtils.isEmpty(form.getId())) {
            memberModel = memberMapper.selectByPrimaryKey(form.getId());
        } else {
            memberModel = memberMapper.selectByPublicKey(form.getChannelId(), form.getPublicKey());
        }
        if (null == memberModel) {
            member = findMemberInCache(form.getChannelId(), form.getPublicKey());
            if (null == member) {
                log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMember,?????????MEMBER???????????????,parameter:{}", form);
                return null;
            }
        } else {
            member = memberModel.toMember();
        }
        List<RoleModel> roleModels = roleMapper.selectByMemberId(member.getId());
        List<Role> roles = new ArrayList<>();
        for (RoleModel roleModel : roleModels) {
            Role role = roleModel.toRole();
            //TODO ???????????????????????????
            role.setAuths(getAuthsByRole(roleModel));
            roles.add(role);
        }
        member.setRoles(roles);
        return member;
    }

    private Member findMemberInCache(String channelId, String publicKey) {
        BlockChangesMap snapshootMap = LedgerThreadLocalContext.blockChangesMap;
        List<Channel> channels = snapshootMap.findInMap(PersistConstant.PersistTarget.memberAdd);
        for (Channel channel : channels) {
            Member memberInCache = (Member) channel.getChannelChange().getActionData();
            if (memberInCache.getPublicKey().equals(publicKey) && memberInCache.getChannelId().equals(channelId)) {
                return memberInCache;
            }
        }
        return null;
    }



    @Override
    //@Transactional
    public void insertMember(Member member) {
        //???????????????????????????????????????????????????
        MemberModel form = memberMapper.selectByPublicKey(member.getChannelId(), member.getPublicKey());
        if (form != null) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.insertMember,?????????MEMBER??????????????????,parameter:{}", member);
            return;
        }
        MemberModel memberModel = MemberModel.createInstance(member);
        memberModel.setCreateTime(new Date());
        int nRow = memberMapper.insert(memberModel);
        int mrTotal = 0;
        for (Role role : member.getRoles()) {
            MemberRoleModel memberRoleModel = MemberRoleModel.createInstance(memberModel.getId(), role.getRoleId(), member.getChannelId());
            int mrRow = memberRoleMapper.insert(memberRoleModel);
            if (mrRow > 0) {
                mrTotal++;
            }
        }
        log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.insertMember,?????????member??????????????????{}???,member_role??????????????????{}???", nRow, mrTotal);
    }

    @Override
    @Transactional
    public void updateMember(Member member) {
        //???????????????????????????????????????????????????
        MemberModel form = memberMapper.selectByPublicKey(member.getChannelId(), member.getPublicKey());
        if (form == null) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.memberUpdateInfo,?????????MEMBER???????????????,parameter:{}", member);
            return;
        }
        MemberModel memberModel = MemberModel.createInstance(member);
        memberModel.setUpdateTime(new Date());
        int nRow = memberMapper.updateByPrimaryKeySelective(memberModel);
        //?????????????????????
        memberRoleMapper.deleteByMemberId(form.getId());
        int insertCnt = 0;
        for (Role role : member.getRoles()) {
            MemberRoleModel memberRoleModel = MemberRoleModel.createInstance(form.getId(), role.getRoleId(), member.getChannelId());
            int mrRow = memberRoleMapper.insert(memberRoleModel);
            if (mrRow > 0) {
                insertCnt++;
            }
        }
        log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.memberUpdateInfo,?????????member??????????????????{}???,member_role??????????????????{}???", nRow, insertCnt);
    }

    @Override
    public List<Member> getMemberList(QueryMemberListReq form) {
        if (StringUtils.isEmpty(form.getChannelId())) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMemberList,????????????,parameter:{}", form);
            return null;
        }
        List<Member> members = new ArrayList<>();
        String statusString = "";
        if (null != form.getStateEnumList()) {
            for (MemberStateEnum state : form.getStateEnumList()) {
                statusString += state.getCode();
                statusString += ",";
            }
            statusString = statusString.substring(0, statusString.length() - 1);
        }
        List<MemberModel> memberModels = memberMapper.selectByChannelAndStatus(form.getChannelId(), statusString);
        if (CollectionUtils.isEmpty(memberModels)) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMemberList,?????????MEMBER???????????????,parameter:{}", form);
            return null;
        }

        for (MemberModel model : memberModels) {
            Member member = model.toMember();
            List<RoleModel> roleModels = roleMapper.selectByMemberId(member.getId());
            List<Role> roles = new ArrayList<>();
            for (RoleModel roleModel : roleModels) {

                Role role = roleModel.toRole();
                //TODO ???????????????????????????
                role.setAuths(getAuthsByRole(roleModel));
                roles.add(role);
            }
            member.setRoles(roles);
            members.add(member);
        }
        return members;
    }

    @Override
    public Member queryOrganziationMember(String channelId, String organizationId) {
        MemberModel memberModel = memberMapper.selectOrganziationMember(channelId, organizationId);
        if (null == memberModel) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getOrganziationMember,?????????MEMBER???????????????,parameter:{}", channelId);
            return null;
        }
        Member member = memberModel.toMember();
        return member;
    }

    @Override
    public void clearChannelCache(String channelId) {
        rolesConcurrentHashMap.remove(channelId);
    }

    @Override
    public Member getAuthorizedMember(Member form) {
        if (!StringUtils.isEmpty(form.getChannelId()) && !StringUtils.isEmpty(form.getPublicKey())) {
            return getMember(form);
        }
        if (StringUtils.isEmpty(form.getIssuerId())) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMember,????????????,parameter:{}", form);
            return null;
        }
        MemberModel memberModel = memberMapper.selectOrganziationMember("", form.getIssuerId());
        if (!StringUtils.isEmpty(form.getPublicKey()) && !form.getPublicKey().equals(memberModel.getPublicKey())) {
            log.info(ModuleClassification.LedM_MLI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",LedgerMgrImpl.getMember,????????????,parameter:{}", form);
            return null;
        }

        Member member = memberModel.toMember();
        List<Role> roles = new ArrayList<>();
        //?????????????????????????????????
        roles.add(getRole("DEE826D175964131B7362E2DD99BB058"));
        member.setRoles(roles);

        return member;
    }

    @Override
    public List<Member> getAllMemberList() {
        List<MemberModel> memberModels = memberMapper.selectAll();
        List<Member> members = new ArrayList<>();
        for (MemberModel memberModel : memberModels) {
            members.add(memberModel.toMember());
        }
        return members;
    }


    /**
     * role???????????????
     *
     * @param roleModel
     * @return
     */
    private List<Auth> getAuthsByRole(RoleModel roleModel) {

        List<Auth> auths = new ArrayList<>();
        //??????????????????
        List<AuthModel> authModels = authsHashMap.get(AUTHS_MAP_KEY);
        if (CollectionUtils.isEmpty(authModels)) {
            //??????????????????
            authModels = authMapper.selectAll();
            authsHashMap.put(AUTHS_MAP_KEY, authModels);
        }
        Map<String, AuthModel> stringAuthModelMap = new HashMap<>();
        for (AuthModel authModel : authModels) {
            stringAuthModelMap.put(authModel.getId().toString(), authModel);
        }
        if (!StringUtils.isEmpty(roleModel.getAuthId())) {
            String[] arrAuthIds = roleModel.getAuthId().split(",");
            for (String authId : arrAuthIds) {
                AuthModel authModel = stringAuthModelMap.get(authId);
                auths.add(authModel.toAuth());
            }
        }

        if (CollectionUtils.isEmpty(auths)) {
            return null;
        }
        return auths;
    }

    /**
     * @param channelId ??????id
     * @param publicKey ??????
     * @return member
     */
    public Member getMemberByKey(String channelId, String publicKey) {
        MemberModel memberModel = memberMapper.selectByPublicKey(channelId, publicKey);
        return memberModel == null ? null : memberModel.toMember();
    }

    public Member getMemberRoleByKey(String channelId, String publicKey) {
        MemberModel memberModel = memberMapper.selectByPublicKey(channelId, publicKey);
        if (memberModel == null) {
            return null;
        }
        List<RoleModel> roleModels = roleMapper.selectByMemberId(memberModel.getId());
        Member member = memberModel.toMember();
        if (member != null) {
            member.setRoles(roleModels.stream().map(RoleModel::toRole).collect(Collectors.toList()));
        }
        return member;
    }

    @Override
    public void delCustomRoleRelation(String roleId) {
        memberRoleMapper.delCustomRoleRelation(roleId);
    }

    @Override
    public List<Role> getRoleByChannelIdandParams(String channelId, String name, String shortName) {
        List<RoleModel> roleModel = roleMapper.selectRoleByChannelIdandParams(channelId, name, shortName);
        List<Role> roleList = new ArrayList<>();
        if (roleModel == null || roleModel.size() == 0) {
            return null;
        }
        for (RoleModel rModel : roleModel) {
            roleList.add(rModel.toRole());
        }
        return roleList;
    }

    /**
     * ??????AuthCode??????AuthId
     */
    @Override
    public String getAuthIdByAuthCode(String authCode) {
        String authId = authMapper.selectAuthIdByAuthCode(authCode);
        return authId;
    }
}
