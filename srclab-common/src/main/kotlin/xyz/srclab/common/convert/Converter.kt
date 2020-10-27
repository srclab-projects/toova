package xyz.srclab.common.convert

import xyz.srclab.common.base.*
import xyz.srclab.common.bean.propertiesToBean
import xyz.srclab.common.bean.propertiesToMap
import xyz.srclab.common.collection.BaseIterableOps.Companion.toAnyArray
import xyz.srclab.common.collection.IterableSchema
import xyz.srclab.common.collection.arrayAsList
import xyz.srclab.common.collection.componentType
import xyz.srclab.common.collection.resolveIterableSchemaOrNull
import xyz.srclab.common.reflect.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

interface Converter {

    @Throws(UnsupportedOperationException::class)
    fun <T> convert(from: Any?, toType: Class<T>): T

    @Throws(UnsupportedOperationException::class)
    fun <T> convert(from: Any?, toType: Type): T

    @Throws(UnsupportedOperationException::class)
    fun <T> convert(from: Any?, toTypeRef: TypeRef<T>): T {
        return convert(from, toTypeRef.type)
    }

    @Throws(UnsupportedOperationException::class)
    fun <T> convert(from: Any?, fromType: Type, toType: Type): T

    @Throws(UnsupportedOperationException::class)
    fun <T> convert(from: Any?, fromTypeRef: TypeRef<T>, toTypeRef: TypeRef<T>): T {
        return convert(from, fromTypeRef.type, toTypeRef.type)
    }

    class Builder : HandlersCachingProductBuilder<Converter, ConvertHandler, Builder>() {

        override fun buildNew(): Converter {
            return ConverterImpl(handlers())
        }

        private class ConverterImpl(private val handlers: List<ConvertHandler>) : Converter {

            override fun <T> convert(from: Any?, toType: Class<T>): T {
                for (handler in handlers) {
                    val result = handler.convert(from, toType, this)
                    if (result === NULL_VALUE) {
                        return null as T
                    }
                    if (result !== null) {
                        return result.asAny()
                    }
                }
                throw UnsupportedOperationException("Cannot convert $from to $toType.")
            }

            override fun <T> convert(from: Any?, toType: Type): T {
                for (handler in handlers) {
                    val result = handler.convert(from, toType, this)
                    if (result === NULL_VALUE) {
                        return null as T
                    }
                    if (result !== null) {
                        return result.asAny()
                    }
                }
                throw UnsupportedOperationException("Cannot convert $from to $toType.")
            }

            override fun <T> convert(from: Any?, fromType: Type, toType: Type): T {
                for (handler in handlers) {
                    val result = handler.convert(from, fromType, toType, this)
                    if (result === NULL_VALUE) {
                        return null as T
                    }
                    if (result !== null) {
                        return result.asAny()
                    }
                }
                throw UnsupportedOperationException("Cannot convert $fromType to $toType.")
            }
        }
    }

    companion object {

        @JvmField
        val DEFAULT: Converter = newBuilder().addHandlers(ConvertHandler.DEFAULTS).build()

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }

        @JvmStatic
        @Throws(UnsupportedOperationException::class)
        fun <T> convertTo(from: Any?, toType: Class<T>): T {
            return DEFAULT.convert(from, toType)
        }

        @JvmStatic
        @Throws(UnsupportedOperationException::class)
        fun <T> convertTo(from: Any?, toType: Type): T {
            return DEFAULT.convert(from, toType)
        }

        @JvmStatic
        @Throws(UnsupportedOperationException::class)
        fun <T> convertTo(from: Any?, toTypeRef: TypeRef<T>): T {
            return DEFAULT.convert(from, toTypeRef)
        }

        @JvmStatic
        @Throws(UnsupportedOperationException::class)
        fun <T> convertTo(from: Any?, fromType: Type, toType: Type): T {
            return DEFAULT.convert(from, fromType, toType)
        }

        @JvmStatic
        @Throws(UnsupportedOperationException::class)
        fun <T> convertTo(from: Any?, fromTypeRef: TypeRef<T>, toTypeRef: TypeRef<T>): T {
            return DEFAULT.convert(from, fromTypeRef, toTypeRef)
        }
    }
}

@Throws(UnsupportedOperationException::class)
fun <T> Any?.convertTo(toType: Class<T>): T {
    return Converter.convertTo(this, toType)
}

@Throws(UnsupportedOperationException::class)
fun <T> Any?.convertTo(toType: Type): T {
    return Converter.convertTo(this, toType)
}

@Throws(UnsupportedOperationException::class)
fun <T> Any?.convertTo(toTypeRef: TypeRef<T>): T {
    return Converter.convertTo(this, toTypeRef)
}

