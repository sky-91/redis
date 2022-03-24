package cn.qsky.aop;


import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public class EnDecryptPojoUtils {
    /**
     * 对象t注解字段加密
     */
    public static <T> void encrypt(T t) {
        if (isEncryptAndDecrypt(t)) {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            try {
                if (declaredFields.length > 0) {
                    for (Field field : declaredFields) {
                        if (field.isAnnotationPresent(EncryptField.class) && field.getType().toString().endsWith("String")) {
                            field.setAccessible(true);
                            String fieldValue = (String) field.get(t);
                            if (StringUtils.isNotEmpty(fieldValue)) {
                                field.set(t, "");//todo;
                            }
                            field.setAccessible(false);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 对含注解字段解密
     */
    public static <T> void decrypt(T t) {
        if (isEncryptAndDecrypt(t)) {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            try {
                if (declaredFields.length > 0) {
                    for (Field field : declaredFields) {
                        if (field.isAnnotationPresent(DecryptField.class) && field.getType().toString().endsWith("String")) {
                            field.setAccessible(true);
                            String fieldValue = (String) field.get(t);
                            if (StringUtils.isNotEmpty(fieldValue)) {
                                field.set(t, "***" + fieldValue);//todo decrypt
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 判断是否需要加密解密的类
     */
    public static <T> Boolean isEncryptAndDecrypt(T t) {
        boolean reslut = false;
        if (t != null) {
            Object object = t.getClass().getAnnotation(EnDecryptMapperType.class);
            if (object != null) {
                reslut = true;
            }
        }
        return reslut;
    }
}
