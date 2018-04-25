package krandom.utils

import mu.KLogger
import mu.KotlinLogging

class TestLifecycle {
    val LOGGER: KLogger = KotlinLogging.logger(TestLifecycle::class.java.simpleName)

    val DOUBLE_TAB = "\t\t"
    val NEW_LINE_DOUBLE_TAB = "\n" + DOUBLE_TAB
    val FORMAT_LINE = "# %s"

    val START_MSG = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------START TEST----------------------------------#\n" +
            DOUBLE_TAB +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    val FINISH_MSG = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------FINISH TEST---------------------------------#\n" +
            DOUBLE_TAB +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    val FAIL_MSG_FIRST_PART = NEW_LINE_DOUBLE_TAB +
            "#----------------------------------FAIL TEST-----------------------------------#\n" +
            DOUBLE_TAB +
            FORMAT_LINE +
            NEW_LINE_DOUBLE_TAB +
            "# with Exception =>\n"

    val FAIL_MSG_SECOND_PART = NEW_LINE_DOUBLE_TAB +
            "#------------------------------------------------------------------------------#\n"

    fun onTestStart(methodName: String) {
        if (checkValidMethodName(methodName)) {
            LOGGER.info(String.format(START_MSG, fillWithEmptySpacesOrReturn(methodName)))
        }
    }

    fun onTestFinish(methodName: String) {
        if (checkValidMethodName(methodName)) {
            LOGGER.info(String.format(FINISH_MSG, fillWithEmptySpacesOrReturn(methodName)))
        }
    }

    // private methods
    private fun fillWithEmptySpacesOrReturn(message: String): String {
        if (message.length > 76) {
            return message
        } else {
            return centerText(message, 78) + "#"
        }
    }

    private fun centerText(text: String, len: Int): String {
        val out: String = String.format("%" + len + "s%s%" + len + "s", " ", text, " ")
        val mid = (out.length / 2)
        val start = (mid - (len / 2))
        val end = (start + len)
        return out.substring( start, end -1)
    }

    private fun checkValidMethodName(methodName: String): Boolean {
        return methodName != "" && methodName.replaceFirst("\\s+", "") != ""
    }
}
