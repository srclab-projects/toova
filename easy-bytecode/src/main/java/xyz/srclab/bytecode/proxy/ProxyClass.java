package xyz.srclab.bytecode.proxy;

import xyz.srclab.common.reflect.method.MethodBody;
import xyz.srclab.common.reflect.method.MethodDefinition;

public interface ProxyClass<T> {

    static Builder<Object> newBuilder() {
        return ProxyClassBuilderHelper.newBuilder(Object.class);
    }

    static <T> Builder<T> newBuilder(Class<T> superClass) {
        return ProxyClassBuilderHelper.newBuilder(superClass);
    }

    T newInstance();

    T newInstance(Class<?>[] parameterTypes, Object[] args);

    interface Builder<T> {

        Builder<T> addInterfaces(Iterable<Class<?>> interfaces);

        <R> Builder<T> overrideMethod(String name, Class<?>[] parameterTypes, MethodBody<R> methodBody);

        <R> Builder<T> overrideMethod(MethodDefinition<R> methodDefinition);

        ProxyClass<T> build();
    }
}
