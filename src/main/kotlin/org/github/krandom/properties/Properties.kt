package org.github.krandom.properties

import mu.KotlinLogging

object Properties {
    private val logger = KotlinLogging.logger(Properties::class.java.simpleName)

    //global var for output initialization of variable
    private var isFirstTimeRunning: Boolean = true

    //double
    val maxDouble: Double = Double.MAX_VALUE
    val minDouble: Double = Double.MIN_VALUE

    //float
    val maxFloat: Float = Float.MAX_VALUE
    val minFloat: Float = Float.MIN_VALUE

    //long
    const val maxLong: Long = Long.MAX_VALUE
    const val minLong: Long = Long.MIN_VALUE

    //int
    const val maxInt: Int = Int.MAX_VALUE
    const val minInt: Int = Int.MIN_VALUE

    init {
        if (isFirstTimeRunning) {
            logger.info("Properties:")
            logger.info("Found DOUBLE { MAX = $maxDouble, MIN = $minDouble")
            logger.info("Found FLOAT  { MAX = $maxFloat, MIN = $minFloat")
            logger.info("Found LONG   { MAX = $maxLong, MIN = $minLong")
            logger.info("Found INT    { MAX = $maxInt, MIN = $minInt")

            isFirstTimeRunning = false
        }
    }
}
