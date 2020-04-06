package xyz.srclab.common.reflect.invoke;

import xyz.srclab.annotation.concurrent.ThreadSafe;
import xyz.srclab.common.provider.AbstractProviderManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@ThreadSafe
public class InvokerProviderManager extends AbstractProviderManager<InvokerProvider> {

    public static InvokerProviderManager getInstance() {
        return INSTANCE;
    }

    private static final InvokerProviderManager INSTANCE = new InvokerProviderManager();

    @Override
    protected InvokerProvider createDefaultProvider() {
        // MethodHandle is stupid.
        //return new MethodHandleInvokerProvider();
        return new ReflectedInvokerProvider();
    }

    private static final class MethodHandleInvokerProvider implements InvokerProvider {
        @Override
        public MethodInvoker newMethodInvoker(Method method) {
            return new MethodHandleMethodInvoker(method);
        }

        @Override
        public ConstructorInvoker newConstructorInvoker(Constructor<?> constructor) {
            return new MethodHandleConstructorInvoker(constructor);
        }
    }

    private static final class ReflectedInvokerProvider implements InvokerProvider {
        @Override
        public MethodInvoker newMethodInvoker(Method method) {
            return new ReflectedMethodInvoker(method);
        }

        @Override
        public ConstructorInvoker newConstructorInvoker(Constructor<?> constructor) {
            return new ReflectedConstructorInvoker(constructor);
        }
    }
}
