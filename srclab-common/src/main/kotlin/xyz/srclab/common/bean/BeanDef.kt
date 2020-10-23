package xyz.srclab.common.bean

import xyz.srclab.common.base.INAPPLICABLE_JVM_NAME
import xyz.srclab.common.base.VirtualInvoker
import xyz.srclab.common.base.asAny
import xyz.srclab.common.base.virtualInvoker
import xyz.srclab.common.collection.MapSchema
import xyz.srclab.common.collection.resolveMapSchema
import xyz.srclab.common.convert.Converter
import xyz.srclab.common.reflect.TypeRef
import xyz.srclab.common.reflect.findField
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * @author sunqian
 */
interface BeanDef {

    @Suppress(INAPPLICABLE_JVM_NAME)
    val type: Class<*>
        @JvmName("type") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val properties: Map<String, PropertyDef>
        @JvmName("properties") get

    fun getProperty(name: String): PropertyDef? {
        return properties[name]
    }

    interface CopyOptions {

        fun filterSourcePropertyName(name: Any?): Boolean

        fun filterSourceProperty(name: Any?, value: Any?): Boolean

        fun convertPropertyName(name: Any?, value: Any?, targetNameType: Type): Any?

        fun convertPropertyValue(name: Any?, value: Any?, targetValueType: Type): Any?

