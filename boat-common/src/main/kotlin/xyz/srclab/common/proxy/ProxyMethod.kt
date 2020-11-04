package xyz.srclab.common.proxy

import xyz.srclab.kotlin.compile.COMPILE_INAPPLICABLE_JVM_NAME
import java.lang.reflect.Method

/**
 * @author sunqian
 */
interface ProxyMethod<T> {

    @Suppress(COMPILE_INAPPLICABLE_JVM_NAME)
    val name: String
        @JvmName("name") get

    @Suppress(COMPILE_INAPPLICABLE_JVM_NAME)
    val parametersTypes: Array<Class<*>>?
        @JvmName("parametersTypes") get

    fun invoke(proxy: T, method: Method, args: Array<out Any?>?, superInvoker: SuperInvoker): Any?
}

interface SuperInvoker {

    fun invoke(args: Array<out Any?>?): Any?
}