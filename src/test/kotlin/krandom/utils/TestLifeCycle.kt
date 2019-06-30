package krandom.utils

import mu.KLogger
import mu.KotlinLogging

object TestLifecycle {
    private val kLogger: KLogger = KotlinLogging.logger(TestLifecycle::class.java.simpleName)

    private const val doubleTab = "\t\t"
    private const val NEW_LINE_DOUBLE_TAB = "\n" + doubleTab
    private const val FORMAT_LINE = "# %s"

    private const val START_MSG = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------START TEST----------------------------------#\n" +
            doubleTab +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    private const val FINISH_MSG = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------FINISH TEST---------------------------------#\n" +
            doubleTab +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    private const val FAIL_MSG_FIRST_PART = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------FAIL TEST-----------------------------------#\n" +
            doubleTab +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "# with Exception =>\n"

    private const val FAIL_MSG_SECOND_PART = NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    fun onTestStart(methodName: String) {
        if (checkValidMethodName(methodName)) {
            kLogger.info(String.format(START_MSG, fillWithEmptySpacesOrReturn(methodName)))
        }
    }

    fun onTestFinish(methodName: String) {
        if (checkValidMethodName(methodName)) {
            kLogger.info(String.format(FINISH_MSG, fillWithEmptySpacesOrReturn(methodName)))
        }
    }

    fun onTestStep(logger: KLogger, message: String) {
        if (checkValidMethodName(message)) {
            logger.info { message }
        }
    }

    // private methods
    private fun fillWithEmptySpacesOrReturn(message: String): String {
        return if (message.length > 76) {
            message
        } else {
            centerText(message) + "#"
        }
    }

    private fun centerText(text: String, len: Int = 78): String {
        val out: String = String.format("%" + len + "s%s%" + len + "s", " ", text, " ")
        val mid = (out.length / 2)
        val start = (mid - (len / 2))
        val end = (start + len)
        return out.substring(start, end - 1)
    }

    private fun checkValidMethodName(methodName: String): Boolean {
        return methodName != "" && methodName.replaceFirst("\\s+", "") != ""
    }
}
