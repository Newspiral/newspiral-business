package com.jinninghui.newspiral.common.entity.common.base;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;


/**
 * @version V1.0
 * @Title: test
 * @Package com.jinninghui.newspiral.gateway.base
 * @Description:
 * @author: xuxm
 * @date: 2019/12/6 15:40
 */
public class test {
    public static void main(String[] args)
    {


/*        PeerServiceUrls initPeerServiceUrls = new PeerServiceUrls();
        initPeerServiceUrls.setUrlOfType("192.168.20.125:12200",PeerServiceTypeEnum.FOR_SDK);
        initPeerServiceUrls.setUrlOfType("192.168.20.125:12200",PeerServiceTypeEnum.FOR_PEER);
        System.out.println(JSONObject.toJSON(initPeerServiceUrls).toString());
        PeerServiceUrls peerServiceUrls= JSONObject.parseObject(JSON.toJSONString(initPeerServiceUrls),PeerServiceUrls.class);
        System.out.println(JSON.toJSONString(peerServiceUrls));
        IdentityKey identityKey=new IdentityKey();
        identityKey.setType(IdentityTypeEnum.CHINA_PKI);
        identityKey.setValue("peerId1OfOrg1Test");
        System.out.println(JSONObject.toJSON(identityKey));

        BaseResponse baseResponse=new BaseResponse();
        baseResponse.setCode("100");
        baseResponse.setMessage("成功");
        List<BlockResp> list=new ArrayList<>();
        BlockResp blockResp=new BlockResp();
        blockResp.setChannelId("1234");
        list.add(blockResp);
        baseResponse.setData(list);
        System.out.println(JSONObject.toJSON(baseResponse));*/


        Block block=new Block();
        BlockHeader blockHeader=new BlockHeader();
        blockHeader.setChannelId("11111");
        block.setBlockHeader(blockHeader);
        System.out.println(block.toString());
        System.out.println(block.toString().getBytes().length);
    }
}
