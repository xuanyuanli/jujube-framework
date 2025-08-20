package cn.xuanyuanli.jdbc.spring;

import java.lang.reflect.Proxy;

import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.binding.DaoProxy;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author xuanyuanli
 */
public class DaoFactoryBean<T extends BaseDao<?,?>> implements FactoryBean<T> {

    private Class<T> daoInterfaceClass;

    @Override
    public T getObject() {
        DaoProxy<?> mapperProxy = new DaoProxy<>(daoInterfaceClass);
        T t = (T) Proxy.newProxyInstance(daoInterfaceClass.getClassLoader(), new Class[] {daoInterfaceClass}, mapperProxy);
        ProxyBeanContext.setCurrentProxy(daoInterfaceClass, t);
        return t;
    }

    @Override
    public Class<?> getObjectType() {
        return daoInterfaceClass;
    }

    @SuppressWarnings("unused")
    public void setDaoInterfaceClass(Class<T> daoInterfaceClass) {
        this.daoInterfaceClass = daoInterfaceClass;
    }
}
