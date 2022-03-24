package cn.qsky.aop;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        if (StringUtils.isEmpty(mappedStatement.getId()) || !mappedStatement.getId().contains("UserMapper")) return invocation.proceed();
        System.out.println("Intercept method: " + mappedStatement.getId());
        //获取操作类型，crud
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (SqlCommandType.SELECT.equals(sqlCommandType)) return enhanceDecryptBySelect(invocation.proceed());
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType))
            enhanceEncryptByInsertOrUpdate(invocation);
        return invocation.proceed();
    }

//    private void enhanceDecryptBySelect(Invocation invocation) throws Throwable {
//        Object parameter = invocation.getArgs()[1];
//        if (parameter instanceof Integer)
//            parameter = 10001;
//        invocation.getArgs()[1] = parameter;
//    }

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

/*
    private void enhanceEncryptByInsertOrUpdate(Invocation invocation) throws Throwable {
        Object parameter = invocation.getArgs()[1];
        Field[] fields = parameter.getClass().getDeclaredFields();
        for (Field field : fields) {
            //注入对应的属性值
            if (field.getName().equals("createTime")) {
                field.setAccessible(true);
                field.set(parameter, new Date());
            }
            if (field.getName().equals("updateTime")) {
                field.setAccessible(true);
                field.set(parameter, new Date());
            }
        }
        invocation.getArgs()[1] = parameter;
    }
 */

    private void enhanceEncryptByInsertOrUpdate(Invocation invocation) {
        Object parameter = invocation.getArgs()[1];
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if (parameter instanceof String) {
            if (isEncryptStr(mappedStatement)) {
                parameter = "";//todo encrypt
            }
        } else if (parameter instanceof Map) {
            return;
        } else {
            EnDecryptPojoUtils.encrypt(parameter);
        }
        invocation.getArgs()[1] = parameter;
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
     * 获取mapper层接口方法
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

    @Override
    public Object plugin(Object target) {
        Object real = realTarget(target);
        return Plugin.wrap(real, this);
    }

    @SuppressWarnings("unchecked")
    private static <T> T realTarget(Object target) {
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
