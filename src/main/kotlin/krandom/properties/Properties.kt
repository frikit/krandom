package krandom.properties

import mu.KotlinLogging

private val logger = KotlinLogging.logger(Properties::class.java.simpleName)

class Properties {

    //global var for output initialization of variable
    private var isFirstTimeRunning: Boolean = true

    //double
    val maxDouble: Double = Double.MAX_VALUE
    val minDouble: Double = Double.MIN_VALUE

    //float
    val maxFloat: Float = Float.MAX_VALUE
    val minFloat: Float = Float.MIN_VALUE

    //long
    val maxLong: Long = Long.MAX_VALUE
    val minLong: Long = Long.MIN_VALUE

    //int
    val maxInt: Int = Int.MAX_VALUE
    val minInt: Int = Int.MIN_VALUE

    //short
    val maxShort: Short = Short.MAX_VALUE
    val minShort: Short = Short.MIN_VALUE

    //byte
    val maxByte: Byte = Byte.MAX_VALUE
    val minByte: Byte = Byte.MIN_VALUE

    init {
        if (isFirstTimeRunning) {
            logger.info("Properties:")
            logger.info("Found DOUBLE { max = $maxDouble, min = $minDouble")
            logger.info("Found FLOAT  { max = $maxFloat, MIN = $minFloat")
            logger.info("Found LONG   { max = $maxLong, MIN = $minLong")
            logger.info("Found INT    { max = $maxInt, MIN = $minInt")
            logger.info("Found SHORT  { max = $maxShort, MIN = $minShort")
            logger.info("Found BYTE   { max = $maxByte, MIN = $minByte")

            isFirstTimeRunning = false
        }
    }
}