package com.jinninghui.newspiral.common.entity.common.annotation;

public class AnnotationConstant {



    /**
     * 普通模式，校验权限
     */
    public static final String COMMON_STRATEGY = "haveNeededInterfaceAuth";
    /**
     * 父节点同意模式，只需父节点同意即可
     */
    public static final String FATHER_PEER_AGREE_STRATEGY = "fatherPeerAgree";


    public static final String SYSTEM_CONTRACT_STRATEGY = "systemContractVerifyStrategy";


    public static final String BUSINESS_CONTRACT_STRATEGY="businessContractStrategy";

    /**
     * 本地节点监控策略
     */
    public static final String PEER_MONITOR_STRATEGY="peerMonitorStrategy";

    public enum SystemClassPathEnum {
        common("com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.SystemSmartContract");

        private final String path;

        SystemClassPathEnum(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

    }

}
