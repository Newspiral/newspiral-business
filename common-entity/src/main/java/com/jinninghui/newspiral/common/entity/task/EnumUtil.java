package com.jinninghui.newspiral.common.entity.task;

/**
 * @version V1.0
 * @Title: EnumUtil
 * @Package com.jinninghui.newspiral.common.entity.task
 * @Description:
 * @author: xuxm
 * @date: 2019/12/16 17:33
 */
public class EnumUtil {
    public static  <T extends CodeEnum> T getByCode(String code, Class<T> enumClass) {
        //通过反射取出Enum所有常量的属性值
        for (T each: enumClass.getEnumConstants()) {
            //利用code进行循环比较，获取对应的枚举
            if (code.equals(each.getCode())) {
                return each;
            }
        }
        return null;
    }
}