        companion object {

            @JvmField
            val DEFAULT = object : CopyOptions {

                override fun filterSourcePropertyName(name: Any?): Boolean {
                    return true
                }

                override fun filterSourceProperty(name: Any?, value: Any?): Boolean {
                    return true
                }

                override fun convertPropertyName(name: Any?, value: Any?, targetNameType: Type): Any? {
                    return name
                }

                override fun convertPropertyValue(name: Any?, value: Any?, targetValueType: Type): Any? {
                    return value
                }
            }

            @JvmField
            val IGNORE_NULL = object : CopyOptions {

                override fun filterSourcePropertyName(name: Any?): Boolean {
                    return true
                }

                override fun filterSourceProperty(name: Any?, value: Any?): Boolean {
                    return value !== null
                }

                override fun convertPropertyName(name: Any?, value: Any?, targetNameType: Type): Any? {
                    return name
                }

                override fun convertPropertyValue(name: Any?, value: Any?, targetValueType: Type): Any? {
                    return value
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun resolve(type: Class<*>): BeanDef {
            return BeanDefImpl(type)
        }

        @JvmStatic
        fun toMap(bean: Any): Map<String, Any?> {
            val def = resolve(bean.javaClass)
            val result = mutableMapOf<String, Any?>()
            for (entry in def.properties) {
                val property = entry.value
                if (!property.isReadable) {
                    continue
                }
                result[property.name] = property.getValue(bean)
            }
            return result.toMap()
        }

        @JvmStatic
        fun <K, V> toMap(bean: Any, type: Type): Map<K, V> {
            return toMap(bean, type.resolveMapSchema(), CopyOptions.DEFAULT)
        }

        @JvmStatic
        fun <K, V> toMap(bean: Any, typeRef: TypeRef<Map<K, V>>): Map<K, V> {
            return toMap(bean, typeRef.type.resolveMapSchema(), CopyOptions.DEFAULT)
        }

        @JvmStatic
        fun <K, V> toMap(bean: Any, mapSchema: MapSchema, copyOptions: CopyOptions): Map<K, V> {
            val def = resolve(bean.javaClass)
            val result = mutableMapOf<K, V>()
            for (entry in def.properties) {
                val property = entry.value
                if (!property.isReadable) {
                    continue
                }
                val name = property.name
                if (!copyOptions.filterSourcePropertyName(name)) {
                    continue
                }
                val value = property.getValue<Any?>(bean)
                if (!copyOptions.filterSourceProperty(name, value)) {
                    continue
                }
                val mapKey = copyOptions.convertPropertyName(name, value, mapSchema.keyType).asAny<K>()
                val mapValue = copyOptions.convertPropertyValue(name, value, mapSchema.valueType).asAny<V>()
                result[mapKey] = mapValue
            }
            return result.toMap()
        }

        @JvmStatic
        @JvmOverloads
        fun copyProperties(from: Any, to: Any, ignoreNull: Boolean = false) {
            return if (ignoreNull) {
                copyProperties(from, to, CopyOptions.IGNORE_NULL)
            } else {
                copyProperties(from, to, CopyOptions.DEFAULT)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun copyProperties(from: Any, to: MutableMap<String, Any?>, ignoreNull: Boolean = false) {
            return if (ignoreNull) {
                copyProperties(from, to, MapSchema.BEAN_PATTERN, CopyOptions.IGNORE_NULL)
            } else {
                copyProperties(from, to, MapSchema.BEAN_PATTERN, CopyOptions.DEFAULT)
            }
        }

        @JvmStatic
        fun copyProperties(from: Any, to: Any, copyOptions: CopyOptions) {
            when (from) {
                is Map<*, *> -> {
                    val fromDef = from.asAny<Map<Any?, Any?>>()
                    val toDef = resolve(to.javaClass)
                    for (propertyEntry in fromDef) {
                        val name = propertyEntry.key.toString()
                        val value = propertyEntry.value
                        if (!copyOptions.filterSourcePropertyName(name)
                            || !copyOptions.filterSourceProperty(name, value)
                        ) {
                            continue
                        }
                        val toProperty = toDef.getProperty(name)
                        if (toProperty === null || !toProperty.isWriteable) {
                            continue
                        }
                        toProperty.setValue(
                            to,
                            copyOptions.convertPropertyValue(name, value, toProperty.genericType)
                        )
                    }
                }
                !is Map<*, *> -> {
                    val fromDef = resolve(from.javaClass)
                    val toDef = resolve(to.javaClass)
                    for (propertyEntry in fromDef.properties) {
                        val fromProperty = propertyEntry.value
                        if (!fromProperty.isReadable) {
                            continue
                        }
                        val name = fromProperty.name
                        if (!copyOptions.filterSourcePropertyName(name)) {
                            continue
                        }
                        val toProperty = toDef.getProperty(name)
                        if (toProperty === null || !toProperty.isWriteable) {
                            continue
                        }
                        val value = fromProperty.getValue<Any?>(from)
                        if (!copyOptions.filterSourceProperty(name, value)) {
                            continue
                        }
                        toProperty.setValue(
                            to,
                            copyOptions.convertPropertyValue(name, value, toProperty.genericType)
                        )
                    }
                }
            }
        }

        @JvmStatic
        fun <K, V> copyProperties(from: Any, to: MutableMap<K, V>, toSchema: MapSchema, copyOptions: CopyOptions) {
            when (from) {
                is Map<*, *> -> {
                    val fromDef = from.asAny<Map<Any?, Any?>>()
                    for (propertyEntry in fromDef) {
                        val name = propertyEntry.key.toString()
                        val value = propertyEntry.value
                        if (!copyOptions.filterSourcePropertyName(name)
                            || !copyOptions.filterSourceProperty(name, value)
                        ) {
                            continue
                        }
                        val mapKey = copyOptions.convertPropertyName(name, value, toSchema.keyType).asAny<K>()
                        if (!to.containsKey(mapKey)) {
                            continue
                        }
                        val mapValue = copyOptions.convertPropertyValue(name, value, toSchema.valueType).asAny<V>()
                        to[mapKey] = mapValue
                    }
                }
                !is Map<*, *> -> {
                    val fromDef = resolve(from.javaClass)
                    for (propertyEntry in fromDef.properties) {
                        val fromProperty = propertyEntry.value
                        if (!fromProperty.isReadable) {
                            continue
                        }
                        val name = fromProperty.name
                        if (!copyOptions.filterSourcePropertyName(name)) {
                            continue
                        }
                        val value = fromProperty.getValue<Any?>(from)
                        if (!copyOptions.filterSourceProperty(name, value)) {
                            continue
                        }
                        val mapKey = copyOptions.convertPropertyName(name, value, toSchema.keyType).asAny<K>()
                        if (!to.containsKey(mapKey)) {
                            continue
                        }
                        val mapValue = copyOptions.convertPropertyValue(name, value, toSchema.valueType).asAny<V>()
                        to[mapKey] = mapValue
                    }
                }
            }
        }
    }
}

fun Class<*>.resolveBean(): BeanDef {
    return BeanDef.resolve(this)
}

fun Any.beanToMap(): Map<String, Any?> {
    return BeanDef.toMap(this)
}

fun <K, V> Any.beanToMap(type: Type): Map<K, V> {
    return BeanDef.toMap(this, type)
}

fun <K, V> Any.beanToMap(typeRef: TypeRef<Map<K, V>>): Map<K, V> {
    return BeanDef.toMap(this, typeRef)
}

fun <K, V> Any.beanToMap(mapSchema: MapSchema, copyOptions: BeanDef.CopyOptions): Map<K, V> {
    return BeanDef.toMap(this, mapSchema, copyOptions)
}

fun Any.copyProperties(to: Any, ignoreNull: Boolean = false) {
    BeanDef.copyProperties(this, to, ignoreNull)
}

fun Any.copyProperties(to: MutableMap<String, Any?>, ignoreNull: Boolean = false) {
    BeanDef.copyProperties(this, to, ignoreNull)
}

fun Any.copyProperties(to: Any, copyOptions: BeanDef.CopyOptions) {
    BeanDef.copyProperties(this, to, copyOptions)
}

fun <K, V> Any.copyProperties(to: MutableMap<K, V>, toSchema: MapSchema, copyOptions: BeanDef.CopyOptions) {
    BeanDef.copyProperties(this, to, copyOptions)
}

interface PropertyDef {

    @Suppress(INAPPLICABLE_JVM_NAME)
    val name: String
        @JvmName("name") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val type: Class<*>
        @JvmName("type") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val genericType: Type
        @JvmName("genericType") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val owner: Class<*>
        @JvmName("owner") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val isReadable: Boolean
        @JvmName("isReadable") get() {
            return getter !== null
        }

    @Suppress(INAPPLICABLE_JVM_NAME)
    val isWriteable: Boolean
        @JvmName("isWriteable") get() {
            return setter !== null
        }

    @Suppress(INAPPLICABLE_JVM_NAME)
    val getter: VirtualInvoker?
        @JvmName("getter") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val setter: VirtualInvoker?
        @JvmName("setter") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val field: Field?
        @JvmName("field") get

    @Suppress(INAPPLICABLE_JVM_NAME)
    val fieldAnnotations: List<Annotation>
        @JvmName("fieldAnnotations") get

    fun <T> getValue(bean: Any): T

    fun setValue(bean: Any, value: Any?)

    fun setValue(bean: Any, value: Any?, converter: Converter) {
        setValue(bean, converter.convert(value, genericType))
    }
}

private class BeanDefImpl(override val type: Class<*>) : BeanDef {

    override val properties: Map<String, PropertyDef> by lazy { tryProperties() }

    private fun tryProperties(): Map<String, PropertyDef> {
        val beanInfo = Introspector.getBeanInfo(type)
        val properties = LinkedHashMap<String, PropertyDef>()
        for (propertyDescriptor in beanInfo.propertyDescriptors) {
            val propertyDef = PropertyDefImpl(type, propertyDescriptor)
            properties[propertyDef.name] = propertyDef
        }
        return properties.toMap()
    }
}

private class PropertyDefImpl(
    override val owner: Class<*>,
    descriptor: PropertyDescriptor
) : PropertyDef {

    override val name: String = descriptor.name
    override val type: Class<*> = descriptor.propertyType
    override val genericType: Type by lazy { tryGenericType() }
    override val getter: VirtualInvoker? by lazy { tryGetter() }
    private val getterMethod: Method? = descriptor.readMethod
    override val setter: VirtualInvoker? by lazy { trySetter() }
    private val setterMethod: Method? = descriptor.writeMethod
    override val field: Field? by lazy { tryField() }
    override val fieldAnnotations: List<Annotation> by lazy { tryFieldAnnotations() }

    private fun tryGenericType(): Type {
        return if (getterMethod !== null) {
            getterMethod.genericReturnType
        } else {
            setterMethod!!.genericParameterTypes[0]
        }
    }

    private fun tryGetter(): VirtualInvoker? {
        return if (getterMethod === null) null else virtualInvoker(getterMethod)
    }

    private fun trySetter(): VirtualInvoker? {
        return if (setterMethod === null) null else virtualInvoker(setterMethod)
    }

    private fun tryField(): Field? {
        return owner.findField(name, declared = true, deep = true)
    }

    private fun tryFieldAnnotations(): List<Annotation> {
        val f = field
        return if (f === null) emptyList() else f.annotations.asList()
    }

    override fun <T> getValue(bean: Any): T {
        val g = getter
        return if (g !== null) {
            g.invokeVirtual(bean)
        } else {
            throw UnsupportedOperationException("This property is not readable: $name")
        }
    }

    override fun setValue(bean: Any, value: Any?) {
        val s = setter
        if (s !== null) {
            s.invokeVirtual<Any?>(bean, value)
        } else {
            throw UnsupportedOperationException("This property is not writeable: $name")
        }
    }
}