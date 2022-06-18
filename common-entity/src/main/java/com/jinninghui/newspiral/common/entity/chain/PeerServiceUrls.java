package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 10:23
 */
@ApiModel(description = "节点服务urls")
@Data
public class PeerServiceUrls implements Serializable {


    /**
     * key为服务类型
     */
    @ApiModelProperty(value = "服务url集合，key为服务类型")
    @Valid
    @NotEmpty
   final private Map<String,PeerServiceUrl> serviceUrlMap = new HashMap<>();

   public String getUrlByType(PeerServiceTypeEnum peerServiceType)
   {
       return serviceUrlMap.get(peerServiceType.code).getUrl();
   }

    public void setUrlOfType(String url, String forPeer) {
        PeerServiceUrl serviceUrl = new PeerServiceUrl();
        serviceUrl.setType(forPeer);
        serviceUrl.setUrl(url);
        serviceUrlMap.put(forPeer,serviceUrl);
    }

    public PeerServiceUrls clone()
    {
        PeerServiceUrls newBean = new PeerServiceUrls();
        for(String key: serviceUrlMap.keySet()) {
            newBean.setUrlOfType(serviceUrlMap.get(key).getUrl(),serviceUrlMap.get(key).getType());
        }
        return newBean;
    }



}
