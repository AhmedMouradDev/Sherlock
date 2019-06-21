package inc.ahmedmourad.sherlock.domain.device

interface DateManager {

    fun getRelativeDateTimeString(timeMillis: Long, minResolution: Long = MINUTE_IN_MILLIS, transitionResolution: Long = YEAR_IN_MILLIS): String

    companion object {
        const val SECOND_IN_MILLIS = 1000L
        const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60L
        const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60L
        const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24L
        const val WEEK_IN_MILLIS = DAY_IN_MILLIS * 7L
        const val YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52L
    }
}
