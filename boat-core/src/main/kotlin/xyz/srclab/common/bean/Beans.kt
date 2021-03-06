@file:JvmName("Beans")
@file:JvmMultifileClass

package xyz.srclab.common.bean

import xyz.srclab.common.convert.Converter
import java.lang.reflect.Type

private val defaultResolver = BeanResolver.DEFAULT

fun Type.resolve(): BeanType {
    return defaultResolver.resolve(this)
}

fun Any.asMap(): MutableMap<String, Any?> {
    return defaultResolver.asMap(this)
}

fun Any.asMap(copyOptions: BeanResolver.CopyOptions): MutableMap<String, Any?> {
    return defaultResolver.asMap(this, copyOptions)
}

fun <T : Any> Any.copyProperties(to: T): T {
    return defaultResolver.copyProperties(this, to)
}

fun <T : Any> Any.copyProperties(to: T, converter: Converter): T {
    return defaultResolver.copyProperties(this, to, converter)
}

fun <T : Any> Any.copyProperties(to: T, fromType: Type, toType: Type, converter: Converter): T {
    return defaultResolver.copyProperties(this, to, fromType, toType, converter)
}

fun <T : Any> Any.copyPropertiesIgnoreNull(to: T): T {
    return defaultResolver.copyPropertiesIgnoreNull(this, to)
}

fun <T : Any> Any.copyPropertiesIgnoreNull(to: T, converter: Converter): T {
    return defaultResolver.copyPropertiesIgnoreNull(this, to, converter)
}

fun <T : Any> Any.copyPropertiesIgnoreNull(to: T, fromType: Type, toType: Type, converter: Converter): T {
    return defaultResolver.copyPropertiesIgnoreNull(this, to, fromType, toType, converter)
}

fun <T : Any> Any.copyProperties(to: T, copyOptions: BeanResolver.CopyOptions): T {
    return defaultResolver.copyProperties(this, to, copyOptions)
}