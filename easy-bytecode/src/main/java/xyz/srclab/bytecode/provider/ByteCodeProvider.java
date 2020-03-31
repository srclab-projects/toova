package xyz.srclab.bytecode.provider;

import xyz.srclab.bytecode.bean.BeanClass;
import xyz.srclab.bytecode.proxy.ProxyClass;

public interface ByteCodeProvider {

    <T> BeanClass.Builder<T> newBeanClassBuilder(Class<T> superClass);

    <T> ProxyClass.Builder<T> newProxyClassBuilder(Class<T> superClass);
}
