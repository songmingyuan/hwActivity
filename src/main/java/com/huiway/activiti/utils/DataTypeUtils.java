package com.huiway.activiti.utils;

import java.util.HashSet;
import java.util.Set;

/**
   * 数据类型工具。
 */
public class DataTypeUtils {

    // 基本数据类型集合
    private static final Set<Class<?>> PRIMITIVE_TYPES = new HashSet<>();

    // 初始化基本数据类型集合
    static {
        PRIMITIVE_TYPES.add(Boolean.class);
        PRIMITIVE_TYPES.add(Character.class);
        PRIMITIVE_TYPES.add(Byte.class);
        PRIMITIVE_TYPES.add(Short.class);
        PRIMITIVE_TYPES.add(Integer.class);
        PRIMITIVE_TYPES.add(Long.class);
        PRIMITIVE_TYPES.add(Float.class);
        PRIMITIVE_TYPES.add(Double.class);
        PRIMITIVE_TYPES.add(String.class);
    }

    /**
     * 检查值是否为 Primitive 类型。
     * @param value 输入值
     * @return 是否为 Primitive 类型
     */
    public static boolean isPrimitive(Object value) {
        return value == null || PRIMITIVE_TYPES.contains(value.getClass());
    }

}
