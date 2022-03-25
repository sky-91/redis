package cn.qsky.aop;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Component
public class MapperInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if (!isEnDecryptMapper(mappedStatement) || !isEnDecryptMapperMethod(mappedStatement)) return invocation.proceed();
        System.out.println("Intercept method: " + mappedStatement.getId());
        //获取操作类型，crud
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (SqlCommandType.SELECT.equals(sqlCommandType)) return enhanceDecryptBySelect(invocation.proceed());
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType))
            enhanceEncryptByInsertOrUpdate(invocation);
        return invocation.proceed();
    }

    /**
     * 判断是否为需要加解密的mapper（@EnDecryptMapperAnnotation）
     */
    private boolean isEnDecryptMapper(MappedStatement mappedStatement) {
        try {
            String namespace = mappedStatement.getId();
            String className = namespace.substring(0, namespace.lastIndexOf("."));
            Class<?> clazz = Class.forName(className);
            Annotation annotation = clazz.getAnnotation(EnDecryptMapperAnnotation.class);
            if (null != annotation) return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * 判断是否为需要加解密的mapper，且方法上有@EnDecryptMapperMethod
     * 不建议方法重载
     */
    private boolean isEnDecryptMapperMethod(MappedStatement mappedStatement) {
        Method method = getMapperTargetMethod(mappedStatement);
        assert method != null;
        Annotation annotation = method.getAnnotation(EnDecryptMapperMethod.class);
        return null != annotation;
    }

    private Object enhanceDecryptBySelect(Object returnValue) {
        if (returnValue != null) {
            if (returnValue instanceof ArrayList<?>) {
                List<?> oriList = (ArrayList<?>) returnValue;
                List<Object> newList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(oriList)) {
                    for (Object object : oriList) {
                        EnDecryptPojoUtils.decrypt(object);
                        newList.add(object);
                    }
                    returnValue = newList;
                }
            } else if (returnValue instanceof Map) {
                return returnValue;
            } else {
                EnDecryptPojoUtils.decrypt(returnValue);
            }
        }
        return returnValue;
    }

    private void enhanceEncryptByInsertOrUpdate(Invocation invocation) {
        Object parameter = invocation.getArgs()[1];
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if (parameter instanceof String) {
            if (isEncryptStr(mappedStatement)) {
                System.out.println("" + parameter);
                //非对象参数，本次加解密不涉及
            }
        } else if (parameter instanceof Map) {
            Parameter[] params = getParams(mappedStatement);
            if (null != params && params.length == 1) {
                //只处理一个List参数的方法
                String paramName = params[0].getName();
                try {
                    Map<String, Object> oriMap = convertObjectToMap(parameter);
                    if (oriMap.get(paramName) instanceof ArrayList) {
                        List<?> oriList = (ArrayList<?>) oriMap.get(paramName);
                        List<Object> newList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(oriList)) {
                            for (Object object : oriList) {
                                EnDecryptPojoUtils.encrypt(object);
                                newList.add(object);
                            }
                            oriMap.put(paramName, newList);
                            parameter = oriMap;
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            EnDecryptPojoUtils.encrypt(parameter);
        }
        invocation.getArgs()[1] = parameter;
    }

    /**
     * 获取方法的所有参数
     */
    private Parameter[] getParams(MappedStatement statement) {
        Method method = getMapperTargetMethod(statement);
        assert method != null;
        return method.getParameters();
    }

    /**
     * 判断字符串是否需要加密
     */
    private boolean isEncryptStr(MappedStatement mappedStatement) {
        boolean result = false;
        try {
            Method method = getMapperTargetMethod(mappedStatement);
            assert method != null;
            method.setAccessible(true);
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if (parameterAnnotations.length > 0) {
                for (Annotation[] parameterAnnotation : parameterAnnotations) {
                    for (Annotation annotation : parameterAnnotation) {
                        if (annotation instanceof EncryptField) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 获取mapper层接口的方法
     */
    private Method getMapperTargetMethod(MappedStatement mappedStatement) {
        Method method = null;
        try {
            String namespace = mappedStatement.getId();
            String className = namespace.substring(0, namespace.lastIndexOf("."));
            String methodName = namespace.substring(namespace.lastIndexOf(".") + 1);
            Method[] ms = Class.forName(className).getMethods();
            for (Method m : ms) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
        } catch (SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return method;
    }

    /**
     * object 转 map
     */
    public Map<String, Object> convertObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();
        System.out.println(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            if (value == null) {
                value = "";
            }
            map.put(fieldName, value);
        }
        return map;
    }

    @Override
    public Object plugin(Object target) {
        Object real = realTarget(target);
        return Plugin.wrap(real, this);
    }

    @SuppressWarnings("unchecked")
    private <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
