package cn.qsky.aop;

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

import java.lang.reflect.Field;
import java.util.Date;
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
        //获取操作类型，crud
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (SqlCommandType.SELECT.equals(sqlCommandType)) {
            enhanceDecryptBySelect(invocation);
        }
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
            enhanceEncryptByInsertOrUpdate(invocation);
        }
        return invocation.proceed();
    }

    private void enhanceDecryptBySelect(Invocation invocation) throws Throwable {
        Object parameter = invocation.getArgs()[1];
        if (parameter instanceof String) {
            parameter = "10005";
        }
    }

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
    }

    @Override
    public Object plugin(Object target) {
        Object real = realTarget(target);
        return Plugin.wrap(real, this);
    }

    @SuppressWarnings("unchecked")
    public static <T> T realTarget(Object target) {
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
