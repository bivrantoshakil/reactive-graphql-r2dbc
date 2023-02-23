package jp.co.anyx.constant

enum class SalesStatementInterval(private val interval: String) {
    BY_MINUTE("minute"),
    HOURLY("hour"),
    DAILY("day"),
    MONTHLY("month"),
    YEAR("year");

    override fun toString(): String {
        return this.interval
    }
}
