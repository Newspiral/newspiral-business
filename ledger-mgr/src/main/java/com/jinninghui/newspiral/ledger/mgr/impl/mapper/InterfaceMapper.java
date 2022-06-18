package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceMapper {

    /**
     * 照接口所在类修改同一个类下所有需要限流接口的限流参数
     * @param interfaceModel
     */
    void updateClassLimitParam(InterfaceModel interfaceModel);

    /**
     * 批量修改限流参数
     * @param interfaceModels
     */
    void batchInsertForLimitParam(@Param("list") List<InterfaceModel> interfaceModels);

    /**
     * 查询需要限流的所有接口
     * @return
     */
    List<InterfaceModel> selectLimitParamMethods();

    /**
     * 根据类名查找该类下所有需要进行限流的方法
     * @param className
     * @return
     */
    List<InterfaceModel> selectLimitParamMethodByClassName(@Param("className") String className);

    /**
     * 根据类名和方法名查找令牌桶的初始化参数
     * @param className
     * @param methodName
     * @return
     */
    InterfaceModel selectLimitParamMethodByClassNameAndMethodName(@Param("className") String className, @Param("methodName") String methodName);
}