@Throws(UnsupportedOperationException::class)
fun <T> Any?.convertTo(fromType: Type, toType: Type): T {
    return Converter.convertTo(this, fromType, toType)
}

@Throws(UnsupportedOperationException::class)
fun <T> Any?.convertTo(fromTypeRef: TypeRef<T>, toTypeRef: TypeRef<T>): T {
    return Converter.convertTo(this, fromTypeRef, toTypeRef)
}

interface ConvertHandler {

    /**
     * Return null if [from] cannot be converted, return [NULL_VALUE] if result value is null.
     */
    fun convert(from: Any?, toType: Class<*>, converter: Converter): Any?

    /**
     * Return null if [from] cannot be converted, return [NULL_VALUE] if result value is null.
     */
    fun convert(from: Any?, toType: Type, converter: Converter): Any?

    /**
     * Return null if [from] cannot be converted, return [NULL_VALUE] if result value is null.
     */
    fun convert(from: Any?, fromType: Type, toType: Type, converter: Converter): Any?

    companion object {

        @JvmField
        val DEFAULTS: List<ConvertHandler> = listOf(
            NopConvertHandler,
            CharsConvertHandler,
            NumberAndPrimitiveConvertHandler,
            DateTimeConvertHandler.DEFAULT,
            UpperBoundConvertHandler,
            IterableConvertHandler,
            BeanConvertHandler,
        )
    }
}

abstract class AbstractConvertHandler : ConvertHandler {

    override fun convert(from: Any?, toType: Class<*>, converter: Converter): Any? {
        if (from === null) {
            return null
        }
        return convertFromNotNull(from, from.javaClass, toType, converter)
    }

    override fun convert(from: Any?, toType: Type, converter: Converter): Any? {
        if (from === null) {
            return null
        }
        return convertFromNotNull(from, from.javaClass, toType, converter)
    }

    override fun convert(from: Any?, fromType: Type, toType: Type, converter: Converter): Any? {
        if (from === null) {
            return null
        }
        return convertFromNotNull(from, fromType, toType, converter)
    }

    protected abstract fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any?
}

object NopConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return when {
            fromType == toType -> from
            fromType is Class<*> && toType is Class<*> && toType.isAssignableFrom(fromType) -> from
            else -> null
        }
    }
}

object CharsConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return when (toType) {
            String::class.java, CharSequence::class.java -> from.toString()
            StringBuilder::class.java -> StringBuilder(from.toString())
            StringBuffer::class.java -> StringBuffer(from.toString())
            CharArray::class.java -> from.toString().toCharArray()
            else -> null
        }
    }
}

object NumberAndPrimitiveConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return when (toType) {
            Boolean::class.javaPrimitiveType, Boolean::class.java -> from.toBoolean()
            Byte::class.javaPrimitiveType, Byte::class.java -> from.toByte()
            Short::class.javaPrimitiveType, Short::class.java -> from.toShort()
            Char::class.javaPrimitiveType, Char::class.java -> from.toChar()
            Int::class.javaPrimitiveType, Int::class.java -> from.toInt()
            Long::class.javaPrimitiveType, Long::class.java -> from.toLong()
            Float::class.javaPrimitiveType, Float::class.java -> from.toFloat()
            Double::class.javaPrimitiveType, Double::class.java -> from.toDouble()
            BigInteger::class.java -> from.toBigInteger()
            BigDecimal::class.java -> from.toBigDecimal()
            else -> null
        }
    }
}

open class DateTimeConvertHandler(
    protected val dateFormat: DateFormat,
    protected val instantFormatter: DateTimeFormatter,
    protected val localDateTimeFormatter: DateTimeFormatter,
    protected val zonedDateTimeFormatter: DateTimeFormatter,
    protected val offsetDateTimeFormatter: DateTimeFormatter,
    protected val localDateFormatter: DateTimeFormatter,
    protected val localTimeFormatter: DateTimeFormatter,
) : AbstractConvertHandler() {

    constructor() : this(
        dateFormat(),
        DateTimeFormatter.ISO_INSTANT,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_ZONED_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_LOCAL_TIME
    )

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return when (toType) {
            Date::class.java -> from.toDate(dateFormat)
            Instant::class.java -> from.toInstant(instantFormatter)
            LocalDateTime::class.java -> from.toLocalDateTime(localDateTimeFormatter)
            ZonedDateTime::class.java -> from.toZonedDateTime(zonedDateTimeFormatter)
            OffsetDateTime::class.java -> from.toOffsetDateTime(offsetDateTimeFormatter)
            LocalDate::class.java -> from.toLocalDate(localDateFormatter)
            LocalTime::class.java -> from.toLocalTime(localTimeFormatter)
            Duration::class.java -> from.toDuration()
            else -> null
        }
    }

    companion object {

        @JvmField
        val DEFAULT = DateTimeConvertHandler()
    }
}

object UpperBoundConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return converter.convert(from, fromType.upperBound, toType.upperBound)
    }
}

object IterableConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        val fromClass = from.javaClass
        if (fromClass.isArray) {
            return iterableToType(from.arrayAsList(), toType, converter)
        }
        if (from is Iterable<*>) {
            return iterableToType(from as Iterable<Any?>, toType, converter)
        }
        return null
    }

    private fun iterableToType(iterable: Iterable<Any?>, toType: Type, converter: Converter): Any? {
        val toComponentType = toType.componentType
        if (toComponentType !== null) {
            return iterable
                .map { converter.convert<Any?>(it, toComponentType) }
                .toAnyArray(toComponentType.upperClass)
        }
        val iterableSchema = toType.resolveIterableSchemaOrNull()
        if (iterableSchema === null) {
            return null
        }
        return if (iterable is Collection<*>)
            collectionMapTo(iterable, iterableSchema, converter)
        else
            iterableMapTo(iterable, iterableSchema, converter)
    }

    private fun iterableMapTo(
        iterable: Iterable<Any?>,
        iterableSchema: IterableSchema,
        converter: Converter
    ): Iterable<Any?>? {
        return when (iterableSchema.rawClass) {
            Iterable::class.java, List::class.java, LinkedList::class.java -> iterable.mapTo(LinkedList()) {
                converter.convert(it, iterableSchema.componentType)
            }
            ArrayList::class.java -> iterable.mapTo(ArrayList()) {
                converter.convert(it, iterableSchema.componentType)
            }
            Collection::class.java, Set::class.java, LinkedHashSet::class.java -> iterable.mapTo(LinkedHashSet()) {
                converter.convert(it, iterableSchema.componentType)
            }
            HashSet::class.java -> iterable.mapTo(HashSet()) {
                converter.convert(it, iterableSchema.componentType)
            }
            TreeSet::class.java -> iterable.mapTo(TreeSet()) {
                converter.convert(it, iterableSchema.componentType)
            }
            else -> null
        }
    }

    private fun collectionMapTo(
        collection: Collection<Any?>,
        iterableSchema: IterableSchema,
        converter: Converter
    ): Iterable<Any?>? {
        return when (iterableSchema.rawClass) {
            Iterable::class.java, List::class.java, ArrayList::class.java -> collection.mapTo(ArrayList(collection.size)) {
                converter.convert(it, iterableSchema.componentType)
            }
            LinkedList::class.java -> collection.mapTo(LinkedList()) {
                converter.convert(it, iterableSchema.componentType)
            }
            Collection::class.java, Set::class.java, LinkedHashSet::class.java -> collection.mapTo(
                LinkedHashSet(
                    collection.size
                )
            ) {
                converter.convert(it, iterableSchema.componentType)
            }
            HashSet::class.java -> collection.mapTo(HashSet(collection.size)) {
                converter.convert(it, iterableSchema.componentType)
            }
            TreeSet::class.java -> collection.mapTo(TreeSet()) {
                converter.convert(it, iterableSchema.componentType)
            }
            else -> null
        }
    }
}

object BeanConvertHandler : AbstractConvertHandler() {

    override fun convertFromNotNull(from: Any, fromType: Type, toType: Type, converter: Converter): Any? {
        return when (toType) {
            is Class<*> -> return convert0(from, toType, toType, converter)
            is ParameterizedType -> return convert0(from, toType.rawClass, toType, converter)
            else -> null
        }
    }

    private fun convert0(from: Any, toRawClass: Class<*>, toType: Type, converter: Converter): Any? {
        return when (toRawClass) {
            Map::class.java -> from.propertiesToMap(HashMap<Any, Any?>(), toType, converter).toMap()
            MutableMap::class.java, HashMap::class.java -> return from.propertiesToMap(
                HashMap<Any, Any?>(),
                toType,
                converter
            )
            LinkedHashMap::class.java -> return from.propertiesToMap(LinkedHashMap<Any, Any?>(), toType, converter)
            TreeMap::class.java -> return from.propertiesToMap(TreeMap<Any, Any?>(), toType, converter)
            ConcurrentHashMap::class.java -> return from.propertiesToMap(
                ConcurrentHashMap<Any, Any?>(),
                toType,
                converter
            )
            else -> {
                val toInstance = toRawClass.toInstance<Any>()
                return from.propertiesToBean(toInstance, toType, converter)
            }
        }
    }
}