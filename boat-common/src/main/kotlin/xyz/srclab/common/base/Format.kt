package xyz.srclab.common.base

import org.slf4j.helpers.MessageFormatter as MessageFormatterSlf4j
import java.text.MessageFormat as MessageFormatKt

interface Format {

    fun format(pattern: CharSequence, vararg args: Any?): String

    companion object {

        @JvmField
        val FAST_FORMAT: FastFormat = FastFormat

        @JvmField
        val PRINTF_FORMAT: PrintfFormat = PrintfFormat

        @JvmField
        val MESSAGE_FORMAT: MessageFormat = MessageFormat

        @JvmStatic
        fun CharSequence.fastFormat(vararg args: Any?): String {
            return FastFormat.format(this, *args)
        }

        @JvmStatic
        fun CharSequence.printfFormat(vararg args: Any?): String {
            return PrintfFormat.format(this, *args)
        }

        @JvmStatic
        fun CharSequence.messageFormat(vararg args: Any?): String {
            return MessageFormat.format(this, *args)
        }
    }
}

object FastFormat : Format {

    override fun format(pattern: CharSequence, vararg args: Any?): String {
        processArguments(args)
        return MessageFormatterSlf4j.arrayFormat(pattern.toString(), args, null).message
    }

    private fun processArguments(array: Array<out Any?>) {
        if (array.isEmpty()) {
            return
        }

        /*
        val lastElement = args[args.size - 1]
        if (lastElement is Throwable) {
            args[args.size - 1] = lastElement.toString()
        }
         */
    }
}

object PrintfFormat : Format {

    override fun format(pattern: CharSequence, vararg args: Any?): String {
        return String.format(Defaults.locale, pattern.toString(), *args)
    }
}

object MessageFormat : Format {

    override fun format(pattern: CharSequence, vararg args: Any?): String {
        return MessageFormatKt.format(pattern.toString(), *args)
    }
}