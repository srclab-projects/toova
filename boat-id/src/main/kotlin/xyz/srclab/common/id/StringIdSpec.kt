package xyz.srclab.common.id

import xyz.srclab.common.base.CharsTemplate
import xyz.srclab.common.base.CharsTemplate.Companion.resolveTemplate
import xyz.srclab.common.id.StringIdSpec.Companion.DEFAULT_COMPONENT_FACTORY_GENERATORS
import java.util.*
import kotlin.collections.HashMap

/**
 * Specification implementation of [IdFactory]. For example:
 * ```
 * String spec = "seq-{timeCount, yyyyMMddHHmmssSSS, 1023, %17s%04d}-tail";
 * StringIdSpec idFactory = new StringIdSpec(spec);
 * ```
 * The result may be:
 * ```
 * seq-202007311734474130000-tail
 * seq-202007311734474130001-tail
 * seq-202007311734474140000-tail
 * seq-202007311734474140001-tail
 * ```
 * An [StringIdSpec] consists of literals and [IdComponentFactory]s,
 * [IdComponentFactory]s are wrapped between {}, format is {name[, param]+} (**a comma following a space**).
 *
 * Using `\` to escape `{` out of parameter scope, and escape `}` in parameter scope:
 * ```
 * seq\{}-{timeCount, yyyyMMddHHmmssSSS, 1023, %17s%04d}-tail
 * will output:
 * seq{}-202007311734474130000-tail
 *
 * seq\{\}-{timeCount, yyyyMMddHHmmssSSS, 1023, %17s%04d}\-tail
 * will output:
 * seq{\}-202007311734474130000\-tail
 * ```
 *
 * There are default name of [IdComponentFactory]:
 * * [DEFAULT_COMPONENT_FACTORY_GENERATORS]
 *
 * Note arguments type of those [IdComponentFactory]s only support [String] type.
 *
 * @author sunqian
 *
 * @see IdFactory
 */
class StringIdSpec @JvmOverloads constructor(
    spec: String,
    componentFactoryGenerators: Map<String, (args: Array<String>) -> IdComponentFactory<*>> =
        DEFAULT_COMPONENT_FACTORY_GENERATORS,
) : IdFactory<String> {

    private val template: CharsTemplate
    private val parameters: Map<String, IdComponentFactory<*>>

    init {

        fun findIdComponentFactory(name: String, args: Array<String>): IdComponentFactory<*> {
            val func = componentFactoryGenerators[name]
            if (func === null) {
                throw IllegalArgumentException("Cannot find IdComponentFactory: $name.")
            }
            return func(args)
        }

        template = spec.resolveTemplate("{", "}", "\\")
        parameters = HashMap()

        for (parameter in template.parameters) {
            val parameterValue = parameter.toString()
            val split = parameterValue.split(", ")
            if (split.size <= 1) {
                parameters[parameterValue] = findIdComponentFactory(parameterValue, emptyArray())
            } else {
                val name = split[0]
                val args = split.subList(1, split.size).toTypedArray()
                parameters[parameterValue] = findIdComponentFactory(name, args)
            }
        }
    }

    override fun create(): String {
        val components = ArrayList<IdComponentHolder<*>>(parameters.size)
        val context = IdContext.newContext(components)
        val args = HashMap<String, Any?>()
        for (parameter in parameters) {
            val component = IdComponentHolder.newIdComponent(parameter.value, context)
            components.add(component)
            args[parameter.key] = LazyToString(parameter.value, context)
        }
        return template.process(args)
    }

    private class LazyToString(private val componentFactory: IdComponentFactory<*>, private val context: IdContext) {
        override fun toString(): String {
            return componentFactory.create(context).toString()
        }
    }

    companion object {

        const val TIME_COUNT_COMPONENT_FACTORY_KEY = "timeCount"

        @JvmField
        val DEFAULT_COMPONENT_FACTORY_GENERATORS: Map<String, (args: Array<String>) -> IdComponentFactory<*>>

        init {
            val map = HashMap<String, (args: Array<String>) -> IdComponentFactory<*>>()
            map[TIME_COUNT_COMPONENT_FACTORY_KEY] = { TimeCountComponentFactory(it) }
            DEFAULT_COMPONENT_FACTORY_GENERATORS = Collections.unmodifiableMap(map)
        }
    }
}